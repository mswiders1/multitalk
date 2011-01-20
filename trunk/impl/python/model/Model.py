# -*- coding: utf-8 -*-

import hashlib
import base64
from network.Interface import getInetAddress
from exceptions import AssertionError
import time
from MessageCache import MessageCache,  DelayedMsgCache

IS_ALIVE_TIME = 20

class Node():
    
    def __init__(self,  uid):
        self.__name = None
        self.__uid = uid
        self.__ip = None
        self.__lastSeen = None
        self.__gui = None
        
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
        self.__nodesUid = [] # lista uid aby zachowac kolejnosc
        self.__logicalTime = [] # macierz czasów logicznych
        self.__msgCache = MessageCache()
        self.__delayedMsgCache = DelayedMsgCache()
        
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
        return list(self.__nodesUid)
        
    def getMatrix(self):
        return self.__logicalTime; #TODO: zwracać kopię?
        
    def getIncrementedTimeVector(self):
        uid = self.getMyId()
        index = self.__nodesUid.index(uid)
        vc = list(self.__logicalTime[index])
        currentValue = vc[index]
        newValue = currentValue + 1 # inkrementujemy nasz zegar logiczny
        print "Model: nowa wartosc zegara logicznego %d %s" % (newValue,  vc)
        self.__logicalTime[index][index] = newValue
        self.__printMatrix(self.__logicalTime,  self.__nodesUid)
        return vc # ... a do tagowanai wiadomosci dajemy wektor taki jaki byl przed inkrementacja
        
    def __printMatrix(self,  matrix,  vector):
        str = u""
        counter = 0
        for node in vector:
            str += unicode(counter) + u":" + node +u" "
            counter += 1
        print str
        
        for row in matrix:
            str = u""
            for cell in row:
                str += unicode(cell) + u" "
            print str
        
    def addMatrix(self,  matrix,  vector):
        print "Wejscie"
        self.__printMatrix(matrix,  vector)
        self.__printMatrix(self.__logicalTime,  self.__nodesUid)
        
        newNodes = set(self.__nodesUid).difference(vector)
        if newNodes:
            print "Model: jakies nowe wezly sa w macierzy: %s" % newNodes
        commonNodes = set(self.__nodesUid).intersection(vector)
        
        for uid in commonNodes:
            myRowIndex = self.__nodesUid.index(uid)
            matRowIndex = vector.index(uid)
            for uid2 in commonNodes:
                myColIndex = self.__nodesUid.index(uid2)
                matColIndex = vector.index(uid2)
                self.__logicalTime[myRowIndex][myColIndex] = max(self.__logicalTime[myRowIndex][myColIndex],  matrix[matRowIndex][matColIndex])
            
        print "Wynik"
        self.__printMatrix(self.__logicalTime,  self.__nodesUid)
    
    def __mustDelayMsg(self,  msg):
        senderUid = msg['SENDER']
        senderIdxInLogicalTime = self.__nodesUid.index(senderUid)# rzuci bledem jak nie bede znal tego UID-a
        senderIdxInMsg = msg['VEC'].index(senderUid)
        myIndex = self.__nodesUid.index(self.getMyId())
        commonUids = set(self.__nodesUid).intersection(msg['VEC'])
        
        self.logMsg("przepisuje wektor macierzy czasu nadawcy")
        for uid in commonUids:
            uidIndexInMsg = msg['VEC'].index(uid)
            timeInMsg = msg['TIME_VEC'][uidIndexInMsg]
            uidIndexInLogicalTimeMatrix = self.__nodesUid.index(uid)
            currentValue = self.__logicalTime[senderIdxInLogicalTime][uidIndexInLogicalTimeMatrix]
            isSender = 0
            if uid == senderUid:
                isSender = 1
            self.__logicalTime[senderIdxInLogicalTime][uidIndexInLogicalTimeMatrix] = max(currentValue, timeInMsg + isSender)
                
        for uid in commonUids:
            uidIndexInMsg = msg['VEC'].index(uid)
            uidIndexInLogicalTimeMatrix = self.__nodesUid.index(uid)
            timeInMsg = msg['TIME_VEC'][uidIndexInMsg]
            timeInMatrix = self.__logicalTime[myIndex][uidIndexInLogicalTimeMatrix]
            if timeInMsg > timeInMatrix: 
                print("Model: odkładamy wiadomość - nie dostaliśmy wcześniejszej wiadomości :( (oczekujemy na %d a dostalismy %d)" %(timeInMatrix,  timeInMsg))
                return True
        
        
        return False
        
    def updateLogicalTimeUsingMsgAndSendToGui(self,  msg):
        """
        True - trzeba forwardowac wiadomosc
        False - mielismy te wiadomosc wiec mowimy ze nie trzeba forwardowac
        """
        senderUid = msg['SENDER']
        receiverUid = msg['RECEIVER']
        content = msg['CONTENT']
        
        if senderUid == self.getMyId():
            #to maja wiadomosc wiec ignoruje 
            return False
        
        if self.__mustDelayMsg(msg):
            # czekamy na inna wiadomosc
            #TODO: ile mamy czekac? jakis timer trzeba ustawic i kogos poprosic o ponowne przeslanie brakujacej wiadomosci
            self.logMsg("trzeba opóźnić wiadomość")
            if self.__delayedMsgCache.isAlreadyDelayed(msg):
                self.logMsg("miałem już taką wiadomość więc pomijam ją")
                return False # nie przekazujemy dalej
            else:
                self.logMsg("opóźniam wiadomość")
                self.__printMatrix(self.__logicalTime,  self.__nodesUid)
                self.__delayedMsgCache.delayMsg(msg)
                return True
        else:
            # sprawdzamy czy wiadomość nie była opóżniana
            self.__printMatrix(self.__logicalTime,  self.__nodesUid)
            self.logMsg("odebralem wiadomosc od %s wiec inkrementuje jego zegar w moim wektorze" % (senderUid))
            senderUidInMyMatrix = self.__nodesUid.index(senderUid)
            myIndexInMyMatrix = self.__nodesUid.index(self.getMyId())
            self.__logicalTime[myIndexInMyMatrix][senderUidInMyMatrix] +=1
            if self.__delayedMsgCache.isAlreadyDelayed(msg):
                self.logMsg("otrzymana wiadomość była w kolekcji wiadomości opóźnionych")
                self.__delayedMsgCache.delMsg(msg)
        
        isKnownMsg = None
        if self.__msgCache.isMsgOnTheList(msg):
            isKnownMsg = True
        else:
            isKnownMsg = False
            self.__msgCache.storeMsg(msg)
            
            
        needForward = not isKnownMsg
        
        # sprawdzamy czy trzeba wyslac wiadomosc do GUI
        if receiverUid == "" or receiverUid == self.getMyId():
            self.__gui.messageReceived(senderUid, self.getNickByUID(senderUid),  receiverUid,  content)
            
        return needForward
  
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
            self.__nodesUid.append(uid)
            
            #TODO: co z macierza
            for row in self.__logicalTime:
                row.append(0)
            newVector = [0] * len(self.__nodesUid) # wektor z zerami
            self.__logicalTime.append(newVector)
            
            self.__gui.addNode(uid,  username)
        else:
            print "Model: juz znam wezel o id %s" % uid
        self.__checkMatrix()

    def __checkMatrix(self):
        nodesCount = len(self.__nodesUid)
        nodesCountInMap = len(self.__nodes.keys())
        assert nodesCount == nodesCountInMap,   "bledna ilosc elemntow w tablicy i mapie (%d != %d)" % (nodesCount,  nodesCountInMap)
        assert len(self.__logicalTime) == nodesCount,  "blena ilosc wierszy (%d != %d)" %(len(self.__logicalTime) ,  nodesCount)
        for row in self.__logicalTime:
            assert len(row) == nodesCount,  "blena ilosc kolumn (%d != %d)" %(len(row) ,  nodesCount)

    def removeNode(self,  uid):
        print "Model: usuwam wskazanego wezla - %s" % uid
        name = self.getNickByUID(uid)
        index = self.__nodesUid.index(uid)
        del self.__nodes[uid]
        self.__logicalTime.pop(index)
        for row in self.__logicalTime:
            row.pop(index)
        self.__nodesUid.pop(index)
        self.__checkMatrix()
        self.__gui.delNode(uid,  name)
        
    def markNodeIsAlive(self,  uid):
        self.__nodes[uid].markIsAlive()

    def setIamFirstNode(self):
        self.addNode(self.getMyId(),  self.getNick(),  getInetAddress())

    def addMeToListOfNodes(self):
        assert len(self.__nodes) > 0,  "nie moge sie dodac do listy wezlow bo jest ona pusta '%s'" % self.__nodes
        self.addNode(self.getMyId(),  self.getNick(),  getInetAddress())
        
    def __setMaxInRow(self,  uid):
        dim = len(self.__nodesUid)
        destinationRow = self.__nodesUid.index(uid)
        for columnIdx in range(0,  dim):
            maxVal =0
            for rowIdx in range(0, dim):
                maxVal = max(maxVal,  self.__logicalTime[rowIdx][columnIdx])
            self.__logicalTime[rowIdx][columnIdx] = maxVal
        
    def logNewUser(self,  uid,  username,  ip):
        print "Model: logowanie uzytkownika %s = %s (%s)" % (username,  uid,  ip)
        try:
            self.addNode(uid,  username,  ip)
            self.__setMaxInRow(uid)
            return True
        except AssertionError as err:
            print "Model: nie mozna zalogowac uzytkownika: %s" % err
            return False
        
    def isIamAlone(self):
        return len(self.__nodes.values()) == 0

    def logMsg(self,  msg):
        print "Model: %s" % msg

    def setGui(self , gui):
        self.__gui = gui

