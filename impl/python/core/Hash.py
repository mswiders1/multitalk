# -*- coding: utf-8 -*-

from network.Interface import *
import hashlib
import base64

def generateUserId(nick):
    #H(MAC + IP + username) SHA-1 base 64 
    s = hashlib.sha1()
    s.update(getMacAddr())
    s.update(getInetAddress())
    s.update(nick)
    d = s.digest()
    encoded = base64.b64encode(d)
    return encoded
    
