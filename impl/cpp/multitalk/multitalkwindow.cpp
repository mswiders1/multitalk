#include "multitalkwindow.h"
#include "ui_multitalkwindow.h"
#include "connectdialog.h"

MultitalkWindow::MultitalkWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MultitalkWindow)
{
    ui->setupUi(this);
    statusBarLabel=new QLabel();
    statusBarLabel->setText("Not Connected");
    statusBar()->addPermanentWidget(statusBarLabel);
}

MultitalkWindow::~MultitalkWindow()
{
    delete ui;
    delete statusBarLabel;
}

void MultitalkWindow::connectToNetwork()
{
    qDebug("connectToNetworkCalled");
    ConnectDialog* connectDialog=new ConnectDialog(this);
    if(!connectDialog->exec())
        qDebug("cancel");
    else
        qDebug("ok");
}

