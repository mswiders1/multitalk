# -*- coding: utf-8 -*-
import os,sys

from PyQt4 import QtCore,QtGui, Qt
from qtForms.Ui_test import Ui_Test


class Test(QtGui.QWidget):
    def __init__(self,  stylesheet):
        QtGui.QWidget.__init__(self)
        self.ui=Ui_Test()
        self.ui.setupUi(self)
        self.ui.styleEdit.setPlainText(stylesheet)
        
    def on_reload_clicked(self):
        stylesheet = self.ui.styleEdit.toPlainText()
        QtCore.QCoreApplication.instance().setStyleSheet(stylesheet)
        

   
