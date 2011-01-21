# -*- coding: utf-8 -*-

from twisted.internet.protocol import Protocol, ClientFactory
from TCPProtocol import TCPProtocol
import TCPServer
import appVar

class TCPClient(TCPProtocol):
    
    def __init__(self):
        self.isClient = True
        self.isReversed = False
        TCPProtocol.__init__(self)

class TCPReversedClient(TCPProtocol):
    
    def __init__(self):
        self.isClient = True
        self.isReversed = True
        TCPProtocol.__init__(self)

class TCPClientFactory(ClientFactory):
    
    def __init__(self,  protocol):
        self.addr = None
        self.protocol = protocol
    
    def startedConnecting(self, connector):
        self.logMsg('Started to connect.')
    
    def buildProtocol(self, addr):
        self.logMsg('Connected.')
        return self.protocol()
    
    def clientConnectionLost(self, connector, reason):
        self.logMsg('utracono polaczenie. powod: %s' % reason)
        appVar.tcpManager.connectionFailed(self.addr)
    
    def clientConnectionFailed(self, connector, reason):
        self.logMsg('blad nawiazywania polaczenia. powod: %s '% reason)
        appVar.tcpManager.connectionFailed(self.addr)

    def logMsg(self,  msg):
        print "TCP-CF: %s " % msg

def startTCPConnection(reactor,  addr):
    appVar.tcpManager.tryingToConnect(addr)
    factory = TCPClientFactory(TCPClient)
    factory.addr = addr
    reactor.connectTCP(addr,  3554,  factory)
    
def startReversedTCPConnection(reactor,  addr):
    appVar.tcpManager.tryingToConnect(addr)
    factory = TCPClientFactory(TCPReversedClient)
    factory.addr = addr
    reactor.connectTCP(addr,  3554,  factory)
    
