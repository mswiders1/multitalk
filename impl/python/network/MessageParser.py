# -*- coding: utf-8 -*-
import re
from Json import *

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
    output = "BEGIN_MESSAGE:" + str(jsonLen + 1) + "\n"
    output += json + "\n"
    return output

def getFullTcpFirstMsg():
    inner = getFirstTcpMsg()
    return getMsgWithJSONInside(inner)

def getFullHiiMsg():
    inner = getHiiMsg()
    return getMsgWithJSONInside(inner)

def getFullLogMsg():
    inner = getLogMsg()
    return getMsgWithJSONInside(inner)

def getFullOutMsg():
    inner = getOutMsg()
    return getMsgWithJSONInside(inner)
    
def getFullP2pMsg():
    inner = getP2pMsg()
    return getMsgWithJSONInside(inner)

def getFullMtxMsg():
    inner = getMtxMsg()
    return getMsgWithJSONInside(inner)

def getFullGetMsg(senderOfMissinMsg,  timeOfSend):
    inner = getGetMsg(senderOfMissinMsg,  timeOfSend)
    return getMsgWithJSONInside(inner)

def getFullMsgMsg(receiverUid, content):
    inner = getMsgMsg(receiverUid, content)
    return getMsgWithJSONInside(inner)

def getFullLivMsg():
    inner = getLivMsg()
    return getMsgWithJSONInside(inner)
