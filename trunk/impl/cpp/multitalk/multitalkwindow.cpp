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
    broadcast=new Broadcast(this);
    connect(this,SIGNAL(connectToNetworkAccepted()),broadcast,SLOT(sendBroadcast()));
}

MultitalkWindow::~MultitalkWindow()
{
    delete ui;
    delete statusBarLabel;
    delete broadcast;
}

void MultitalkWindow::connectToNetwork()
{
    qDebug()<<"connectToNetworkCalled";
    ConnectDialog* connectDialog=new ConnectDialog(this);
    connectDialog->deleteLater();
    if(!connectDialog->exec())
        qDebug()<<"cancel";
    else
        emit connectToNetworkAccepted();
}

