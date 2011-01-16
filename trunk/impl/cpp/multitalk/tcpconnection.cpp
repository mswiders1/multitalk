#include "tcpconnection.h"
#include <qjson/parser.h>

TcpConnection::TcpConnection(QObject *parent, MultitalkWindow *main_) :
    QTcpSocket(parent), headerRead(false), main(main_)
{
    connect(this,SIGNAL(disconnected()),this,SLOT(connectionClosed()));
    connect(this,SIGNAL(readyRead()),this,SLOT(dataWaiting()));
    connect(this,SIGNAL(gotHIIMessage(QString,QString)),main,SLOT(receiveHIIMessage(QString,QString)));
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
                    emit gotHIIMessage(result["UID"].toString(),result["USERNAME"].toString());
                }
            }
            else
                qDebug()<<"failed to parse json";
            headerRead=false;
            numberOfBytesToRead=0;
        }
    }
}
