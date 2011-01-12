# -*- coding: utf-8 -*-

import socket, traceback
import Json
import threading
import SocketServer
import queues

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

def doDiscovery():
    print("%s: doing discovery" % __name__)
    dest = ('<broadcast>',3554)
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    #s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.sendto(Json.getDiscoveryMsg(),  dest)
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
    #s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.bind(("0.0.0.0",  0))
    while 1:
        try:
            print "Waiting for broadcast msg"
            message = {}
            message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.BROADCAST_REQUEST_RECEIVED
            message['HOST'] = "192.168.0.1"
            message['PORT'] = 3445
            queues.coreQueue.put(message)
            data, address = s.recvfrom(1024)
            print "Got data from", address
            #TODO: przeslac dane do Core
        except (KeyboardInterrupt, SystemExit):
            raise
        except:
            traceback.print_exc()

def startServer():
    threading.Thread(target=serve_thread).start()
    print "Server loop running"
