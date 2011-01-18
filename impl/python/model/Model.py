# -*- coding: utf-8 -*-

import hashlib
import base64
from network.Interface import getInetAddress
from exceptions import AssertionError
import appVar

class Model():
    """Model danych aplikacji: zalogowany uzytkownik, zegar logiczny etc"""
    
    def __init__(self):
        self.__nickname = None # login uzytkownka
        self.__myId = None  # uid wezla(uzytkownika)
        self.__nodes = []   # lista UID innych wezlow
        self.__nodeToNickMaping = {} # mapowanie z UID na nazwe uzytkownika
        self.__nodeToIPMapping = {} # mapowanie x UID na ip uzytkownika (nie dla kazdego UID!!! - tylko dla tych co wyslali HII)
        self.__logicalTime = [] # macierz czasów logicznych
        self.__preferredNodesAddr = None
        
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
        return self.__nodeToNickMaping[uid]
        
    def getIPByUID(self,  uid):
        return self.__nodeToIPMapping[uid]
    
    def setMyId(self, id):
        print "MODEL: ustawiam id na " + id
        self.__myId = id
        
    def getMyId(self):
        return self.__myId

    def getListOfNodes(self):
        return list(self.__nodes)
        
    def addNode(self,  uid,  username,  ip):
        #TODO: dodanie do macierzy zegarow
        assert(len(uid) == len(base64.b64encode(hashlib.sha1().digest())))
        assert(len(username) > 0)
        #assert(ip)
        if self.__nodes.count(uid) == 0:
            print "Model: dodaje nowy wezel do macierzy %s(%s)" % (uid,  username)
            self.__nodes.append(uid)
            self.__nodeToNickMaping[uid] = username
            self.__nodeToIPMapping[uid] = ip
            appVar.guiInstance.addNode(uid,  username)
        else:
            print "Model: juz znam wezel o id %s" % uid

    def removeNode(self,  uid):
        print "Model: usuwam wskazanego wezla - %s" % uid
        self.__nodes.remove(uid)
        del self.__nodeToIPMapping[uid]
        del self.__nodeToNickMaping[uid]
        appVar.guiInstance.delNode(uid)

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
        return len(self.__nodes) == 0
