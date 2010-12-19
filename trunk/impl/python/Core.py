# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys
import multicast
from PyQt4 import QtCore

class Core(QtCore.QObject):
    
    def __iniit__(self):
        return 
        
    def connect(self,  config):
        self.addr = config["address"]
        self.port = config["port"]
        self.delay = config["delay"]
        self.__doConnect()
        
    def broadcastForNodes(self):
        """Preszukuje siec w poszukiwaniu węzłów"""
        
    def __doConnect(self):
        interface = "0.0.0.0"
        datagramSourceAddr = "0.0.0.0"
        self.writer = multicast.DatagramSender(datagramSourceAddr,  self.port,  self.addr,  self.port)
        self.reader = multicast.MulticastUDPReceiver(interface,  self.addr,  self.port)
        self.emit(QtCore.SIGNAL("newMessage()"))
        
    def sendMessage(self,  text):
        if self.writer:
            #TODO: send valid packet
            self.writer.write(text)
        else:
            #TODO: throw exception
            return
        
    def receiveMessage(self):
        return None
