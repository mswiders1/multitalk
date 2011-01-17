# -*- coding: utf-8 -*-

from twisted.internet.protocol import Protocol, Factory
from twisted.internet.endpoints import TCP4ServerEndpoint
from TCPProtocol import TCPProtocol
import appVar

class TCPServer(TCPProtocol):
    
    def __init__(self):
        self.isClient = False
        self.isReversed = True
        TCPProtocol.__init__(self)
        
def startTCPServer(reactor):
    factory = Factory()
    factory.protocol = TCPServer
    endpoint = TCP4ServerEndpoint(reactor, 3554)
    endpoint.listen(factory)
    return factory
   
