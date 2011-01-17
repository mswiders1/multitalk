# -*- coding: utf-8 -*-

from PyQt4.QtGui import *
from PyQt4.QtCore import * 

class ConversationModel(QAbstractTableModel): 
    def __init__(self, parent=None, *args): 
        """ datain: a list of lists
            headerdata: a list of strings
        """
        QAbstractTableModel.__init__(self, parent, *args) 
        self.arraydata = []
        self.headerdata = ['Date', 'Source', 'Message']
        self.lastMsgId = 0
 
    def rowCount(self, parent): 
        return len(self.arraydata) 
 
    def columnCount(self, parent): 
        return len(self.headerdata) 
 
    def data(self, index, role): 
        if not index.isValid(): 
            return QVariant() 
        elif role != Qt.DisplayRole: 
            return QVariant() 
        return QVariant(self.arraydata[index.row()][index.column()]) 

    def headerData(self, columnIdx, orientation, role):
        if orientation == Qt.Horizontal and role == Qt.DisplayRole:
            return QVariant(self.headerdata[columnIdx])
        return QVariant()

    def addMessage(self,  source,  message):
        self.emit(SIGNAL("layoutAboutToBeChanged()"))
        self.lastMsgId += 1
        self.arraydata.append([QTime.currentTime(),  source,  message,  self.lastMsgId])
        self.emit(SIGNAL("layoutChanged()"))       
        return self.lastMsgId

    def sort(self, Ncol, order):
        """Sort table by given column number.
        """
        self.emit(SIGNAL("layoutAboutToBeChanged()"))
        self.arraydata = sorted(self.arraydata, key=operator.itemgetter(Ncol))        
        if order == Qt.DescendingOrder:
            self.arraydata.reverse()
        self.emit(SIGNAL("layoutChanged()"))
