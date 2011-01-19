# -*- coding: utf-8 -*-

class CacheElem:
    
    def __init__(self,  msgOrSender,  senderTime = None):
        if senderTime:
            self.__sender = msgOrSender
            self.__logicalTimeOfSender = senderTime
            self.__msg = None        
        else:
            self.__sender = msgOrSender['SENDER']
            idx = msgOrSender['VEC'].index(self.__sender)
            self.__logicalTimeOfSender = msgOrSender['TIME_VEC'][idx]
            self.__msg = msgOrSender


    def getSender(self):
        return self.__sender
        
    def getSenderTime(self):
        return self.__logicalTimeOfSender

    def __eq__(self,  other):
        return self.__sender == other.__sender and self.__logicalTimeOfSender == other.__logicalTimeOfSender
    
    def __hash__(self):
        return self.__sender.__hash__() + self.__logicalTimeOfSender.__hash__()
    
class BaseMessageCache:
    
    def __init__(self):
        self.__msgList = set()
        
    def isMsgOnTheList(self,  msg):
        elem = CacheElem(msg)
        return elem in self.__msgList
        
    def addMsg(self,  msg):
        elem = CacheElem(msg)
        if not self.isMsgOnTheList(msg):
            self.__msgList.add(elem)
        else:
            raise ValueError("nie można dodać wiadomości: istniej już")
    
    def delMsg(self,  msg):
        elem = CacheElem(msg)
        if not self.isMsgOnTheList(elem):
            raise ValueError("nie można skasować wiadomości: wiadomość nie istnieje")
        self.__msgList.remove(elem)
        
    def getMsg(self,  uid,  time):
        stub = CacheElem(uid,  timer)
        idx = self.__msgList.index(stub)
        return  self.__msgList[idx]

class MessageCache(BaseMessageCache):
    
    def storeMsg(self,  msg):
        self.addMsg(msg)
        self.logMsg("dodalem wiadomosc do cache-a od %s z timeVec=%s" % (msg['SENDER'],  msg['TIME_VEC']))
    
    def delMsgWithLowerOrEqTime(self,  senderUid,  time):
        for elem in self.__msgList:
            if elem.getSender() == senderUid and elem.getSenderTime() <= time:
                self.__msgList.remove(elem)
    
    def logMsg(self,  msg):
        print ("MsgCache: %s " %msg)
        
class DelayedMsgCache(BaseMessageCache):
    
    def delayMsg(self,  msg):
        self.addMsg(msg)
        
    def isAlreadyDelayed(self,  msg):
        return self.isMsgOnTheList(msg)
        
    def logMsg(self,  msg):
        print ("DelayedMsgCache: %s " %msg)
        
    
