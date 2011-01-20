# -*- coding: utf-8 -*-

from PyQt4 import QtCore,QtGui, Qt
from PyQt4.QtCore import QModelIndex
from qtForms.Ui_window import Ui_MainWindow
from qtModels.ConversationModel import ConversationModel
from qtModels.PeersModel import PeersModel
from Private import Private

appName = "SRAppp"
aboutMe = "MarcinKamionowski"
windowGeo = "windowGeometry"
windowFlags = "windowFlags"
splitterLeftRight = "splitterConvPeers"
splitterUpDown = "splitterUpDown"
TO_ALL = ""
# Create a class for our main window
class Main(QtGui.QMainWindow):
    logger = None
    
    def __init__(self,  qsettings=None):
        QtGui.QMainWindow.__init__(self)
        self.peerToWindow = {}
        self.setWindowTitle("Multitalk")
        self.canClose = False
        self.settings = qsettings
        self.ui=Ui_MainWindow()
        self.ui.setupUi(self)
        self.initModels()
        self.restoreWindowState()
        Main.logger = self.showMsgInLogView
        self.ui.newMessage.setFocus()
        
    def showGui(self):
        if self.showLoginForm():
            QtGui.QMainWindow.show(self)
            return True
        else:
            self.close()
            return False
        
    def showLoginForm(self):
        (networkAddr,  ok)  = QtGui.QInputDialog.getText (self, Qt.QObject.trUtf8(self,"Podaj adres ip"), Qt.QObject.trUtf8(self,"Podaj adres ip lub anuluj"))
        if ok and len(unicode(networkAddr)) > 0:
           self.core.userInsertedNetworkAddr(unicode(networkAddr))
        elif ok:
            print "Gui: wprowadzono pusty adres hosta"
        while 1:
            (nickName, ok)  = QtGui.QInputDialog.getText (self, Qt.QObject.trUtf8(self,"Podaj nazwę"), Qt.QObject.trUtf8(self,"Podaj nick"))
            if ok and len(nickName) > 0:
                break
            elif ok:
                print "Gui: uzytkownik podal zly nick"
            else:
                return False
        self.core.handleUserInsertedNick(unicode(nickName))
        title = self.windowTitle()
        self.setWindowTitle(unicode(title) + u" (" + unicode(nickName) + ")")
        return True
        
    def on_sendButton_released(self):
        msg = unicode(self.ui.newMessage.text())
        self.ui.newMessage.setText(u"")
        self.core.sendMessage(TO_ALL,  msg)
        
    def on_peersView_clicked(self):
        index = self.ui.peersView.currentIndex()
        rowNumber = index.row()
        name,  uid = self.peersModel.getPeerData(rowNumber)
        if self.peerToWindow.keys().count(uid):
            print "Gui: okno juz jest otwarte"
        elif self.core.isThisMyUid(uid):
            print "Gui: sam z sobą chcesz rozmawiać?"
        else:
            print "Gui: otwieram okno rozmowy z %s(%s)" % (uid,  name)
            self.__openPrivateWin(uid,  name)
        
    def __openPrivateWin(self,  uid,  name):
        newWin = Private(self, self.core,  uid,  name)
        newWin.show()
        self.peerToWindow[uid] = newWin
        return newWin
        
    def conversationWindowClosed(self,  uid):
        del self.peerToWindow[uid]
        
    def addNode(self,  uid,  name):
        Main.logger(u"Dodano użytkownika %s" % name)
        self.peersModel.addPeer(uid,  name)
        
    def delNode(self,  uid,  name):
        Main.logger(u"Usunięto użytkownika %s" % (name))
        self.peersModel.delPeer(uid)
        if self.peerToWindow.keys().count(uid):
            print "Gui: blokuje okno rozmowy z uzytkownikiem %s " % uid
            self.peerToWindow[uid].userDisconnected()
            
    def messageReceived(self,  uidSender, senderName,  uidReceiver,  msg):
        isMsgToAll = uidReceiver == TO_ALL
        Main.logger(u"Otrzymano wiadomość '%s' od %s (do : %s)" %(msg,  uidSender,  uidReceiver))
        if isMsgToAll:
            self.conversationModel.addMessage(self.core.userNameByUid(uidSender),  msg)
        else:
            if self.core.isThisMyUid(uidSender):
                peerWin = self.peerToWindow[uidReceiver]
            elif self.peerToWindow.keys().count(uidSender):
                peerWin = self.peerToWindow[uidSender]
            else:
                print "Gui: brak okna dla %s " % uidSender
                peerWin = self.__openPrivateWin(uidSender,  senderName)
            peerWin.messageReceived(uidSender, msg)
            
   
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
        print "Gui: close event"
        self.saveWindowState()
        if self.core.closeApp():
            return
        else:
            closeEvent.ingore()
    
    def setBroadcastProgress(self,  progress):
        if progress == 0:
            self.pd = QtGui.QProgressDialog("Broadcast in progress.", QtCore.QString(), 0, 100,  self)
            self.pd.setWindowTitle("Progress...")
            self.pd.open()
        elif progress == 100:
            self.pd.cancel()
        else:
            self.pd.setValue(progress)
            self.pd.show()

    def setCore(self,  core):
        self.core = core
    
