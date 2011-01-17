#include "tcpconnection.h"
#include <qjson/parser.h>
#include <qjson/serializer.h>

TcpConnection::TcpConnection(QObject *parent, MultitalkWindow *main_) :
    QTcpSocket(parent), headerRead(false), main(main_)
{
    connect(this,SIGNAL(disconnected()),this,SLOT(connectionClosed()));
    connect(this,SIGNAL(readyRead()),this,SLOT(dataWaiting()));
    connect(this,SIGNAL(gotHIIMessage(QString,QString,QString)),main,SLOT(receiveNewClientMessage(QString,QString,QString)));
    connect(this,SIGNAL(gotLOGMessage(QString,QString,QString)),main,SLOT(receiveNewClientMessage(QString,QString,QString)));
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
            QJson::Parser parser;
            bool ok;
            QVariantMap result = parser.parse(data,&ok).toMap();
            if(ok)
            {
                if(result["TYPE"].toString()=="HII")
                {
                    qDebug()<<"got HII message";
                    clientUid=result["UID"].toString();
                    emit gotHIIMessage(clientUid,result["USERNAME"].toString(),peerAddress().toString());

                    QVariantMap packet;
                    packet.insert("TYPE","LOG");
                    packet.insert("UID",main->uid);
                    packet.insert("USERNAME",main->nick);
                    QJson::Serializer serializer;
                    QByteArray packetArray=serializer.serialize(packet);
                    QString header;
                    QTextStream(&header)<<"BEGIN_MESSAGE:"<<packetArray.size()<<"\n";
                    write(QByteArray(header.toAscii()));
                    write(packetArray);



                }
                else if(result["TYPE"].toString()=="LOG")
                {
                    qDebug()<<"got LOG message";
                    clientUid=result["UID"].toString();
                    emit gotLOGMessage(clientUid,result["USERNAME"].toString(),peerAddress().toString());
                }
            }
            else
                qDebug()<<"failed to parse json";
            headerRead=false;
            numberOfBytesToRead=0;
        }
    }
}
