# -*- coding: utf-8 -*-
from twisted.internet.protocol import Protocol
from twisted.protocols.basic import LineReceiver
import MessageParser
import json
import appVar

INIT = 1
WAIT_FOR_HII_OR_MULTITALK = 3
WAIT_FOR_LOG = 4
CONNECTED = 5
MULTITALK_TAG = 'MULTITALK_5387132'

class TCPProtocol(LineReceiver):
    
    def __init__(self):
        self.state = INIT
        self.delimiter = "\n"
    
    def jsonReceived(self,  jsonObj):
        self.logMsg("odebralem obiekt json")
        msgType = jsonObj['TYPE']
        
        if msgType == "HII" and self.state == WAIT_FOR_HII_OR_MULTITALK:
            appVar.coreInstance.handleHiiMessage(jsonObj,  self)
        elif msgType == "LOG" and self.state == WAIT_FOR_LOG:
            if appVar.coreInstance.handleLogMessage(jsonObj,  self):
                self.state = CONNECTED
            else:
                self.logMsg("odrzucono logowanie - przerywam polaczenie")
                self.transport.loseConnection()
        else:
            self.logMsg("bledny typ wiadomosci '%s' w stanie %d" % (msgType,  self.state))
            self.transport.loseConnection()
    
    def sendPacket(self,  msg):
        self.transport.write(msg)
        self.transport.write("\n")
        self.logMsg("wyslano dane ")
    
    def connectionMade(self):
        self.logMsg("nowe polaczenie")
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
                # to my utworzylismy polaczenie wiec wysylamy MULTITALK_5387132 i czekamy na HII msg
                self.logMsg("czekam na znacznik %s")
                self.sendPacket(MULTITALK_TAG)
                self.state = WAIT_FOR_HII_OR_MULTITALK
            else:
                # ktos do nas sie podlaczyl wiec poprostu czekamy na HII lub MULTITALK_5387132
                self.logMsg("czekam na hii/multitalk message")
                self.state = WAIT_FOR_HII_OR_MULTITALK
        else:
            self.logMsg("bledny stan protokolu")
            assert(False)
        
    def connectionLost(self, reason):
        self.logMsg("przerwano polaczenie")
        appVar.tcpManager.delConnection(self)
    
    def lineReceived(self, line):
        self.logMsg("otrzymałem linie tekstu")
        if self.state == WAIT_FOR_HII_OR_MULTITALK and line == MULTITALK_TAG:
            #ktos podlaczyl sie do nas podajac z palca IP wiec wysylamy mu HII i oczekujemy na LOG
            msgToSend = MessageParser.getFullHiiMsg()
            self.logMsg("wysylam hi msg '%s'" % msgToSend)
            self.sendPacket(msgToSend)
            self.state = WAIT_FOR_LOG
            return
        elif self.state == WAIT_FOR_HII_OR_MULTITALK:
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
            self.__deserializeJson()
            
        else:
            self.logMsg("odczytano fragment pakietu %s" % len(self.packet))
            
        
    def __deserializeJson(self):
        assert(self.packet)
        self.logMsg("deserializacja jsona '%s'" % self.packet)
        jsonObj = json.loads(self.packet)
        if jsonObj:
            self.jsonReceived(jsonObj)
        else:
            self.logMsg("nie mozna zdeserializowac pakietu")
    
    def sendLogMsg(self):
        msgToSend = MessageParser.getFullLogMsg()
        self.logMsg("wysylam log msg '%s'" % msgToSend)
        self.sendPacket(msgToSend)
                    
    def logMsg(self,  msg):
        print "TCP: %s (%s)" %(msg,  self.transport.getPeer())

