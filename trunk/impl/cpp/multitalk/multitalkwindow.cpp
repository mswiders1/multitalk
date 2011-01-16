#include "multitalkwindow.h"
#include "ui_multitalkwindow.h"
#include "connectdialog.h"
#include <QNetworkInterface>
#include <QList>
#include <QCryptographicHash>

MultitalkWindow::MultitalkWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MultitalkWindow)
{
    ui->setupUi(this);
    statusBarLabel=new QLabel(this);
    statusBarLabel->setText("Not Connected");
    statusBar()->addPermanentWidget(statusBarLabel);
    broadcast=new Broadcast(this);
    connect(this,SIGNAL(connectToNetworkAccepted()),broadcast,SLOT(sendBroadcast()));
    QList<QNetworkInterface> interfaces=QNetworkInterface::allInterfaces();
    QList<QNetworkInterface>::const_iterator i;
    for(i=interfaces.constBegin();i!=interfaces.constEnd();++i)
    {
         if((i->flags()|QNetworkInterface::IsPointToPoint|QNetworkInterface::CanMulticast)==(QNetworkInterface::IsUp|QNetworkInterface::IsRunning|QNetworkInterface::CanBroadcast|QNetworkInterface::IsPointToPoint|QNetworkInterface::CanMulticast))
            break;
    }
    macAddress=i->hardwareAddress();
    ipAddress=i->addressEntries().begin()->ip().toString();
    qDebug()<<macAddress<<ipAddress;

}

MultitalkWindow::~MultitalkWindow()
{
    delete ui;
    //delete statusBarLabel;
   // delete broadcast;
}

void MultitalkWindow::connectToNetwork()
{
    qDebug()<<"connectToNetworkCalled";
    ConnectDialog* connectDialog=new ConnectDialog(this);
    connectDialog->deleteLater();
    connect(connectDialog,SIGNAL(ipChanged(QString)),this,SLOT(setConnectIp(QString)));
    connect(connectDialog,SIGNAL(nickChanged(QString)),this,SLOT(setNick(QString)));
    if(!connectDialog->exec())
        qDebug()<<"cancel";
    else
        emit connectToNetworkAccepted();
}

void MultitalkWindow::setNick(QString newNick)
{
    nick=newNick;
    QString text=macAddress+ipAddress+nick;
    uid=QString(QCryptographicHash::hash(text.toAscii(),QCryptographicHash::Sha1).toBase64());
    qDebug()<<"uid:"<<uid;
}

void MultitalkWindow::setConnectIp(QString ip)
{
    connectIp=ip;
}
