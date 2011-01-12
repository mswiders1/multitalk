# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys
import multicast
import Queue
import threading
import queues
from network import Broadcast,  TCP
from PyQt4 import QtCore

class Core(threading.Thread):
    
    def __init__(self):
        threading.Thread.__init__(self)
        return 
        
    def run(self):
        TCP.startTcpServer()
        Broadcast.startServer()
        Broadcast.doDiscovery()
        while 1:
            queueElem = queues.coreQueue.get()
            insertElem = {}
            print("Core: parsing queue element %s" % queueElem)
            
            if queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_BEGIN:
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_WIN_SHOW
                queues.guiQueue.put(insertElem)
                
            if queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_PROGRESS:
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_PROGRESS
                insertElem['PROGRESS'] = queueElem['PROGRESS']
                queues.guiQueue.put(insertElem)
                
            if queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_END:
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_WIN_CLOSE
                queues.guiQueue.put(insertElem)
                
            if queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_REQUEST_RECEIVED:
                TCP.connectToTcpServer(queueElem['HOST'],  queueElem['PORT'])
                
                
        
    def __connect(self,  config):
        self.addr = config["address"]
        self.port = config["port"]
        self.delay = config["delay"]
        self.__doConnect()
        
    def __doConnect(self):
        interface = "0.0.0.0"
        datagramSourceAddr = "0.0.0.0"
        self.writer = multicast.DatagramSender(datagramSourceAddr,  self.port,  self.addr,  self.port)
        self.reader = multicast.MulticastUDPReceiver(interface,  self.addr,  self.port)
        self.emit(QtCore.SIGNAL("newMessage()"))
        
