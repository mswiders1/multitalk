# -*- coding: utf-8 -*-
from twisted.internet.protocol import DatagramProtocol
from socket import SOL_SOCKET, SO_BROADCAST
import appVar

BROADCAST_COUNT = 3
BROADCAST_TIME_BETWEEN_IN_SEC = 1
BROADCAST_TIME_AFTER_IN_SEC =2

class BroadcastSender(DatagramProtocol):
    MSG = u'MULTITALK_5387132'
    
    def __init__(self, reactor,  port = 3554):
        self.__port = port
        self.__reactor = reactor
        
    def startProtocol(self):
        print "BC: start protokołu"
        self.transport.socket.setsockopt(SOL_SOCKET, SO_BROADCAST, True)
        self.sendDatagram()
        for delay in range(1,  BROADCAST_COUNT * BROADCAST_TIME_BETWEEN_IN_SEC):
            self.__reactor.callLater(delay,  self.sendDatagram)
    
    def sendDatagram(self):
        print "BC: wysyłam rozgłoszenie"
        self.transport.write(self.getPacket(), ("<broadcast>", self.__port))
        
    def getPacket(self):
        return  BroadcastSender.MSG

def startSender(reactor):
    broadcastSender = BroadcastSender(reactor)
    reactor.listenUDP(0, broadcastSender)
    __startTimers(reactor)
    return broadcastSender

def __startTimers(reactor):
    appVar.coreInstance.broadcastProgress(0)
    time = BROADCAST_COUNT * BROADCAST_TIME_BETWEEN_IN_SEC + BROADCAST_TIME_AFTER_IN_SEC
    for delay in range(1, time + 1):
        procentage = delay *  (100/time)
        reactor.callLater(delay,  _handleDiscoveryProgress,  procentage)
    
def _handleDiscoveryProgress(procentage):
    appVar.coreInstance.broadcastProgress(procentage)
    
