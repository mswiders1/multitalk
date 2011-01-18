# -*- coding: utf-8 -*-
from twisted.internet.protocol import Protocol
from twisted.protocols.basic import LineReceiver
import MessageParser
import json
import appVar

INIT = 1
WAIT_FOR_HII = 2
WAIT_FOR_HII_OR_P2P = 3
WAIT_FOR_LOG = 4
WAIT_FOR_MTX = 5
CONNECTED = 6
DISCONNECTED = 7
MULTITALK_TAG = 'MULTITALK_5387132'

class TCPProtocol(LineReceiver):
    
    def __init__(self):
        self.state = INIT
        self.delimiter = "\n"
    
    def jsonReceived(self,  jsonObj):
        self.logMsg("odebralem obiekt json")
        msgType = jsonObj['TYPE']
        
        if msgType == "HII" and ( self.state == WAIT_FOR_HII_OR_P2P or self.state == WAIT_FOR_HII):
            appVar.coreInstance.handleHiiMessage(jsonObj,  self)
            self.state = WAIT_FOR_MTX
        elif msgType == "P2P" and self.state == WAIT_FOR_HII_OR_P2P:
            # otrzymalismy P2P wiec przedstawiamy sie i oczekujemy na LOG
            msgToSend = MessageParser.getFullHiiMsg()
            self.logMsg("wysyłam wiadomosc i czekam na log msg : '%s'" % msgToSend)
            self.sendPacket(msgToSend)
            self.state = WAIT_FOR_LOG
        elif msgType == "LOG" and self.state == WAIT_FOR_LOG:
            if appVar.coreInstance.handleLogMessage(jsonObj,  self):
                self.state = CONNECTED
            else:
                self.logMsg("odrzucono logowanie - przerywam polaczenie")
                self.transport.loseConnection()
        elif msgType == 'MTX' and self.state == WAIT_FOR_MTX:
            appVar.coreInstance.handleMtxMessage(jsonObj)
            self.state = CONNECTED
        elif msgType == 'OUT' and self.state == CONNECTED:
            appVar.coreInstance.handleOutMessage(jsonObj)
            self.state = DISCONNECTED
        elif msgType == 'LIV' and self.state == CONNECTED:
            appVar.coreInstance.handleLivMessage(jsonObj)
        else:
            self.logMsg("bledny typ wiadomosci '%s' w stanie %d" % (msgType,  self.state))
            self.state = DISCONNECTED
            self.transport.loseConnection()
    
    def sendPacket(self,  msg):
        self.transport.write(msg)
        self.logMsg("wyslano dane ")
    
    def connectionMade(self):
        self.logMsg("nowe polaczenie")
        if not appVar.tcpManager.isNotConnectedToIp(self.transport.getPeer().host):
            #mamy juz polaczenie do niego wiec kazemy mu spadac
            self.logMsg("dziekuje ale mam juz takie polaczenie :)")
            self.transport.loseConnection()
            return
        self.transport.setTcpNoDelay(True)
        if self.state == INIT:
            appVar.tcpManager.newConnection(self)
            if self.isClient and self.isReversed == False:
                # to my utworzylismy polaczenie wiec wysylamy hii msg i czekamy na log
                self.state = WAIT_FOR_LOG
                msgToSend = MessageParser.getFullHiiMsg()
                self.logMsg("wysyłam wiadomosc i czekam na log msg : '%s'" % msgToSend)
                self.sendPacket(msgToSend)
            elif self.isClient and self.isReversed:
                # to my utworzylismy polaczenie wiec wysylamy P2P i czekamy na HII msg
                self.logMsg("czekam na znacznik %s")
                self.sendPacket(MessageParser.getFullP2pMsg())
                self.state = WAIT_FOR_HII
            else:
                # ktos do nas sie podlaczyl wiec poprostu czekamy na HII lub P2P
                self.logMsg("czekam na hii/multitalk message")
                self.state = WAIT_FOR_HII_OR_P2P
        else:
            self.logMsg("bledny stan protokolu")
            assert(False)
        
    def connectionLost(self, reason):
        self.logMsg("przerwano polaczenie")
        appVar.tcpManager.delConnection(self)
    
    def lineReceived(self, line):
        self.logMsg("otrzymałem linie tekstu")
        if self.state == WAIT_FOR_HII_OR_P2P and line == MULTITALK_TAG:
            #ktos podlaczyl sie do nas podajac z palca IP wiec wysylamy mu HII i oczekujemy na LOG
            msgToSend = MessageParser.getFullHiiMsg()
            self.logMsg("wysylam hi msg '%s'" % msgToSend)
            self.sendPacket(msgToSend)
            self.state = WAIT_FOR_LOG
            return
        elif self.state == WAIT_FOR_HII_OR_P2P:
            self.logMsg("niedopasowalem znacznika %s wiec oczekuja ze otrzymam HII" % MULTITALK_TAG)
            
        len = MessageParser.getMessageLen(line)
        if len:
            self.logMsg("otrzymalem naglowek - czekam na odczyt %d bajtow" % len)
            self.packetSize = len
            self.toRead = len
            #teraz chcemy odczytać wnętrze pakietu
            self.packet = ""
            self.setRawMode()
        else:
            #self.logMsg("przerywam polaczenie - bledy poczatek pakietu: '%s'" % line)
            #self.transport.loseConnection()
            self.logMsg("bledy poczatek pakietu - ignoruje go: '%s'" % line)

    def rawDataReceived(self,  data):
        self.packet += data
        if len(self.packet) >= self.packetSize:
            self.logMsg("odczytano caly pakiet")
            self.setLineMode()
            #wydzielamy z pakietu to co chcielismy otrzymac
            packet = self.packet[0:self.packetSize]
            restOfData = self.packet[self.packetSize:]
            self.__deserializeJson(packet)
            self.setLineMode()
            self.packet = None
            # reszte przekazujemy do ponownej analizy
            if len(restOfData):
                self.dataReceived(restOfData)
        else:
            self.logMsg("odczytano fragment pakietu %s" % len(self.packet))
            
        
    def __deserializeJson(self,  packet):
        assert(packet)
        self.logMsg("deserializacja jsona '%s'" % packet)
        jsonObj = json.loads(packet)
        if jsonObj:
            self.jsonReceived(jsonObj)
        else:
            self.logMsg("nie mozna zdeserializowac pakietu")
    
    def sendLogMsg(self):
        msgToSend = MessageParser.getFullLogMsg()
        self.logMsg("wysylam log msg '%s'" % msgToSend)
        self.sendPacket(msgToSend)
    
    def sendLivMsg(self):
        msgToSend = MessageParser.getFullLivMsg()
        self.sendPacket(msgToSend)
    
    def sendOutMsgAndCloseConnection(self):
        msgToSend = MessageParser.getFullOutMsg()
        self.logMsg("wysylam out msg '%s'" % msgToSend)
        self.sendPacket(msgToSend)
        self.transport.loseConnection()
        
    def sendMtxMsg(self):
        msgToSend = MessageParser.getFullMtxMsg()
        self.sendPacket(msgToSend)
        
    def logMsg(self,  msg):
        print "TCP: %s (%s)" %(msg,  self.transport.getPeer())

