# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys, traceback,  SocketServer,  threading,  socket,  time

from network.MessageParser import *
from Json import *
TCP_PORT = 3554

class ThreadedTCPRequestHandler(SocketServer.StreamRequestHandler):

    def handle(self):
        self.waitingForFirstMsg = True
        print "TCP: przychodzace polaczenie z ",  self.client_address
        while 1:
            lineOfText = self.rfile.readline()
            len = getMessageLen(lineOfText)
            if len:
                msg = self.request.recv(len)
                print "Odebrano wiadomosc od %s: %s" % (self.client_address,  msg)
                if self.waitingForFirstMsg and isMsgLoginReqViaTcp(msg):
                    self.waitingForFirstMsg = False
                    print "TCP: wezel sie podlaczyl do nas - mozemy sie zalogowac"
                else:
                    if not self.waitingForFirstMsg:
                        print "TCP: wezel wyslal JSNO-a"
                    else:
                        print "TCP: chcialem sie zalogowac ale druga strona zle przedstawila sie"
                        return
            else:
                print "TCP: nie prawidlowy poczatek wiadomosci: '%s'" %  lineOfText
        

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
    while 1:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # Connect to server and send data
            print "TCP: proba nawiazania polaczenia do %s:%s" % (host,  port)
            sock.settimeout(5)
            sock.connect((host, port))
            print "TCP: podlaczono do %s:%s" % (host,  port)
            counter = 0
            jsonMsgLogin = getHiiMsg()
            sock.send(getMsgWithJSONInside(jsonMsgLogin))
            # Receive data from the server and shut down
            #received = sock.recv(1024)
            #sock.close()
        except  IOError as err:
            print err
            counter += 1
            if counter < 3:
                print "Wait %s sec and try one more time" % timeBetweenAttemps
                time.sleep(timeBetweenAttemps)
            else:
                print "Give up! 3 attemps has been made"
                break
    
def connectToTcpServer(host,  port=TCP_PORT):
    #uruchamia watek ktory za zadanie ma podlaczyc sie do wskazanego wezla
    threading.Thread(target=__tcpClientHandler,  args=(host,  port)).start()
    
    
