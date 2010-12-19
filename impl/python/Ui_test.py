# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file '/mnt/duza/studia/sem8/SR/SR_PROJ/trunk/test.ui'
#
# Created: Fri Dec  3 10:25:02 2010
#      by: PyQt4 UI code generator 4.8.1
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    _fromUtf8 = lambda s: s

class Ui_Test(object):
    def setupUi(self, Test):
        Test.setObjectName(_fromUtf8("Test"))
        Test.resize(470, 388)
        self.verticalLayout_2 = QtGui.QVBoxLayout(Test)
        self.verticalLayout_2.setObjectName(_fromUtf8("verticalLayout_2"))
        self.verticalLayout = QtGui.QVBoxLayout()
        self.verticalLayout.setObjectName(_fromUtf8("verticalLayout"))
        self.styleEdit = QtGui.QPlainTextEdit(Test)
        self.styleEdit.setObjectName(_fromUtf8("styleEdit"))
        self.verticalLayout.addWidget(self.styleEdit)
        self.reload = QtGui.QPushButton(Test)
        self.reload.setObjectName(_fromUtf8("reload"))
        self.verticalLayout.addWidget(self.reload)
        self.verticalLayout_2.addLayout(self.verticalLayout)

        self.retranslateUi(Test)
        QtCore.QMetaObject.connectSlotsByName(Test)

    def retranslateUi(self, Test):
        Test.setWindowTitle(QtGui.QApplication.translate("Test", "Dialog", None, QtGui.QApplication.UnicodeUTF8))
        self.reload.setText(QtGui.QApplication.translate("Test", "Reload", None, QtGui.QApplication.UnicodeUTF8))


if __name__ == "__main__":
    import sys
    app = QtGui.QApplication(sys.argv)
    Test = QtGui.QDialog()
    ui = Ui_Test()
    ui.setupUi(Test)
    Test.show()
    sys.exit(app.exec_())

