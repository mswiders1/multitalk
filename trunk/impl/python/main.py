# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys, traceback

from PyQt4 import QtCore,QtGui, Qt
from gui.Main import *
from Settings import Settings
from Test import Test
from core.Core import Core
from qtForms.res_rc import *
import Queue
import queues

core = None
   
def loadStyle():
    file = QtCore.QFile(":/qss/default.qss")
    file.open(QtCore.QFile.ReadOnly)
    stylesheet = QtCore.QString(file.readAll())
    return stylesheet
    
def main():
    app = QtGui.QApplication(sys.argv)
    stylesheet = loadStyle()
    app.setStyleSheet(stylesheet)
    core = Core()
    core.start()
    qtWinSettings = QtCore.QSettings(aboutMe, appName);
    window=Main(qtWinSettings)
    window.show()
    sys.exit(app.exec_())
     
if __name__ == "__main__":
    print("Call main()")
    main()
