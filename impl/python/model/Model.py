# -*- coding: utf-8 -*-

import hashlib
import base64
from network.Interface import getInetAddress
from exceptions import AssertionError
import appVar,  time

IS_ALIVE_TIME = 20

class Node():
    
    def __init__(self,  uid):
        self.__name = None
        self.__uid = uid
        self.__ip = None
        self.__lastSeen = None

    def getUid(self):
        return self.__uid

    def setName(self,  name):
        self.__name = name
        
    def getName(self):
        return self.__name
        
    def setIp(self,  ip):
        self.__ip = ip
        
    def getIp(self):
        return self.__ip
        
    def markIsAlive(self):
        self.__lastSeen = time.time()
        
    def isAlive(self):
        timeDiff = time.time() - self.__lastSeen #TODO: to nie jest bezpieczne (ten czas nie jest monotoniczny), ale nie chcę dodawać innych bibliotek
        return timeDiff < IS_ALIVE_TIME

class Model():
    """Model danych aplikacji: zalogowany uzytkownik, zegar logiczny etc"""
    
    def __init__(self):
        self.__nickname = None # login uzytkownka
        self.__myId = None  # uid wezla(uzytkownika)
        self.__preferredNodesAddr = None # adres wezla podany przez uzytkownika abysmy podlaczyli sie do niego
        
        self.__nodes = {}   # mapa uid -> obiekt Node 
        #self.__nodeToNickMaping = {} # mapowanie z UID na nazwe uzytkownika
        #self.__nodeToIPMapping = {} # mapowanie x UID na ip uzytkownika (nie dla kazdego UID!!! - tylko dla tych co wyslali HII)
        self.__logicalTime = [] # macierz czasów logicznych
        
        
    def __getLogicalTimeForNode(self,  uid):
        assert(False)
        #TODO : do roboty
        return 
        
    def setPreferredNodesAddr(self,  address):
        self.__preferredNodesAddr = address
        
    def getPreferredNodesAddr(self):
        return self.__preferredNodesAddr
        
    def setNick(self,  nick):
        print "MODEL: ustawiam nick na " + nick
        self.__nickname = nick
    
    def getNick(self):
        return self.__nickname
        
    def getNickByUID(self,  uid):
        return self.__nodes[uid].getName()
        
    def getIPByUID(self,  uid):
        return self.__nodes[uid].getIp()
    
    def setMyId(self, id):
        print "MODEL: ustawiam id na " + id
        self.__myId = id
    
    def getMyIP(self):
        return getInetAddress()
    
    def getMyId(self):
        return self.__myId

    def getListOfNodes(self):
        return list(self.__nodes.keys())
        
    def addNode(self,  uid,  username,  ip):
        #TODO: dodanie do macierzy zegarow
        assert(len(uid) == len(base64.b64encode(hashlib.sha1().digest())))
        assert(len(username) > 0)
        #assert(ip)
        if self.__nodes.keys().count(uid) == 0:
            print "Model: dodaje nowy wezel do macierzy %s(%s)" % (uid,  username)
            node = Node(uid)
            node.setName(username)
            node.setIp(ip)
            self.__nodes[uid] = node
            appVar.guiInstance.addNode(uid,  username)
        else:
            print "Model: juz znam wezel o id %s" % uid

    def removeNode(self,  uid):
        print "Model: usuwam wskazanego wezla - %s" % uid
        del self.__nodes[uid]
        appVar.guiInstance.delNode(uid)
        
    def markNodeIsAlive(self,  uid):
        self.__nodes[uid].markIsAlive()

    def setIamFirstNode(self):
        self.addNode(self.getMyId(),  self.getNick(),  getInetAddress())

    def addMeToListOfNodes(self):
        assert len(self.__nodes) > 0,  "nie moge sie dodac do listy wezlow bo jest ona pusta '%s'" % self.__nodes
        self.addNode(self.getMyId(),  self.getNick(),  getInetAddress())
        
    def logNewUser(self,  uid,  username,  ip):
        print "Model: logowanie uzytkownika %s = %s (%s)" % (username,  uid,  ip)
        try:
            self.addNode(uid,  username,  ip)
            return True
        except AssertionError as err:
            print "Model: nie mozna zalogowac uzytkownika: %s" % err
            return False
        
    def isIamAlone(self):
        return len(self.__nodes.values()) == 0
