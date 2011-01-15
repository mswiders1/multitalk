# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys,traceback
import Queue
import socket
from model.Model import Model
import threading
import exceptions
import queues
from network import Broadcast,  TCP
from PyQt4 import QtCore
import Hash

model = None

class Core(threading.Thread):
    
    def __init__(self):
        threading.Thread.__init__(self)
        return 
        
    def run(self):
        global model 
        model = Model()
        self.tcpServer = None
        Broadcast.startServer()
        while 1:
            queueElem = queues.coreQueue.get()
            insertElem = {}
            #print("Core: parsing queue element %s" % queueElem)
            
            if queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_BEGIN:
                #Rozpoczecie przeszukiwania
                print "Core: rozpoczynam przeszukiwanie sieci"
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_WIN_SHOW
                queues.guiQueue.put(insertElem)
                
            elif queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_PROGRESS:
                #Przeszukiwanie w trakcie
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_PROGRESS
                insertElem['PROGRESS'] = queueElem['PROGRESS']
                queues.guiQueue.put(insertElem)
                
            elif queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_END:
                #Koniec przeszukiwania
                print "Core: koniec przeszukiwanie sieci"
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_WIN_CLOSE
                queues.guiQueue.put(insertElem)
                
            elif queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_RECEIVED:
                #Ktos chce abysmy sie do niego podlaczyli
                print "Core: ktos chce abysmy podlaczyli sie do niego"
                TCP.connectToTcpServer(queueElem['FROM'])
            
            elif queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.USER_LOGIN:
                #Uzytkownik zamknal okno z nickiem
                self.handleUserInsertedNick(queueElem)
                
            elif queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.CLOSE_APP_REQ:
                #uzytkownik chce zamknac aplikacje
                print("Core: zamykamy aplikacje")
                self.tcpServer.stop()
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.CLOSE_APP
                queues.guiQueue.put(insertElem)
                sys.exit()
            else:
                #Nieobslugiwany typ wiadomosci
                assert(False)
    
    def handleUserInsertedNick(self,  queueElem):
        if queueElem['STATUS']:
            #Logowanie uzytkownika
            nick = queueElem['NICK']
            model.setNick(nick)
            print ("Core: logowanie z nickiem %s" % nick)
            model.setMyId(Hash.generateUserId(nick))
            try:
                self.tcpServer = TCP.TCPServer()
                self.tcpServer.startTcpServer()
                Broadcast.doDiscovery()
            except socket.error as err:
                print("Core: nie można uruchomić zerwer TCP lub wyslac rozgloszenia")
                traceback.print_exc()
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.CLOSE_APP
                queues.guiQueue.put(insertElem)
                sys.exit()
        else:
            #Anulowal logowanie
            print("Core: brak loginu wiec konczymy")
            insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.CLOSE_APP
            queues.guiQueue.put(insertElem)
            sys.exit()

