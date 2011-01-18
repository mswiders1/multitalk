# -*- coding: utf-8 -*-

"""The user interface for our app"""

from network import BroadcastReceiver,  BroadcastSender,  TCPClient,  TCPServer,  TCPManager
import os,sys,traceback
import Queue
import socket
import exceptions
import queues
import Hash
import appVar

class Core:
    def __init__(self,  reactor):
        print "Core: swiat jest piekny ;)"
        appVar.tcpManager = TCPManager.TCPManager()
        self.tcpm = appVar.tcpManager
        self.model = appVar.modelInstance
        self.reactor = reactor
        return 
    
    def userInsertedNetworkAddr(self,  addr):
        print "Core: uzytkownik chce polaczyc sie z %s" % addr
        self.model.setPreferredNodesAddr(addr)
        
    def handleUserInsertedNick(self,  nick):
            #Logowanie uzytkownika
            self.model.setNick(nick)
            print ("Core: logowanie z nickiem %s" % nick)
            self.model.setMyId(Hash.generateUserId(nick))
            try:
                print "Core: tworze serwer TCP"
                self.tcpFactory = TCPServer.startTCPServer(self.reactor)
                netAddr = self.model.getPreferredNodesAddr()
                if netAddr:
                    print "Core: tworze klienta tcp do polaczenia do %s" % netAddr
                    TCPClient.startReversedTCPConnection(self.reactor,  netAddr)
                else:
                    print "Core: tworze klienta broadcast"
                    self.broadcastSender = BroadcastSender.startSender(self.reactor)
            except socket.error as err:
                print("Core: nie można uruchomić zerwer TCP lub wyslac rozgloszenia")
                traceback.print_exc()
                sys.exit()
            
    def sendMessage(self,  uid,  msg):
        if not msg:
            return
        if uid:
            print u"Core: wysyłam wiadomość '%s' do %s" % (msg,  uid)
            self.gui.messageReceived(self.model.getMyId(),  uid,  msg)#do testow
        else:
            print u"Core: wysyłam wiadomość '%s' do wszystkich" % msg#do testow
            self.gui.messageReceived(self.model.getMyId(),  None,  msg)
        #TODO: wysylka
    
    def userNameByUid(self,  uid):
        return self.model.getNickByUID(uid)
        
    def isThisMyUid(self,  uid):
        return uid == self.model.getMyId()
    
    def setDelayPerNode(self,  uid,  delayInSec):
        print u"Core: ustawiam opóźnienie %d sekund dla klienta %s" % (delayInSec,  uid)
            
    def handleHiiMessage(self,  msg,  connection):
        print "Core: analiza wiadomosci Hii"
        for nodeFromVector in msg['VECTOR']:
            print "Core: dodaje wezel z wiadomosci hi: '%s'" % nodeFromVector
            self.model.addNode(nodeFromVector['UID'],  nodeFromVector['USERNAME'],  nodeFromVector['IP_ADDRESS'])
        print "Core: mapuje wezel %s na polaczenie %s" % (msg['UID'],  connection)
        self.tcpm.mapNodeToConnection(msg['UID'],  connection)

    def handleLogMessage(self,  msg,  connection):
        print "Core: analiza wiadomosci Log"
        self.model.logNewUser(msg['UID'],  msg['USERNAME'], None) # TODO: jaki adres ip wstawic?
        self.tcpm.mapNodeToConnection(msg['UID'],  connection) # TODO : co w przypadku gdy connection sluzylo jako proxy dla tej wiadomoscis
        return True

    def handleOutMessage(self,  msg):
        print "Core: ktos sie żegna z nami :("
        uid = msg["UID"]
        self.model.removeNode(uid)

    def closeApp(self):
        print "Core: zamykam applikacje"
        self.__sendOutMsgToAll()
        return True
        
    def __sendOutMsgToAll(self):
        for connection in self.tcpm.getMappedConnections():
            connection.sendOutMsgAndCloseConnection()
        
    def __handleBroadcastEnd(self):
        #Koniec przeszukiwania
        print "Core: koniec przeszukiwanie sieci"
        if self.model.isIamAlone():
            print "Core: jestem sam :("
            self.model.setIamFirstNode()
        else:
            print "Core: znalezniono wezly - rozpoczynam logowanie"
            self.model.addMeToListOfNodes()
            self.__sendLogMsgToAll()
        #Wlączamy server broadcast aby otrzymywav informacje o koniecznosci podlaczenia
        print "Core: uruchamiam broadcast receiver"
        self.broadcastReceiver = BroadcastReceiver.startReceiver(self.reactor)
        
    def __sendLogMsgToAll(self):
        assert not self.tcpm.getUnmappedConnections(),  "w chwili wyslania log msg nie moze byc niezmapowane polaczenie %s" % self.tcpm.getUnmappedConnections()
        for connection in self.tcpm.getMappedConnections():
            connection.sendLogMsg()
        
    def broadcastProgress(self,  progress):
        appVar.guiInstance.setBroadcastProgress(progress)
        if progress == 100:
            self.__handleBroadcastEnd()
                
    def handleReceivedBroadcastPacket(self,  fromIP):
        print "Core: ktos chce abysmy podlaczyli sie do niego"
        TCPClient.startTCPConnection(self.reactor,  fromIP)
    
    def setGui(self,  gui):
        self.gui = gui
