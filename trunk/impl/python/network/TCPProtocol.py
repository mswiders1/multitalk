# -*- coding: utf-8 -*-
from twisted.internet.protocol import Protocol
from twisted.protocols.basic import LineReceiver
import MessageParser
import json

INIT = 1
WAIT_FOR_HII = 2
WAIT_FOR_LOG = 3

class TCPProtocol(LineReceiver):
    
    def __init__(self):
        self.state = INIT
    
    def jsonReceived(self,  jsonObj):
        self.logMsg("odebralem obiekt json")
        msgType = jsonObj['TYPE']
        
        if msgType == "HII" and self.state == WAIT_FOR_HII:
            appVar.coreInstance.handleHiiMessage(jsonObj)
        elif msgType == "LOG" and self.state == WAIT_FOR_LOG:
            appVar.coreInstance.handleLogMessage(jsonObj)
        else:
            self.logMsg("bledny typ wiadomosci '%s'" % msgType)
            self.transport.loseConnection()
    
    def sendJson(self,  jsonObj):
        serialized = json.dumps(jsonObj)
        self.transport.write(serialized)
        self.transport.flush()

    def connectionMade(self):
        self.logMsg("utworzono polaczenie")
        if self.state == INIT:
            if self.isClient:
                # to my utworzylismy polaczenie wiec wysylamy hii msg i czekamy na log
                self.state = WAIT_FOR_LOG
                msgToSend = MessageParser.getFullHiiMsg()
                self.transport.write(msgToSend)
            else:
                # ktos do nas sie podlaczyl wiec poprostu czekamy na HII od niego
                self.state = WAIT_FOR_HII
        else:
            self.logMsg("bledny stan protokolu")
            assert(False)
        
    def connectionLost(self, reason):
        self.logMsg("przerwano polaczenie")
    
    def lineReceived(self, line):
        len = MessageParser.getMessageLen(line)
        if len:
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
        if len(self.packet) == self.packetSize:
            self.logMsg("odczytano caly pakiet")
            self.setLineMode()
        else:
            self.logMsg("odczytano fragment pakietu")
            __deserializeJson()
        
    def __deserializeJson(self):
        assert(self.packet)
        jsonObj = self.jsonReceived(json.loads(self.packet))
        self.jsonReceived(jsonObj)
        
    def logMsg(self,  msg):
        print "TCP: %s (%s)" %(msg,  self.transport.getPeer())
        
