#include "tcpconnection.h"
#include <qjson/parser.h>
#include <qjson/serializer.h>
#include "message.h"
#include <QHostAddress>

Q_DECLARE_METATYPE(QList<int>)


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
            //qDebug()<<line;
            QString lineString(line);
            if(lineString.startsWith("BEGIN_MESSAGE:"))
            {
                //qDebug()<<"message signature ok";
                QString numberOfBytesString=lineString.section(':',1,1);
                numberOfBytesToRead=numberOfBytesString.toInt();
                //qDebug()<<"number of bytes to read:"<<numberOfBytesToRead;
                headerRead=true;
            }
            else
            {
                //qDebug()<<"wrong message signature";
                numberOfBytesToRead=0;
            }
        }
    }

    if(headerRead)
    {
        if(bytesAvailable()>=numberOfBytesToRead)
        {
            QByteArray data=read(numberOfBytesToRead);
            //qDebug()<<"data dump:"<<data<<":data dump end";
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
        qDebug()<<"received message from:"<<peerAddress()<<" type:"<<msg.type;
        if(msg.type=="HII")
        {
            //qDebug()<<"got HII message";
            clientUid=result["UID"].toString();
            msg.uid=clientUid;
            msg.username=result["USERNAME"].toString();
            foreach(QVariant item,result["VECTOR"].toList())
            {
                QVariantMap client=item.toMap();
                UserData user;
                user.ip=client["IP_ADDRESS"].toString();
                user.uid=client["UID"].toString();
                user.username=client["USERNAME"].toString();
                msg.vector.append(user);
            }
        }
        else if(msg.type=="LOG")
        {
            //qDebug()<<"got LOG message";
            msg.uid=result["UID"].toString();
            msg.username=result["USERNAME"].toString();
            msg.ip_address=result["IP_ADDRESS"].toString();
        }
        else if(msg.type=="OUT")
        {
            msg.uid=result["UID"].toString();
        } else if(msg.type=="LIV")
        {
            msg.uid=result["UID"].toString();
            msg.ip_address=result["IP_ADDRESS"].toString();
            msg.sequence=result["SEQUENCE"].toLongLong();
        } else if(msg.type=="MSG")
        {
            msg.sender=result["SENDER"].toString();
            if(result.contains("RECEIVER"))
                msg.receiver=result["RECEIVER"].toString();
            msg.msg_id=result["MSG_ID"].toInt();
            foreach(QVariant item,result["TIME_VEC"].toList())
            {
                msg.time_vec.append(item.toInt());
            }

            foreach(QVariant item,result["VEC"].toList())
            {
                msg.vec.append(item.toString());
            }
            msg.content=result["CONTENT"].toString();
        } else if(msg.type=="P2P")
        {
            msg.peerAddress=peerAddress();
        } else if(msg.type=="MTX")
        {

        } else
        {
            qDebug()<<"WARNING:unknown message type, ignoring";
            qDebug()<<"__________________MESSAGE DUMP____________________";
            qDebug()<<data;
            qDebug()<<"________________END MESSAGE DUMP__________________";
            return;
        }
        emit receivedMessageFromNetwork(msg);
    }
    else
        qDebug()<<"ERROR:failed to parse json";
}

void TcpConnection::sendMessageToNetwork(Message msg)
{
    qDebug()<<"sending message to:"<<peerAddress()<<" type:"<<msg.type;
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
    } else if(msg.type=="LIV")
    {
        packet.insert("TYPE",msg.type);
        packet.insert("UID",msg.uid);
        packet.insert("IP_ADDRESS",msg.ip_address);
        packet.insert("SEQUENCE",QString().setNum(msg.sequence));
    } else if(msg.type=="OUT")
    {
        packet.insert("TYPE",msg.type);
        packet.insert("UID",msg.uid);
    } else if(msg.type=="MSG")
    {
        packet.insert("TYPE",msg.type);
        packet.insert("SENDER",msg.sender);
        //if(msg.receiver!="")
            packet.insert("RECEIVER",msg.receiver);
        packet.insert("MSG_ID",msg.msg_id);
        QVariantList time_vec;
        for(int i=0;i<msg.time_vec.size();i++)
            time_vec.append(msg.time_vec[i]);
        packet.insert("TIME_VEC",time_vec);
        QVariantList vec;
        for(int i=0;i<msg.vec.size();i++)
            vec.append(msg.vec[i]);
        packet.insert("VEC",vec);
        packet.insert("CONTENT",msg.content);
    } else
    {
        qDebug()<<"ERROR:bad message type to send, dropping type:"<<msg.type;
        return;
    }
    QJson::Serializer serializer;
    QByteArray packetArray=serializer.serialize(packet);
    QString header;
    QTextStream(&header)<<"BEGIN_MESSAGE:"<<packetArray.size()<<"\n";
    write(QByteArray(header.toAscii()));
    write(packetArray);
    if(state()==QAbstractSocket::ConnectedState)
        flush();
    qDebug()<<"data dump send:"<<packetArray<<":data dump send end";
}
