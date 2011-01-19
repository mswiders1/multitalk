# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys, traceback

from PyQt4 import QtCore,QtGui, Qt
from gui.Main import *
from Settings import Settings
from Test import Test
from core.Core import Core
from model.Model import Model
from qtForms.res_rc import *
import appVar

from network import qt4reactor,  TCPManager
from twisted.internet import protocol


def loadStyle():
    file = QtCore.QFile(":/qss/default.qss")
    file.open(QtCore.QFile.ReadOnly)
    stylesheet = QtCore.QString(file.readAll())
    return stylesheet
    
def main():
    app = Qt.QApplication([])
    stylesheet = loadStyle()
    app.setStyleSheet(stylesheet)
    qt4reactor.install(app)
    from twisted.internet import reactor
    # make sure stopping twisted event also shuts down QT
    reactor.addSystemEventTrigger('after', 'shutdown', app.quit )
    # shutdown twisted when window is closed
    app.connect(app, QtCore.SIGNAL("lastWindowClosed()"), reactor.stop)
    qtWinSettings = QtCore.QSettings(aboutMe, appName);
    appVar.modelInstance = Model()
    appVar.guiInstance = Main(qtWinSettings)
    appVar.coreInstance = Core(reactor)
    appVar.tcpManager = TCPManager.TCPManager()
    appVar.modelInstance.setGui(appVar.guiInstance)
    appVar.coreInstance.setGui(appVar.guiInstance)
    appVar.coreInstance.setModel(appVar.modelInstance)
    appVar.coreInstance.setTcpManager(appVar.tcpManager)
    appVar.guiInstance.setCore(appVar.coreInstance)
    if appVar.guiInstance.showGui():
        reactor.run()
     
if __name__ == "__main__":
    print("Uruchamiam funkcję main()")
    main()

