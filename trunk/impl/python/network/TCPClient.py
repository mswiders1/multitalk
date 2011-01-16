# -*- coding: utf-8 -*-

from twisted.internet.protocol import Protocol, Factory
from TCPProtocol import TCPProtocol
from twisted.internet.endpoints import TCP4ClientEndpoint

class TCPClient(TCPProtocol):
    
    def __init__(self):
        self.isClient = True
        TCPProtocol.__init__(self)
         
         

def startTCPConnection(reactor,  addr):
    factory = Factory()
    factory.protocol = TCPClient
    point = TCP4ClientEndpoint(reactor, addr, 3554)
    d = point.connect(factory)
    d.addCallback(gotProtocol)

    return factory
    
