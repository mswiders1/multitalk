#include "tcpconnection.h"
#include <qjson/parser.h>
#include <qjson/serializer.h>
#include "message.h"

TcpConnection::TcpConnection(QObject *parent) :
    QTcpSocket(parent), headerRead(false)
{
    connect(this,SIGNAL(disconnected()),this,SLOT(connectionClosed()));
    connect(this,SIGNAL(readyRead()),this,SLOT(dataWaiting()));;
}

void TcpConnection::connectionClosed()
{
    emit connectionDisconnected(this);
}

void TcpConnection::dataWaiting()
{
    if(!headerRead)
    {
        if(canReadLine())
        {
            QByteArray line=readLine();
            qDebug()<<line;
            QString lineString(line);
            if(lineString.startsWith("BEGIN_MESSAGE:"))
            {
                qDebug()<<"message signature ok";
                QString numberOfBytesString=lineString.section(':',1,1);
                numberOfBytesToRead=numberOfBytesString.toInt();
                qDebug()<<"number of bytes to read:"<<numberOfBytesToRead;
                headerRead=true;
            }
            else
            {
                qDebug()<<"wrong message signature";
                numberOfBytesToRead=0;
            }
        }
    }

    if(headerRead)
    {
        if(bytesAvailable()>=numberOfBytesToRead)
        {
            QByteArray data=read(numberOfBytesToRead);
            qDebug()<<"data dump:"<<data<<":data dump end";
            parseMessage(data);
            headerRead=false;
            numberOfBytesToRead=0;
        }
    }
}

void TcpConnection::parseMessage(QByteArray& data)
{
    Message msg;
    QJson::Parser parser;
    bool ok;
    QVariantMap result = parser.parse(data,&ok).toMap();
    if(ok)
    {
        msg.type=result["TYPE"].toString();
        if(msg.type=="HII")
        {
            qDebug()<<"got HII message";
            clientUid=result["UID"].toString();

            msg.uid=clientUid;
            msg.username=result["USERNAME"].toString();

            /*
            QVariantMap packet;
            packet.insert("TYPE","LOG");
            packet.insert("UID",main->uid);
            packet.insert("USERNAME",main->nick);
            QJson::Serializer serializer;
            QByteArray packetArray=serializer.serialize(packet);
            QString header;
            QTextStream(&header)<<"BEGIN_MESSAGE:"<<packetArray.size()<<"\n";
            write(QByteArray(header.toAscii()));
            write(packetArray);*/
        }
        else if(msg.type=="LOG")
        {
            qDebug()<<"got LOG message";
            msg.uid=clientUid;
            msg.username=result["USERNAME"].toString();
            msg.ip_address=result["IP_ADDRESS"].toString();
        }
        else
        {
            qDebug()<<"WARNING:unknown message type, ignoring";
            return;
        }
        emit receivedMessageFromNetwork(msg);
    }
    else
        qDebug()<<"ERROR:failed to parse json";
}

void TcpConnection::sendMessageToNetwork(Message msg)
{
    QVariantMap packet;

    if(msg.type=="HII")
    {
        packet.insert("TYPE",msg.type);
        packet.insert("UID",msg.uid);
        packet.insert("USERNAME",msg.username);
        QList<UserData>::iterator i;
        QVariantList vector;
        for(i=msg.vector.begin();i!=msg.vector.end();++i)
        {
            QVariantMap userMap;
            userMap.insert("IP_ADDRESS",i->ip);
            userMap.insert("UID",i->uid);
            userMap.insert("USERNAME",i->username);
            vector.append(userMap);
        }
        packet.insert("VECTOR",vector);
    }
    else if(msg.type=="LOG")
    {
        packet.insert("TYPE",msg.type);
        packet.insert("UID",msg.uid);
        packet.insert("USERNAME",msg.username);
        packet.insert("IP_ADDRESS",msg.ip_address);
    } else
    {
        qDebug()<<"ERROR:bad message type to connect, dropping type:"<<msg.type;
        return;
    }
    QJson::Serializer serializer;
    QByteArray packetArray=serializer.serialize(packet);
    QString header;
    QTextStream(&header)<<"BEGIN_MESSAGE:"<<packetArray.size()<<"\n";
    write(QByteArray(header.toAscii()));
    write(packetArray);
    qDebug()<<"data dump send:"<<packetArray<<":data dump send end";
}
