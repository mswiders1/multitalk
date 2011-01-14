/********************************************************************************
** Form generated from reading UI file 'multitalkwindow.ui'
**
** Created: Sat Jan 15 00:08:00 2011
**      by: Qt User Interface Compiler version 4.7.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_MULTITALKWINDOW_H
#define UI_MULTITALKWINDOW_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QHeaderView>
#include <QtGui/QListView>
#include <QtGui/QMainWindow>
#include <QtGui/QMenu>
#include <QtGui/QMenuBar>
#include <QtGui/QPlainTextEdit>
#include <QtGui/QPushButton>
#include <QtGui/QStatusBar>
#include <QtGui/QTextEdit>
#include <QtGui/QToolBar>
#include <QtGui/QWidget>

QT_BEGIN_NAMESPACE

class Ui_MultitalkWindow
{
public:
    QAction *actionConnect;
    QAction *actionClose;
    QWidget *centralWidget;
    QPlainTextEdit *plainTextEdit;
    QTextEdit *textEdit;
    QListView *listView;
    QPushButton *pushButton;
    QMenuBar *menuBar;
    QMenu *menuMultitalk;
    QToolBar *mainToolBar;
    QStatusBar *statusBar;

    void setupUi(QMainWindow *MultitalkWindow)
    {
        if (MultitalkWindow->objectName().isEmpty())
            MultitalkWindow->setObjectName(QString::fromUtf8("MultitalkWindow"));
        MultitalkWindow->resize(610, 318);
        actionConnect = new QAction(MultitalkWindow);
        actionConnect->setObjectName(QString::fromUtf8("actionConnect"));
        actionClose = new QAction(MultitalkWindow);
        actionClose->setObjectName(QString::fromUtf8("actionClose"));
        centralWidget = new QWidget(MultitalkWindow);
        centralWidget->setObjectName(QString::fromUtf8("centralWidget"));
        plainTextEdit = new QPlainTextEdit(centralWidget);
        plainTextEdit->setObjectName(QString::fromUtf8("plainTextEdit"));
        plainTextEdit->setGeometry(QRect(10, 0, 371, 171));
        textEdit = new QTextEdit(centralWidget);
        textEdit->setObjectName(QString::fromUtf8("textEdit"));
        textEdit->setGeometry(QRect(10, 180, 371, 31));
        listView = new QListView(centralWidget);
        listView->setObjectName(QString::fromUtf8("listView"));
        listView->setGeometry(QRect(400, 0, 191, 211));
        pushButton = new QPushButton(centralWidget);
        pushButton->setObjectName(QString::fromUtf8("pushButton"));
        pushButton->setGeometry(QRect(10, 220, 581, 31));
        MultitalkWindow->setCentralWidget(centralWidget);
        menuBar = new QMenuBar(MultitalkWindow);
        menuBar->setObjectName(QString::fromUtf8("menuBar"));
        menuBar->setGeometry(QRect(0, 0, 610, 25));
        menuMultitalk = new QMenu(menuBar);
        menuMultitalk->setObjectName(QString::fromUtf8("menuMultitalk"));
        MultitalkWindow->setMenuBar(menuBar);
        mainToolBar = new QToolBar(MultitalkWindow);
        mainToolBar->setObjectName(QString::fromUtf8("mainToolBar"));
        MultitalkWindow->addToolBar(Qt::TopToolBarArea, mainToolBar);
        statusBar = new QStatusBar(MultitalkWindow);
        statusBar->setObjectName(QString::fromUtf8("statusBar"));
        MultitalkWindow->setStatusBar(statusBar);

        menuBar->addAction(menuMultitalk->menuAction());
        menuMultitalk->addAction(actionConnect);
        menuMultitalk->addAction(actionClose);

        retranslateUi(MultitalkWindow);
        QObject::connect(actionConnect, SIGNAL(activated()), MultitalkWindow, SLOT(connectToNetwork()));
        QObject::connect(actionClose, SIGNAL(activated()), MultitalkWindow, SLOT(close()));

        QMetaObject::connectSlotsByName(MultitalkWindow);
    } // setupUi

    void retranslateUi(QMainWindow *MultitalkWindow)
    {
        MultitalkWindow->setWindowTitle(QApplication::translate("MultitalkWindow", "MultitalkWindow", 0, QApplication::UnicodeUTF8));
#ifndef QT_NO_STATUSTIP
        MultitalkWindow->setStatusTip(QString());
#endif // QT_NO_STATUSTIP
        actionConnect->setText(QApplication::translate("MultitalkWindow", "Connect", 0, QApplication::UnicodeUTF8));
        actionClose->setText(QApplication::translate("MultitalkWindow", "Close", 0, QApplication::UnicodeUTF8));
        pushButton->setText(QApplication::translate("MultitalkWindow", "Send message", 0, QApplication::UnicodeUTF8));
        menuMultitalk->setTitle(QApplication::translate("MultitalkWindow", "File", 0, QApplication::UnicodeUTF8));
    } // retranslateUi

};

namespace Ui {
    class MultitalkWindow: public Ui_MultitalkWindow {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MULTITALKWINDOW_H
