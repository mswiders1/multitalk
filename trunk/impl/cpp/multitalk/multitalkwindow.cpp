#include "multitalkwindow.h"
#include "ui_multitalkwindow.h"
#include "connectdialog.h"
#include <QNetworkInterface>
#include <QList>
#include <QCryptographicHash>
#include "userdata.h"
#include <QTimer>

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
        {
            tcpServer=new TcpServer(this);
            connect(tcpServer,SIGNAL(receivedMessageFromNetwork(Message)),this,SLOT(handleReceivedMessage(Message)));
            connect(tcpServer,SIGNAL(clientDisconnected(QString)),this,SLOT(clientDisconnected(QString)));
        }
        emit connectToNetworkAccepted();

        QTimer::singleShot(5000,this,SLOT(sendLogMessage()));
        QTimer::singleShot(1000,broadcast,SLOT(sendBroadcast()));
        QTimer::singleShot(2000,broadcast,SLOT(sendBroadcast()));
        connect(this,SIGNAL(sendMessageToNetwork(Message)),tcpServer,SIGNAL(sendMessageToNetwork(Message)));
    }
}

void MultitalkWindow::setNick(QString newNick)
{
    username=newNick;
    QString text=macAddress+ipAddress+username;
    uid=QString(QCryptographicHash::hash(text.toAscii(),QCryptographicHash::Sha1).toBase64());
    qDebug()<<"uid:"<<uid;
}

void MultitalkWindow::setConnectIp(QString ip)
{
    connectIp=ip;
}

void MultitalkWindow::connectToAddress(QHostAddress address)
{
    Message msg;
    msg.type="HII";
    msg.uid=uid;
    msg.username=username;
    msg.vector=users;
    tcpServer->connectToClient(address,msg);
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

void MultitalkWindow::handleReceivedMessage(Message msg)
{
    qDebug()<<"Multitalkwindow got message type:"<<msg.type;
    if(msg.type=="HII")
    {
        QList<UserData>::iterator myList;
        QList<UserData>::iterator remoteList;
        for(remoteList=msg.vector.begin();remoteList!=msg.vector.end();remoteList++)
        {
            qDebug()<<"got hii user:"<<remoteList->username;
            for(myList=users.begin();myList!=users.end();myList++)
            {
                if(myList->uid==remoteList->uid)
                    break;
            }
            if(myList==users.end())
            {
                QListWidgetItem *newItem= new QListWidgetItem(ui->listWidget);
                newItem->setText(remoteList->username);
                users.append(*remoteList);
            }
        }
    } else if(msg.type=="LOG")
    {
        qDebug()<<"uid:"<<msg.uid<<" username:"<<msg.username;
        QList<UserData>::iterator i;
        for(i=users.begin();i!=users.end();++i)
        {
            if(i->uid==msg.uid)
                break;
        }
        if(i==users.end())
        {
            QListWidgetItem *newItem= new QListWidgetItem(ui->listWidget);
            newItem->setText(msg.username);
            ui->listWidget->addItem(newItem);
            UserData userData;
            userData.uid=msg.uid;
            userData.username=msg.username;
            userData.ip=msg.ip_address;
            users.append(userData);
        }
        else
            qDebug()<<"client already exists";
    }
}

void MultitalkWindow::sendLogMessage()
{
    Message msg;
    msg.type="LOG";
    msg.uid=uid;
    msg.username=username;
    msg.ip_address=ipAddress;
    emit sendMessageToNetwork(msg);
}
