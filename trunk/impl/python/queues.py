# -*- coding: utf-8 -*-

import threading
import Queue
import collections

coreQueue = Queue.Queue()

dircoll=collections.namedtuple('CORE_MSG_TYPE', ('BROADCAST_END', 'BROADCAST_BEGIN', 'BROADCAST_PROGRESS',  'BROADCAST_REQUEST_RECEIVED'))
CORE_MSG_TYPE=dircoll('BROADCAST_END','BROADCAST_BEGIN','BROADCAST_PROGRESS',  'BROADCAST_REQUEST_RECEIVED')

dircoll=collections.namedtuple('GUI_MSG_TYPE', ('BROADCAST_WIN_SHOW', 'BROADCAST_WIN_CLOSE', 'BROADCAST_PROGRESS'))
GUI_MSG_TYPE=dircoll('SHOW_WIN','CLOSE_WIN','PROGRESS')


guiQueue =  Queue.Queue()
