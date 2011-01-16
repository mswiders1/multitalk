# -*- coding: utf-8 -*-

"""Pobieranie informacji o kartach sieciowych"""

import netinfo


def getDefaultGatewayInterfaceName():
    for route in netinfo.get_routes():
        if route['gateway'] == '0.0.0.0':
            return route['dev']
    print "Brak sieci!!"
    assert(False)

def getBroadcastAddress():
    interface = getDefaultGatewayInterfaceName()
    return netinfo.get_broadcast(interface)
    
def getInetAddress():
    interface = getDefaultGatewayInterfaceName()
    return netinfo.get_ip(interface)

def getMacAddr():
    interface = getDefaultGatewayInterfaceName()
    return netinfo.get_hwaddr(interface)
    
def isMyAddr(addr):
    for devName in netinfo.list_active_devs():
        #print "Addr %s %s" % (netinfo.get_ip(devName),  addr)
        if cmp(netinfo.get_ip(devName),  addr) == 0:
            return True
    return False
   
