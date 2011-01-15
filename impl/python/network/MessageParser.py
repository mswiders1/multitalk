# -*- coding: utf-8 -*-
import re

DISCOVERY_MSG = u'MULTITALK_5387132'
pattern = re.compile("BEGIN_MESSAGE:(\d+){1}")

def getMessageLen(lineOfText):
    d = pattern.search(lineOfText)
    if d:
        lenght = d.group(1)
        return int(lenght)
    else:
        return None
 
def getMsgWithJSONInside(json):
    jsonLen = len(json)
    output = "BEGIN_MESSAGE:" + str(jsonLen) + ":\n"
    output += json + "\n"
 
def isMsgLoginReqViaTcp(msg):
    if cmp(msg,  DISCOVERY_MSG) == 0:
        return True
    else:
        return False
