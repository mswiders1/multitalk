# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys, traceback,  SocketServer,  threading,  socket,  time

from network.MessageParser import *
from Json import deserializeJsonObject,  unpackHiiMsg
import queues
TCP_PORT = 3554

class ThreadedTCPRequestHandler(SocketServer.StreamRequestHandler):

    def handle(self):
        self.waitingForFirstMsg = True
        print "TCP: przychodzace polaczenie z %s" % self.client_address[0]
        while 1:
            isCorrect,  json = readMsgFromStream("TCP",  self.rfile,  self.client_address[0])
            if isCorrect:
                #TODO : asdasd
                jsonObj = deserializeJsonObject(json)
                if jsonObj['TYPE'] == "HII":
                    print "TCP: odczytano wiadomosc typu Hi"
                    message = unpackHiiMsg(jsonObj,  self.client_address[0])
                    queues.coreQueue.put(message)
                else:
                    print "TCP: nieznany typ wiadomosci"
            else:
                print "TCP: zamykam polaczenie - nie mozna odebrac wiadomosci od %s" % self.client_address[0]
                return

class ThreadedTCPServer(SocketServer.ThreadingMixIn, SocketServer.TCPServer):
    pass

class TCPServer():
    
    def __init__(self):
        self.server = None
    
    def startTcpServer(self, hostIp='0.0.0.0',  port=TCP_PORT):
        # uruchamia serwer TCP
        print "TCP: uruchamianie serwera TCP na %s:%s" % (hostIp,  str(port))
        self.server = ThreadedTCPServer((hostIp, port), ThreadedTCPRequestHandler)
        ip, port = self.server.server_address
        server_thread = threading.Thread(target=self.server.serve_forever)
        server_thread.setDaemon(True)
        server_thread.start()
        print "TCP: uruchomiono serwer TCP na %s:%s" % (hostIp,  str(port))

    def stop(self):
        print "TCP: zamykam serwer"
        self.server.shutdown();

def __tcpClientHandler(host,  port, timeBetweenAttemps = 2):
    counter = 0
    state = "init"
    while 1:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # Connect to server and send data
            print "TCP-c: proba nawiazania polaczenia do %s:%s" % (host,  port)
            connectionTimeout = 5
            sock.connect((host, connectionTimeout,   port))
            file = sock.makefile("rb")
            print "TCP-c: podlaczono do %s:%s" % (host,  port)
            counter = 0
            while(1):
                if state == "init":
                    msgToSend = getFullHiiMsg()
                    print "TCP-c: wysylam przywitanie do %s: %s" % (host,  msgToSend)
                    file.write(msgToSend)
                    file.flush()
                    print "TCP-c: wyslano przywitanie do %s" % (host)
                    state = "waitFotHii"
                else:
                    isCorrect,  json = readMsgFromStream("TCP-c",  file,  host)
                    if isCorrect:
                        print "TCP-c: odebrano wiadomosc od %s: '%s'" %(json,  host)
                    else:
                        print "TCP-c: nie można odczytać wiadomosci"
                        break
            print "TCP-c: koniec - zamykam polaczenie"
            sock.close()
        except  IOError as err:
            print err
            counter += 1
            if counter < 3:
                print "Wait %s sec and try one more time" % timeBetweenAttemps
                time.sleep(timeBetweenAttemps)
            else:
                print "Give up! 3 attemps has been made"
                return

def readMsgFromStream(whoCall,  stream,  addr):
    while 1:
        print "%s: proba odczytu linijki tekstu od %s" %( whoCall,  addr)
        lineOfText = stream.readline()
        if not lineOfText:
            print "%s: nie mozna odczytac linijki tekstu od %s" % (whoCall,  addr)
            return (False,  None)
        len = getMessageLen(lineOfText)
        if len:
            print "%s: Odczytywanie zawartosci wiadomosci od %s(%s)" % (whoCall, addr, str(len))
            json = stream.read(len)
            print "%s: odczytano wiadomosc o dlugosci %s: %s" % (whoCall,  len,  json)
            return (True,  json)
        else:
            print "%s: bledny poczatek wiadomosci od %s: %s" % (whoCall, addr,  lineOfText)
            #i wracamy na poczatek petli
        
def connectToTcpServer(host,  port=TCP_PORT):
    #uruchamia watek ktory za zadanie ma podlaczyc sie do wskazanego wezla
    threading.Thread(target=__tcpClientHandler,  args=(host,  port)).start()
    
    
