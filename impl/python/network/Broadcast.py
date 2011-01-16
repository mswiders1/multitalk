# -*- coding: utf-8 -*-

import socket, traceback
import Json
import threading
import SocketServer
import queues
from network.Interface import *
from network.MessageParser import *

UDP_PORT = 3554
DISCOVERY_TIMEOUT_IN_SEC = 10
DISCOVERY_PROGRESS = -1

def _handleDiscoveryTimeout():
    print("Stoping discovery.")
    message = {}
    message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.BROADCAST_END
    queues.coreQueue.put(message)

progressSemaphore = threading.BoundedSemaphore(value=1)

def _handleDiscoveryProgress():
    global DISCOVERY_PROGRESS
    global progressSemaphore
    
    progressSemaphore.acquire()
    DISCOVERY_PROGRESS = DISCOVERY_PROGRESS + 10
    tmpProgress = DISCOVERY_PROGRESS
    progressSemaphore.release()
    
    message = {}
    message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.BROADCAST_PROGRESS
    message['PROGRESS'] = tmpProgress;
    queues.coreQueue.put(message)
    if tmpProgress == 100:
        _handleDiscoveryTimeout()
    

def _startTimers(sequence):
    timerProgress = threading.Timer(sequence * DISCOVERY_TIMEOUT_IN_SEC/10,  _handleDiscoveryProgress)
    timerProgress.start()

def doDiscovery(host=None):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    if not host:
        host = '<broadcast>'
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    dest = (host,UDP_PORT)
    s.bind(('',  0))
    print u"UDP: rozsyłamy zgłoszenie na ",  dest
    s.sendto(DISCOVERY_MSG,  dest)
    global DISCOVERY_PROGRESS
    DISCOVERY_PROGRESS = 0
    message = {}
    message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.BROADCAST_BEGIN
    queues.coreQueue.put(message)
    for seq in range(1, 11):
        _startTimers(seq)
    
class MyUDPHandler(SocketServer.BaseRequestHandler):
    """Obsluguje zadanie"""
    def handle(self):
        data = self.request.recv(1024)
        cur_thread = threading.currentThread()
        response = "%s %s: %s" % (__name__,  cur_thread.getName(), data)
        print(response)

def serve_thread():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    bindAddr = ('<broadcast>',  UDP_PORT)
    s.bind(bindAddr)
    while 1:
        try:
            print u"UDP: czekamy na broadcast", bindAddr 
            data, (address,  port) = s.recvfrom(8192)
            print u"UDP: odczytano broadcast z ", address
            if data != DISCOVERY_MSG:
                print u"UDP: Odebrane rozgloszenie ma inna zawartosc: ",  data
                break
            if not isMyAddr(address):
                #Odebralismy komunikat wiec informujemy kontroler
                message = {}
                message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.BROADCAST_RECEIVED
                message['FROM'] = address
                queues.coreQueue.put(message)
            else:
                print u"UDP: ignoruje rozgloszenie bo sam je wyslalem :)"
        except (KeyboardInterrupt, SystemExit):
            raise
        except:
            traceback.print_exc()

def startServer():
    threading.Thread(target=serve_thread).start()
    print "UDP: Uruchomiono serwer UDP w oddzielnym watku"
