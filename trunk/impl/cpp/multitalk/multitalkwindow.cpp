#include "multitalkwindow.h"
#include "ui_multitalkwindow.h"
#include "connectdialog.h"
#include <QNetworkInterface>
#include <QList>
#include <QCryptographicHash>
#include "userdata.h"

MultitalkWindow::MultitalkWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MultitalkWindow)
{
    ui->setupUi(this);
    statusBarLabel=new QLabel(this);
    statusBarLabel->setText("Not Connected");
    statusBar()->addPermanentWidget(statusBarLabel);
    broadcast=new Broadcast(this);
    connect(this,SIGNAL(connectToNetworkAccepted()),broadcast,SLOT(startListening()));
    connect(this,SIGNAL(connectToNetworkAccepted()),broadcast,SLOT(sendBroadcast()));
    connect(broadcast,SIGNAL(gotConnectionRequest(QHostAddress)),this,SLOT(connectToAddress(QHostAddress)));
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
    tcpServer=NULL;
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
    {
        if(tcpServer!=NULL)
            delete tcpServer;
        else
            tcpServer=new TcpServer(this,this);
        emit connectToNetworkAccepted();

    }
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

void MultitalkWindow::connectToAddress(QHostAddress address)
{
    tcpServer->connectToClient(address);
}

void MultitalkWindow::receiveNewClientMessage(QString uid, QString nick,QString ip)
{
    qDebug()<<"receiveNewClientMessage";
    QList<UserData>::iterator i;
    for(i=users.begin();i!=users.end();++i)
    {
        if(i->uid==uid)
            break;
    }
    if(i==users.end())
    {
        QListWidgetItem *newItem= new QListWidgetItem(ui->listWidget);
        newItem->setText(nick);
        ui->listWidget->addItem(newItem);
        UserData userData;
        userData.uid=uid;
        userData.nick=nick;
        userData.ip=ip;
        userData.item=newItem;
        users.append(userData);
    }
    else
        qDebug()<<"client already exists";
}

void MultitalkWindow::clientDisconnected(QString uid)
{
    qDebug()<<"removing disconnected client:"<<uid;
    QList<UserData>::iterator i;
    int pos=0;
    for(i=users.begin();i!=users.end();++i)
    {
        if(i->uid==uid)
            break;
        pos++;
    }
    if(i!=users.end())
    {
        qDebug()<<"removing:"<<pos;
        delete ui->listWidget->takeItem(pos);
        users.removeAt(pos);
    }
}
