# -*- coding: utf-8 -*-
from twisted.internet.protocol import DatagramProtocol
from socket import SOL_SOCKET, SO_BROADCAST
import appVar

class BroadcastSender(DatagramProtocol):
    MSG = u'MULTITALK_5387132'
    
    def __init__(self, port = 3554):
        self.port = port
        
    def startProtocol(self):
        print "BC: start protokołu"
        self.transport.socket.setsockopt(SOL_SOCKET, SO_BROADCAST, True)
        self.sendDatagram()
    
    def sendDatagram(self):
        print "BC: wysyłam rozgłoszenie"
        self.transport.write(self.getPacket(), ("<broadcast>", self.port))
        
    def getPacket(self):
        return  BroadcastSender.MSG

def startSender(reactor):
    broadcastSender = BroadcastSender()
    reactor.listenUDP(0, broadcastSender)
    __startTimers(reactor)
    return broadcastSender

def __startTimers(reactor):
    appVar.coreInstance.broadcastProgress(0)
    for delay in range(1, 3):
        procentage = delay * 50
        reactor.callLater(delay,  _handleDiscoveryProgress,  procentage)
    
def _handleDiscoveryProgress(procentage):
    appVar.coreInstance.broadcastProgress(procentage)
    
