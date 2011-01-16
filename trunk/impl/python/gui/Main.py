# -*- coding: utf-8 -*-

from PyQt4 import QtCore,QtGui, Qt
from qtForms.Ui_window import Ui_MainWindow
from qtModels.ConversationModel import ConversationModel
from qtModels.PeersModel import PeersModel

appName = "SRAppp"
aboutMe = "MarcinKamionowski"
windowGeo = "windowGeometry"
windowFlags = "windowFlags"
splitterLeftRight = "splitterConvPeers"
splitterUpDown = "splitterUpDown"

# Create a class for our main window
class Main(QtGui.QMainWindow):
    logger = None
    
    def __init__(self,  qsettings):
        QtGui.QMainWindow.__init__(self)
        self.canClose = False
        self.settings = qsettings
        self.ui=Ui_MainWindow()
        self.ui.setupUi(self)
        self.initModels()
        self.restoreWindowState()
        Main.logger = self.showMsgInLogView
        self.ui.newMessage.setFocus()
        
    def show(self):
        self.showLoginForm()
        QtGui.QMainWindow.show(self)
        
    def showLoginForm(self):
        while 1:
            (nickName, ok)  = QtGui.QInputDialog.getText (self, Qt.QObject.trUtf8(self,"Podaj nazwę"), Qt.QObject.trUtf8(self,"Podaj nick"))
            if ok and len(nickName) > 0:
                break
            else:
                print "Gui: uzytkownik podal zly nick"
        if ok:
            self.core.handleUserInsertedNick(nickName)
        else:
            self.core.closeApp()

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
        if self.core.closeApp():
            return
        else:
            closeEvent.ingore()
    
    def setBroadcastProgress(self,  progress):
        if progress == 0:
            self.pd = QtGui.QProgressDialog("Broadcast in progress.", QtCore.QString(), 0, 100,  self)
            self.setWindowTitle("Progress...")
            self.pd.open()
        elif progress == 100:
            self.pd.cancel()
        else:
            self.pd.setValue(progress)

    def setCore(self,  core):
        self.core = core
    
