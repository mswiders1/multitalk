# -*- coding: utf-8 -*-

from PyQt4 import QtCore,QtGui, Qt
from qtForms.Ui_window import Ui_MainWindow
from qtModels.ConversationModel import ConversationModel
from qtModels.PeersModel import PeersModel
import Queue
import queues

appName = "SRAppp"
aboutMe = "MarcinKamionowski"
windowGeo = "windowGeometry"
windowFlags = "windowFlags"
splitterLeftRight = "splitterConvPeers"
splitterUpDown = "splitterUpDown"
GUI_TIMER_INTERVAL_IN_MS = 100

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
        self.startTimer(GUI_TIMER_INTERVAL_IN_MS)
        self.showLoginForm()
     
    def showLoginForm(self):
        (nickName, ok)  = QtGui.QInputDialog.getText (self, Qt.QObject.trUtf8(self,"Podaj nazwę"), Qt.QObject.trUtf8(self,"Podaj nick"))
        message = {}
        message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.USER_LOGIN
        if ok:
            message['STATUS'] = True
            message['NICK'] = unicode(nickName)
        else:
            message['STATUS'] = False;
        queues.coreQueue.put(message)

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
        if self.canClose:
            self.saveWindowState()
            QtGui.QMainWindow.closeEvent(self,  closeEvent)
        else:
            print("GUI: close event")
            closeEvent.ignore()
            message = {}
            message['CORE_MSG_TYPE'] = queues.CORE_MSG_TYPE.CLOSE_APP_REQ
            queues.coreQueue.put(message)
        
    def parseQueue(self):
        try:
            queueElement = queues.guiQueue.get_nowait()
            #print("GUI: Parsing queue element %s" % queueElement)
            
            if queueElement['GUI_MSG_TYPE'] == queues.GUI_MSG_TYPE.BROADCAST_WIN_SHOW:
                self.pd = QtGui.QProgressDialog("Broadcast in progress.", QtCore.QString(), 0, 100,  self)
                self.setWindowTitle("Progress...")
                self.pd.open()
            
            if queueElement['GUI_MSG_TYPE'] == queues.GUI_MSG_TYPE.BROADCAST_PROGRESS:
                self.pd.setValue(queueElement['PROGRESS'])
                
            if queueElement['GUI_MSG_TYPE'] == queues.GUI_MSG_TYPE.BROADCAST_WIN_CLOSE:
                self.pd.cancel()
            
            if queueElement['GUI_MSG_TYPE'] == queues.GUI_MSG_TYPE.CLOSE_APP:
                print("GUI: zamykamy okno")
                self.canClose = True
                self.close()


        except Queue.Empty:
            pass
        except:
            traceback.print_exc(file=sys.stdout)
            raise
        
    def timerEvent(self,  timerEvent):
        self.parseQueue()
