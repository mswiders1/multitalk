# -*- coding: utf-8 -*-

"""The user interface for our app"""

from network import BroadcastReceiver,  BroadcastSender,  TCPClient,  TCPServer,  MessageParser
import os,sys,traceback, time
import Queue
import socket
import exceptions
import queues
import Hash
from Heartbeat import Heartbeat

class Core:
    def __init__(self,  reactor):
        print "Core: swiat jest piekny ;)"
        self.__tcpm = None
        self.__model = None
        self.__gui = None
        self.reactor = reactor
        self.heartbeat = Heartbeat(self)
        return 
    
    def userInsertedNetworkAddr(self,  addr):
        print "Core: uzytkownik chce polaczyc sie z %s" % addr
        self.__model.setPreferredNodesAddr(addr)
        
    def handleUserInsertedNick(self,  nick):
            #Logowanie uzytkownika
            self.__model.setNick(nick)
            print ("Core: logowanie z nickiem %s" % nick)
            self.__model.setMyId(Hash.generateUserId(nick))
            try:
                print "Core: tworze serwer TCP"
                self.tcpFactory = TCPServer.startTCPServer(self.reactor)
                netAddr = self.__model.getPreferredNodesAddr()
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
    
    def handleHeartbeatTimePassed(self):
        print "Core: wysylam heartbeat(%s)" % time.strftime("%H:%S") 
        for connection in self.__tcpm.getMappedConnections():
            connection.sendLivMsg()
            
    def handleLivMessage(self,  msg):
        uid = msg['UID']
        self.__model.markNodeIsAlive(uid)

    def sendMessage(self,  uid,  msg):
        if not msg:
            return
        if uid:
            print u"Core: wysyłam wiadomość '%s' do %s" % (msg,  uid)
            self.__gui.messageReceived(self.__model.getMyId(), self.__model.getNick(), uid,  msg)
        else:
            print u"Core: wysyłam wiadomość '%s' do wszystkich" % msg
            self.__gui.messageReceived(self.__model.getMyId(), self.__model.getNick(),  None,  msg)
            
        for connection in self.__tcpm.getMappedConnections():
            #ale lipa - musze z tego poziomu tworzyc pakiet do wyslania    
            connection.sendPacket(MessageParser.getFullMsgMsg(uid,  msg))
        
    def handleMsgMessage(self,  msg):
        print u"Core: przesylam wiadomosc MSG do analizujy "
        self.__model.updateLogicalTimeUsingMsgAndSendToGui(msg)
    
    def userNameByUid(self,  uid):
        return self.__model.getNickByUID(uid)
        
    def isThisMyUid(self,  uid):
        return uid == self.__model.getMyId()
    
    def setDelayPerNode(self,  uid,  delayInSec):
        print u"Core: ustawiam opóźnienie %d sekund dla klienta %s" % (delayInSec,  uid)
            
    def handleHiiMessage(self,  msg,  connection):
        print "Core: analiza wiadomosci Hii"
        for nodeFromVector in msg['VECTOR']:
            print "Core: dodaje wezel z wiadomosci hi: '%s'" % nodeFromVector
            self.__model.addNode(nodeFromVector['UID'],  nodeFromVector['USERNAME'],  nodeFromVector['IP_ADDRESS'])
        print "Core: mapuje wezel %s na polaczenie %s" % (msg['UID'],  connection)
        self.__tcpm.mapNodeToConnection(msg['UID'],  connection)
        if self.__model.getPreferredNodesAddr():
            #uzytkownik podal z palca adres wiec w opoiedzi na HII wysylamy LOG
            self.__doLog()
            
    def __doLog(self):
        self.__model.addMeToListOfNodes()
        self.__sendLogMsgToAll()
        self.heartbeat.start()
    
    def __doNewNetwork(self):
        self.__model.setIamFirstNode()
        self.heartbeat.start()
    
    def handleLogMessage(self,  msg,  connection):
        print "Core: analiza wiadomosci Log"
        if self.__model.logNewUser(msg['UID'],  msg['USERNAME'], msg['IP_ADDRESS']):
            self.__tcpm.mapNodeToConnection(msg['UID'],  connection) # TODO : co w przypadku gdy connection sluzylo jako proxy dla tej wiadomoscis
            connection.sendMtxMsg()
            print "Core: przesłałem MTX"
            return True
        else:
            return False

    def handleMtxMessage(self,  msg):
        print "Core: analiza wiadomosci MTX"
        self.__model.addMatrix(msg['MAC'],  msg['VEC'])

    def handleOutMessage(self,  msg):
        print "Core: ktos sie żegna z nami :("
        uid = msg["UID"]
        self.__model.removeNode(uid)

    def closeApp(self):
        print "Core: zamykam applikacje"
        self.heartbeat.stop()
        self.__sendOutMsgToAll()
        return True
        
    def __sendOutMsgToAll(self):
        for connection in self.__tcpm.getMappedConnections():
            connection.sendOutMsgAndCloseConnection()
        
    def __handleBroadcastEnd(self):
        #Koniec przeszukiwania
        print "Core: koniec przeszukiwanie sieci"
        if self.__model.isIamAlone():
            print "Core: jestem sam :("
            self.__doNewNetwork()
        else:
            print "Core: znalezniono wezly - rozpoczynam logowanie"
            self.__doLog()
        #Wlączamy server broadcast aby otrzymywav informacje o koniecznosci podlaczenia
        print "Core: uruchamiam broadcast receiver"
        self.broadcastReceiver = BroadcastReceiver.startReceiver(self.reactor)
        
        
    def __sendLogMsgToAll(self):
        assert not self.__tcpm.getUnmappedConnections(),  "w chwili wyslania log msg nie moze byc niezmapowane polaczenie %s" % self.__tcpm.getUnmappedConnections()
        for connection in self.__tcpm.getMappedConnections():
            connection.sendLogMsg()
        
    def broadcastProgress(self,  progress):
        self.__gui.setBroadcastProgress(progress)
        if progress == 100:
            self.__handleBroadcastEnd()
                
    def handleReceivedBroadcastPacket(self,  fromIP):
        print "Core: ktos chce abysmy podlaczyli sie do niego"
        if self.__tcpm.isNotConnectedToIp(fromIP):
            print "Core: nie mam do niego polaczenie wiec tworze je %s" % fromIP
            TCPClient.startTCPConnection(self.reactor,  fromIP)
    
    def setGui(self,  gui):
        self.__gui = gui

    def setModel(self,  model):
        self.__model = model
        
    def setTcpManager(self,  manager):
        self.__tcpm = manager
