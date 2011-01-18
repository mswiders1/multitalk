#include "tcpserver.h"
#include <QTcpSocket>
#include <QVariant>
#include <qjson/serializer.h>
#include <QTextStream>

TcpServer::TcpServer(QObject *parent) :
    QTcpServer(parent)
{
    if(!listen(QHostAddress::Any,3554))
        qDebug()<<"unable to Listen on port 3554";
    else
    {
        qDebug()<<"listening on port 3554";
        //connect(this,SIGNAL(newConnection()),this,SLOT(incomingConnection()));
    }
}

void TcpServer::incomingConnection(int socketDescriptor)
{
    TcpConnection *clientConnection=new TcpConnection(this);
    connectionList.append(clientConnection);
    clientConnection->setSocketDescriptor(socketDescriptor);
    connect(this,SIGNAL(sendMessageToNetwork(Message)),clientConnection,SLOT(sendMessageToNetwork(Message)));
    connect(clientConnection, SIGNAL(disconnected()),clientConnection, SLOT(deleteLater()));
    connect(clientConnection,SIGNAL(connectionDisconnected(TcpConnection*)),this,SLOT(disconnectedConnection(TcpConnection*)));
    connect(clientConnection,SIGNAL(receivedMessageFromNetwork(Message)),this,SIGNAL(receivedMessageFromNetwork(Message)));
    qDebug()<<"client connected:"<<clientConnection->peerAddress();
    //qDebug()<<"socket descriptor:"<<socketDescriptor;
}

void TcpServer::disconnectedConnection(TcpConnection *connection)
{
    qDebug()<<"removing disconnected connection";
    emit clientDisconnected(connection->clientUid);
    connectionList.removeOne(connection);
}

void TcpServer::connectToClient(QHostAddress address,Message msg)
{
    QList<TcpConnection*>::iterator i;
    for(i=connectionList.begin();i!=connectionList.end();i++)
    {
        TcpConnection *conn=*i;
        if(conn->peerAddress()==address)
        {
            qDebug()<<"already connected to this address:"<<address;
            return;
        }
    }

    TcpConnection *clientConnection=new TcpConnection(this);
    connectionList.append(clientConnection);
    clientConnection->connectToHost(address,3554);
    connect(clientConnection,SIGNAL(receivedMessageFromNetwork(Message)),this,SIGNAL(receivedMessageFromNetwork(Message)));
    clientConnection->sendMessageToNetwork(msg);
    //clientConnection->write(QByteArray("test\n"));*/
    connect(clientConnection, SIGNAL(disconnected()),clientConnection, SLOT(deleteLater()));
    connect(clientConnection,SIGNAL(connectionDisconnected(TcpConnection*)),this,SLOT(disconnectedConnection(TcpConnection*)));
    connect(this,SIGNAL(sendMessageToNetwork(Message)),clientConnection,SLOT(sendMessageToNetwork(Message)));
    qDebug()<<"connecting to:"<<address;
}

