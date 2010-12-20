# -*- coding: utf-8 -*-

import json

def getDiscoveryMsg():
    list = {'TYPE': "WHO", 'COMMENT': "Broadcast msg: who is there?"}
    return json.dumps(list,  indent=4)

