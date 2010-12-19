# -*- coding: utf-8 -*-

"""The user interface for our app"""

import os,sys

from PyQt4 import QtCore,QtGui, Qt
from Ui_window import Ui_MainWindow
from ConversationModel import ConversationModel
from PeersModel import PeersModel
from Settings import Settings
from Test import Test
from Core import Core
from res_rc import *
import socket

appName = "SRAppp"
aboutMe = "MarcinKamionowski"
windowGeo = "windowGeometry"
windowFlags = "windowFlags"
splitterLeftRight = "splitterConvPeers"
splitterUpDown = "splitterUpDown"

core = None

# Create a class for our main window
class Main(QtGui.QMainWindow):
    logger = None
    
    def __init__(self,  qsettings):
        QtGui.QMainWindow.__init__(self)
        self.settings = qsettings
        self.ui=Ui_MainWindow()
        self.ui.setupUi(self)
        self.initModels()
        self.restoreWindowState()
        Main.logger = self.showMsgInLogView
        self.ui.newMessage.setFocus()
        print("Window showed")
        
    def on_sendButton_released(self):
        self.peersModel.addPeer("192.168.0.1",  "Firebird")
        self.conversationModel.addMessage(QtCore.QDateTime.currentDateTime(),  "Marcin",  "Hej tu ja!")
        if callable(Main.logger):
            Main.logger(self.ui.newMessage.text())
        else:
            print("Cannot log msg:")
            print(self.ui.newMessage.text())
            
    def on_newMessage_returnPressed(self):
        mouseEvent = QtGui.QMouseEvent(QtCore.QEvent.MouseButtonPress, self.ui.sendButton.pos(), QtCore.Qt.LeftButton, QtCore.Qt.LeftButton, QtCore.Qt.NoModifier )
        QtGui.QApplication.sendEvent(self.focusWidget(), mouseEvent)
        mouseEvent = QtGui.QMouseEvent(QtCore.QEvent.MouseButtonRelease, self.ui.sendButton.pos(), QtCore.Qt.LeftButton, QtCore.Qt.LeftButton, QtCore.Qt.NoModifier )
        QtGui.QApplication.sendEvent(self.focusWidget(), mouseEvent)
        self.ui.sendButton.click()
            
    def initModels(self):
        self.conversationModel = ConversationModel()
        self.ui.conversationView.setModel(self.conversationModel)
        tv = self.ui.conversationView.horizontalHeader()
        tv.setStretchLastSection(True)
        self.ui.conversationView.resizeColumnsToContents()
        self.peersModel = PeersModel()
        self.ui.peersView.setModel(self.peersModel)
        tv = self.ui.peersView.horizontalHeader()
        tv.setStretchLastSection(True)
        self.ui.conversationView.resizeColumnsToContents()
        
    def showMsgInLogView(self,  message):
        text = self.ui.logView.toPlainText()
        self.ui.logView.setPlainText(text.append(message).append("\n"))

    def restoreWindowState(self):
        self.settings = QtCore.QSettings(aboutMe, appName); 
        self.restoreGeometry(self.settings.value(windowGeo,  QtCore.QVariant()).toByteArray() );
        self.restoreState(self.settings.value(windowFlags,  QtCore.QVariant()).toByteArray() );
        self.ui.splitterUpDown.restoreState(self.settings.value(splitterUpDown,  QtCore.QVariant()).toByteArray() );
        self.ui.splitterLeftRight.restoreState(self.settings.value(splitterLeftRight,  QtCore.QVariant()).toByteArray() );
        
    def saveWindowState(self):
        self.settings.setValue(windowGeo, self.saveGeometry());
        self.settings.setValue(windowFlags, self.saveState());
        self.settings.setValue(splitterLeftRight, self.ui.splitterLeftRight.saveState());
        self.settings.setValue(splitterUpDown, self.ui.splitterUpDown.saveState());
        
    def closeEvent(self,  closeEvent):
        self.saveWindowState()
        QtGui.QMainWindow.closeEvent(self, closeEvent);
   
def loadStyle():
    file = QtCore.QFile(":/qss/default.qss")
    file.open(QtCore.QFile.ReadOnly)
    stylesheet = QtCore.QString(file.readAll())
    return stylesheet
    
def main():
    app = QtGui.QApplication(sys.argv)
    stylesheet = loadStyle()
    app.setStyleSheet(stylesheet)
    settings = QtCore.QSettings(aboutMe, appName);
    settingsWin = Settings(settings)
    core = Core()
    while True:
        settingsWin.exec_()
        values = settingsWin.getSettings()
        if values:
            try:
                core.connect(values)
            except socket.error as err:
                print err
                continue
            except Exception as err:
                print err
                continue
            else:
                window=Main(settings)
                window.show()
                #test= Test(stylesheet)
                #test.show()
                sys.exit(app.exec_())
        print ("Exit")
        sys.exit(0)
    
if __name__ == "__main__":
    print("Call main()")
    main()
