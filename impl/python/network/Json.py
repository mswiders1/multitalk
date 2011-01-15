# -*- coding: utf-8 -*-

import json
from core.Core import *

def getHiiMsg():
    assert(model)
    nick = model.getNIck()
    id = model.getMyId()
    nodes = model.getListOfNodes()
    assert(nick)
    assert(id)
    assert(nodes)
    assert(len(nodes) >= 1)
    assert(nodes.count(id) == 1)
    list = {'TYPE': u"HII", 'UID': id,  "USERNAME":nick,  "VECTOR":nodes}
    return json.dumps(list,  indent=4)
