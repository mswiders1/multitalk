# -*- coding: utf-8 -*-

from twisted.internet.task import LoopingCall

INTERVAL = 5
BEGIN = 3

class Heartbeat(LoopingCall):
    
    def __init__(self,  core):
        LoopingCall.__init__(self,  core.handleHeartbeatTimePassed)
    
    def start(self):
        print "Heartbeat: start"
        LoopingCall.start(self,  interval=INTERVAL,  now=BEGIN)
        
    def stop(self):
        print "Heartbeat: stop"
        LoopingCall.stop(self)
        
