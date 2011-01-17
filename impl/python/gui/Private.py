# -*- coding: utf-8 -*-

from PyQt4 import QtCore,QtGui, Qt
from qtForms.Ui_private import Ui_Private
from qtModels.ConversationModel import ConversationModel

class Private(QtGui.QDialog):
    
    def __init__(self,  parent, core,  uid,  useraname):
        QtGui.QDialog.__init__(self)
        self.mainWndow = parent
        self.core = core
        self.uidOfOtherSide = uid
        self.nameOfOtherSide = useraname
        self.setWindowTitle("Rozmowa z %s" % useraname)
        self.ui=Ui_Private()
        self.ui.setupUi(self)
        self.initModels()
        self.ui.msgText.setFocus()
        
    def on_sendButton_released(self):
        msg = unicode(self.ui.msgText.text())
        self.ui.msgText.setText(u"")
        self.core.sendMessage(self.uidOfOtherSide,  msg)
        
    def messageReceived(self,  uidSender,  msg):
        print u"Otrzymano wiadomość '%s' od %s" %(msg,  uidSender)
        self.conversationModel.addMessage(self.core.userNameByUid(uidSender),  msg)
        
    def on_newMessage_returnPressed(self):
        mouseEvent = QtGui.QMouseEvent(QtCore.QEvent.MouseButtonPress, self.ui.sendButton.pos(), QtCore.Qt.LeftButton, QtCore.Qt.LeftButton, QtCore.Qt.NoModifier )
        QtGui.QApplication.sendEvent(self.focusWidget(), mouseEvent)
        mouseEvent = QtGui.QMouseEvent(QtCore.QEvent.MouseButtonRelease, self.ui.sendButton.pos(), QtCore.Qt.LeftButton, QtCore.Qt.LeftButton, QtCore.Qt.NoModifier )
        QtGui.QApplication.sendEvent(self.focusWidget(), mouseEvent)
        self.ui.sendButton.click()
    
    def on_delay_valueChanged(self,  delayInSec):
        self.core.setDelayPerNode(self.uidOfOtherSide,  int(delayInSec))
    
    def initModels(self):
        self.conversationModel = ConversationModel()
        self.ui.conversationView.setModel(self.conversationModel)
        tv = self.ui.conversationView.horizontalHeader()
        tv.setStretchLastSection(True)
        self.ui.conversationView.resizeColumnsToContents()
        
    def showMsgInLogView(self,  message):
        text = self.ui.logView.toPlainText()
        self.ui.logView.setPlainText(text.append(message).append("\n"))

    def closeEvent(self,  closeEvent):
        print "Gui: zamykam okno rozmowy"
        self.mainWndow.conversationWindowClosed(self.uidOfOtherSide)
    
    def setCore(self,  core):
        self.core = core
    
