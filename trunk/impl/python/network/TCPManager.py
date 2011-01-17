# -*- coding: utf-8 -*-

class Singleton():
    __single = None
    
    def __init__( self ):
        if Singleton.__single:
            raise Singleton.__single
        Singleton.__single = self  

class TCPManager(Singleton):
    
    def __init__(self):
        self.__unmappedProtocols= []
        self.__mappedProtocols = {}
    
    def newConnection(self,  protocol):
        self.logMsg("dodaje polączenie %s" % protocol)
        assert(self.__unmappedProtocols.count(protocol) == 0)
        self.__unmappedProtocols.append(protocol)
        
    def delConnection(self,  protocol):
        self.logMsg("usuwam polączenie %s" % protocol)
        if self.__unmappedProtocols.count(protocol):
            self.__unmappedProtocols.remove(protocol)
        elif self.__mappedProtocols.values().count(protocol):
            for uid in self.__mappedProtocols.keys():
                if self.__mappedProtocols[uid] == protocol:
                    del self.__mappedProtocols[uid]
                    return
            assert 1==0,  "nie znaleziono polaczenia"
            #TODO: uzytkownik byl zalogowany wiec moze trzeba kogos powiadomic?
        else:
            assert 1==0,  "nie znam takiego polaczenia"
        
    def mapNodeToConnection(self,  uid,  protocol):
        self.logMsg("mapuje wezel %s na polaczenie %s" % (uid,  protocol))
        self.__unmappedProtocols.remove(protocol)
        assert(self.__mappedProtocols.has_key(uid) == False)
        self.__mappedProtocols[uid] = protocol
        
    def getConnectionToNode(self,  uid):
        return self.__mappedProtocols[uid]
        
    def getUnmappedConnections(self):
        return list(self.__unmappedProtocols)
    
    def getMappedConnections(self):
        return self.__mappedProtocols.values()
        
    def logMsg(self,  msg):
        print "TCPM: %s" % msg
