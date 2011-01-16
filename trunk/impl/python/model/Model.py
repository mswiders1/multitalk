# -*- coding: utf-8 -*-

import hashlib
import base64
from network.Interface import getInetAddress

class Model():
    """Model danych aplikacji: zalogowany uzytkownik, zegar logiczny etc"""
    
    def __init__(self):
        self.__nickname = None # login uzytkownka
        self.__myId = None  # uid wezla(uzytkownika)
        self.__nodes = []   # lista UID innych wezlow
        self.__nodeToNickMaping = {} # mapowanie z UID na nazwe uzytkownika
        self.__nodeToIPMapping = {} # mapowanie x UID na ip uzytkownika (nie dla kazdego UID!!! - tylko dla tych co wyslali HII)
        self.__logicalTime = []
        
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
        assert(ip)
        print "MODEL: dodaje nowy wezel do macierzy %s(%s)" % (uid,  username)
        self.__nodes.append(uid)
        self.__nodeToNickMaping[uid] = username
        self.__nodeToIPMapping[uid] = ip

    def setIamFirstNode(self):
        self.addNode(self.getMyId(),  self.getNick(),  getInetAddress())

    def isIamAlone(self):
        return len(self.__nodes) == 0
