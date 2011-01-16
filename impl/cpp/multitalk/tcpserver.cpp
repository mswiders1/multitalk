#include "tcpserver.h"
#include <QTcpSocket>
#include <QVariant>
#include <qjson/serializer.h>
#include <QTextStream>

TcpServer::TcpServer(QObject *parent,MultitalkWindow *main_) :
    QTcpServer(parent),main(main_)
{
    connect(this,SIGNAL(clientDisconnected(QString)),main,SLOT(clientDisconnected(QString)));
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
    TcpConnection *clientConnection=new TcpConnection(this,main);
    connectionList.append(clientConnection);
    clientConnection->setSocketDescriptor(socketDescriptor);
    connect(clientConnection, SIGNAL(disconnected()),clientConnection, SLOT(deleteLater()));
    connect(clientConnection,SIGNAL(connectionDisconnected(TcpConnection*)),this,SLOT(disconnectedConnection(TcpConnection*)));
    qDebug()<<"client connected:"<<clientConnection->peerAddress();
    qDebug()<<"socket descriptor:"<<socketDescriptor;
}

void TcpServer::disconnectedConnection(TcpConnection *connection)
{
    qDebug()<<"removing disconnected connection";
    emit clientDisconnected(connection->clientUid);
    connectionList.removeOne(connection);
}

void TcpServer::connectToClient(QHostAddress address)
{
    TcpConnection *clientConnection=new TcpConnection(this,main);
    connectionList.append(clientConnection);
    clientConnection->connectToHost(address,3554);
    QVariantMap packet;
    packet.insert("TYPE","HII");
    packet.insert("UID",main->uid);
    packet.insert("USERNAME",main->nick);
    QVariantList vector;
    packet.insert("VECTOR",vector);
    QJson::Serializer serializer;
    QByteArray packetArray=serializer.serialize(packet);
    QString header;
    QTextStream(&header)<<"BEGIN_MESSAGE:"<<packetArray.size()<<"\n";
    clientConnection->write(QByteArray(header.toAscii()));
    clientConnection->write(packetArray);

    //clientConnection->write(QByteArray("test\n"));
    connect(clientConnection, SIGNAL(disconnected()),clientConnection, SLOT(deleteLater()));
    connect(clientConnection,SIGNAL(connectionDisconnected(TcpConnection*)),this,SLOT(disconnectedConnection(TcpConnection*)));
    qDebug()<<"connect to:"<<address;
}
