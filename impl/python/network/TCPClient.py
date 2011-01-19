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
        self.protocol = protocol
    
    def startedConnecting(self, connector):
        self.logMsg('Started to connect.')
    
    def buildProtocol(self, addr):
        self.logMsg('Connected.')
        return self.protocol()
    
    def clientConnectionLost(self, connector, reason):
        self.logMsg('Lost connection.  Reason: %s' % reason)
    
    def clientConnectionFailed(self, connector, reason):
        self.logMsg('Connection failed. Reason: %s '% reason)
        dir(connector)
        appVar.tcpManager.connectionFailed(connector.transport.getPeer().host)

    def logMsg(self,  msg):
        print "TCP-CF: %s " % msg

def startTCPConnection(reactor,  addr):
    appVar.tcpManager.tryingToConnect(addr)
    reactor.connectTCP(addr,  3554,  TCPClientFactory(TCPClient))
    
def startReversedTCPConnection(reactor,  addr):
    reactor.connectTCP(addr,  3554,  TCPClientFactory(TCPReversedClient))
    
