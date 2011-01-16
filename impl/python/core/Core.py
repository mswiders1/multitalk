# -*- coding: utf-8 -*-
# -*- coding: utf-8 -*-

"""The user interface for our app"""

from network import BroadcastReceiver,  BroadcastSender,  TCPClient,  TCPServer
import os,sys,traceback
import Queue
import socket
import exceptions
import queues
import Hash
import appVar

class Core:
    def __init__(self,  reactor):
        self.model = appVar.modelInstance
        self.reactor = reactor
        return 
        
    def run(self):
        self.tcpServer = None
        while 1:
            queueElem = queues.coreQueue.get()
            insertElem = {}
            #print("Core: parsing queue element %s" % queueElem)
            
            if queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_BEGIN:
                #Rozpoczecie przeszukiwania
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_WIN_SHOW
                queues.guiQueue.put(insertElem)
                
            elif queueElem['CORE_MSG_TYPE'] == queues.CORE_MSG_TYPE.BROADCAST_PROGRESS:
                #Przeszukiwanie w trakcie
                insertElem['GUI_MSG_TYPE'] = queues.GUI_MSG_TYPE.BROADCAST_PROGRESS
                insertElem['PROGRESS'] = queueElem['PROGRESS']
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
    
    def handleUserInsertedNick(self,  nick):
            #Logowanie uzytkownika
            self.model.setNick(nick)
            print ("Core: logowanie z nickiem %s" % nick)
            self.model.setMyId(Hash.generateUserId(nick))
            try:
                print "Core: tworze serwer TCP"
                self.tcpFactory = TCPServer.startTCPServer(self.reactor)
                
                print "Core: tworze klienta broadcast"
                self.broadcastSender = BroadcastSender.startSender(self.reactor)
            except socket.error as err:
                print("Core: nie można uruchomić zerwer TCP lub wyslac rozgloszenia")
                traceback.print_exc()
                sys.exit()
            
    def handleHiiMessage(self,  msg):
        print "Core: analiza wiadomosci Hii"
        for nodeFromVector in msg['VECTOR']:
            model.addNode(msg['UID'],  msg['USERNAME'],  msg['IP_ADDRESS'])

    def handleLogMessage(self,  msg):
        print "Core: analiza wiadomosci Log"
        #TODO

    def closeApp(self):
        print "Core: zamykam applikacje"
        return True
    
    def broadcastProgress(self,  progress):
        appVar.guiInstance.setBroadcastProgress(progress)
        if progress == 100:
            #Koniec przeszukiwania
            print "Core: koniec przeszukiwanie sieci"
            #TODO: teraz uzytkownik powinien podac IP lub stwierdzic ze jest pierwszy
            if self.model.isIamAlone():
                print "Core: jestem sam :("
                self.model.setIamFirstNode()
            #Wlączamy server broadcast aby otrzymywav informacje o koniecznosci podlaczenia
            print "Core: uruchamiam broadcast receiver"
            self.broadcastReceiver = BroadcastReceiver.startReceiver(self.reactor)
                
    def handleReceivedBroadcastPacket(self,  fromIP):
        print "Core: ktos chce abysmy podlaczyli sie do niego"
        TCPClient.startTCPConnection(self.reactor,  fromIP)
    
    def setGui(self,  gui):
        self.gui = gui
