# -*- coding: utf-8 -*-
from twisted.internet.protocol import DatagramProtocol
from Interface import isMyAddr
from BroadcastSender import BroadcastSender
import appVar

class BroadcastReceiver(DatagramProtocol):
    def datagramReceived(self, data, (host, port)):
        print "BS: odebrano pakiet %r od %s:%d" % (data, host, port)
        if data == BroadcastSender.MSG:
            print "BS: odebrano prawidlowa wiadomosc od %s:%d" % (host, port)
            if not isMyAddr(host):
                #Odebralismy komunikat wiec informujemy kontroler
                appVar.coreInstance.handleReceivedBroadcastPacket(host)
            else:
                print u"BS: ignoruje rozgloszenie bo sam je wyslalem :)"
        else:
            print "BS: odebrano nieprawidlowa wiadomosc od %s:%d" % (host, port)

def startReceiver(reactor):
    broadcastReceiver = BroadcastReceiver()
    reactor.listenUDP(3554,  broadcastReceiver)
    return broadcastReceiver
