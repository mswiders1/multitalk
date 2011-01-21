# -*- coding: utf-8 -*-

from exceptions import ValueError

class CacheElem:
    
    def __init__(self,  msgOrSender,  senderTime = None):
        self.__broadcastStarted = False
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
        self.msgList = set()
        
    def isMsgOnTheList(self,  msg):
        elem = CacheElem(msg)
        return elem in self.msgList
        
    def addMsg(self,  msg):
        elem = CacheElem(msg)
        if not self.isMsgOnTheList(msg):
            self.msgList.add(elem)
        else:
            raise ValueError("nie można dodać wiadomości: istniej już")
    
    def delMsg(self,  msg):
        elem = CacheElem(msg)
        if not self.isMsgOnTheList(elem):
            raise ValueError("nie można skasować wiadomości: wiadomość nie istnieje")
        self.msgList.remove(elem)
        
    def getMsg(self,  uid,  time):
        stub = CacheElem(uid,  timer)
        idx = self.msgList.index(stub)
        return  self.msgList[idx]

class MessageCache(BaseMessageCache):
    
    def storeMsg(self,  msg):
        self.addMsg(msg)
        self.logMsg("dodalem wiadomosc do cache-a od %s z timeVec=%s" % (msg['SENDER'],  msg['TIME_VEC']))
    
    def delMsgWithLowerOrEqTime(self,  senderUid,  time):
        for elem in set(self.msgList):
            if elem.getSender() == senderUid and elem.getSenderTime() <= time:
                self.logMsg("usuwam wiadomosc z cache-a: od %s time %d " % (senderUid,  elem.getSenderTime()))
                self.msgList.remove(elem)
                
    def delMsgBySender(self,  senderUid):
        for elem in set(self.msgList):
            if elem.getSender() == senderUid:
                self.msgList.remove(elem)
    
    def logMsg(self,  msg):
        print ("MsgCache: %s " %msg)
        
class DelayedMsgCache(BaseMessageCache):
    
    def delayMsg(self,  msg):
        self.addMsg(msg)
        
    def isAlreadyDelayed(self,  msg):
        return self.isMsgOnTheList(msg)
        
    def logMsg(self,  msg):
        print ("DelayedMsgCache: %s " %msg)
        
class MessageInfoStore:
    
    def __init__(self):
        self.__liveCounter = {}
        self.__logMsgUids = set()
        self.__outMsgUids = set()
        
    def __isMsgLogToForward(self,  msg):
        uid = msg['UID']
        try:
            self.__outMsgUids.remove(uid)
        except:
            pass
        if uid in self.__logMsgUids:
            return False
        else:
            self.__logMsgUids.add(uid)
            return True
    
    def __isMsgOutToForward(self,  msg):
        uid = msg['UID']
        try:
            self.__logMsgUids.remove(uid)
        except:
            pass
        if uid in self.__outMsgUids:
            return False
        else:
            self.__outMsgUids.add(uid)
            if self.__liveCounter.keys().count(uid):
                del self.__liveCounter[uid]
            return True
        
    def __isMsgLivToForward(self,  msg):
        uid = msg['UID']
        seq = int(msg['SEQUENCE'])
        if uid in self.__liveCounter:
            currentSeq = self.__liveCounter[uid]
            if currentSeq < seq:
                self.__liveCounter[uid] = seq
                return True
            else:
                return False
        else:
            self.__liveCounter[uid] = seq
            return False
        
    def isToForward(self,  msg):
        if msg['TYPE'] == 'LOG':
            return self.__isMsgLogToForward(msg)
        elif msg['TYPE'] == 'LIV':
            return self.__isMsgLivToForward(msg)
        elif msg['TYPE'] == 'OUT':
            return self.__isMsgOutToForward(msg)
        else:
            raise ValueError("Bledny typ wiadomosci")
        
    def logMsg(self,  msg):
        print "InfoStore: %s" % msg
        
