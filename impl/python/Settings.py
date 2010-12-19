# -*- coding: utf-8 -*-
import os,sys

from PyQt4 import QtCore,QtGui, Qt
from qtForms.Ui_settings import Ui_Settings

ADDR = "address"
PORT = "port"
DELAY = "delay"

class Settings(QtGui.QDialog):
    def __init__(self,  qsettings):
        QtGui.QDialog.__init__(self)
        icon = QtGui.QIcon()
        icon.addPixmap(QtGui.QPixmap(QtCore.QString.fromUtf8("Android-Messages-256.png")), QtGui.QIcon.Normal, QtGui.QIcon.Off)
        self.setWindowIcon(icon)
        self.qsettings = qsettings
        self.ui=Ui_Settings()
        self.ui.setupUi(self)
        self.restoreValues()
        self.fromValuesToControls()
        self.errorMsg = str();
        print("Settings showed")
        
    def fromValuesToControls(self):
        self.ui.address.setText(self.addr)
        self.ui.port.setValue(self.port)
        self.ui.delay.setValue(self.delay)
        
    def fromControlsToValues(self):
        self.addr = self.ui.address.text()
        self.port = self.ui.port.value()
        self.delay = self.ui.delay.value()
        
    def saveValues(self):
         self.qsettings.setValue(ADDR,  self.addr)
         self.qsettings.setValue(PORT,  self.port)
         self.qsettings.setValue(DELAY,  self.delay)
         self.qsettings.sync()
        
    def restoreValues(self):
        self.addr = self.qsettings.value(ADDR,  QtCore.QVariant("239.255.0.1")).toString()
        self.port = self.qsettings.value(PORT,  QtCore.QVariant(1234)).toInt()[0]
        self.delay = self.qsettings.value(DELAY,  QtCore.QVariant(0)).toInt()[0]
        
    def on_buttonBox_accepted(self):
        print("Accepted configuration")
        self.fromControlsToValues()
        self.saveValues()
        self.accepted = True
    
    def on_buttonBox_rejected(self):
        print("Rejected!")
        self.accepted = False
        
    def setError(self,  msg):
        pass
        
    def getSettings(self):
        if self.accepted:
            config = {}
            config["address"] = str(self.addr)
            config["port"] = self.port
            config["delay"] = self.delay
            return config
        else:
            return None
