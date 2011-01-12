# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys, traceback,  SocketServer,  threading,  socket,  time

class ThreadedTCPRequestHandler(SocketServer.BaseRequestHandler):

    def handle(self):
        data = self.request.recv(1024)
        cur_thread = threading.currentThread()
        response = "%s: %s" % (cur_thread.getName(), data)
        self.request.send(response)

class ThreadedTCPServer(SocketServer.ThreadingMixIn, SocketServer.TCPServer):
    pass

def startTcpServer(host='localhost',  port=3445):
    server = ThreadedTCPServer((host, port), ThreadedTCPRequestHandler)
    ip, port = server.server_address
    # Start a thread with the server -- that thread will then start one
    # more thread for each request
    server_thread = threading.Thread(target=server.serve_forever)
    # Exit the server thread when the main thread terminates
    server_thread.setDaemon(True)
    server_thread.start()
    print "Server TCP loop running in thread:", server_thread.getName()


def __tcpClientHandler(host,  port, timeBetweenAttemps = 2):
    print "Connecting to TCP server %s:%s" % (host,  port)
    counter = 0
    while 1:
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            # Connect to server and send data
            sock.connect((host, port))
            counter = 0
            sock.send("Hej!\n")
            # Receive data from the server and shut down
            received = sock.recv(1024)
            sock.close()
        except  IOError as err:
            print err
            counter += 1
            if counter < 3:
                print "Wait %s sec and try one more time" % timeBetweenAttemps
                time.sleep(timeBetweenAttemps)
            else:
                print "Give up! 3 attemps has been made"
                break
    
def connectToTcpServer(host,  port):
    threading.Thread(target=__tcpClientHandler,  args=(host,  port)).start()
    
    
