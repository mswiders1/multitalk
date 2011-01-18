# -*- coding: utf-8 -*-


import json
import queues
import appVar

def unpackHiiMsg(jsonObj,  senderIP):
    message = {}
    message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.HII_MESSAGE_RECEIVED
    nodes = []
    for v in jsonObj['VECTOR']:
        node={}
        node['UID'] = v['UID']
        node['NICK'] = v['USERNAME']
        node['IP'] = v['IP_ADDRESS']
        nodes.append(node)
    message['NODES'] = nodes
    message['UID'] = jsonObj['UID']
    message['NICK'] = jsonObj['USERNAME']
    message['IP'] = senderIP
    return message

def getLogMsg():
    model = appVar.modelInstance
    id = model.getMyId()
    nick = model.getNick()
    list = {'TYPE': u"LOG", 'UID': id,  "USERNAME":nick,  'IP_ADDRESS': appVar.modelInstance.getMyIP()}
    return json.dumps(list,  indent=4)

def getOutMsg():
    list = {'TYPE': u"OUT", 'UID': appVar.modelInstance.getMyId()}
    return json.dumps(list,  indent=4)

def getLivMsg():
    list = {'TYPE': u"LIV", 'UID': appVar.modelInstance.getMyId(),  'IP_ADDRESS': appVar.modelInstance.getMyIP()}
    return json.dumps(list,  indent=4)
    
def getGetMsg(uidOfMissingMsgSender,  timeOfSend):
    list = {'TYPE': u"GET", 'UID': uidOfMissingMsgSender,  'MSG_ID': timeOfSend}
    return json.dumps(list,  indent=4)
    
def getP2pMsg():
    list = {'TYPE': u"P2P"}
    return json.dumps(list,  indent=4)
    
def getHiiMsg():
    model = appVar.modelInstance
    assert(model)
    nick = model.getNick()
    id = model.getMyId()
    nodes = model.getListOfNodes()
    assert(nick)
    assert(id)
    assert(nodes)
    assert(len(nodes) >= 1)
    assert nodes.count(id) == 1,   "brak wezla w na liscie %s" % nodes
    nodesWithInfo = []
    for node in nodes:
        n = {}
        n['UID'] = node
        n['USERNAME'] = model.getNickByUID(node)
        n['IP_ADDRESS'] = model.getIPByUID(node)
        nodesWithInfo.append(n)
    list = {'TYPE': u"HII", 'UID': id,  "USERNAME":nick,  "VECTOR":nodesWithInfo}
    return json.dumps(list,  indent=4)

