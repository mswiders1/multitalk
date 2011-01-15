#include "broadcast.h"
#include <QtNetwork/QUdpSocket>

Broadcast::Broadcast(QObject *parent) :
    QUdpSocket(parent)
{
    bind(3554,QUdpSocket::ShareAddress);
    connect(this,(SIGNAL(readyRead())),this,SLOT(processDatagrams()));
}

void Broadcast::sendBroadcast()
{
    QByteArray datagram = "MULTITALK_5387132";
    writeDatagram(datagram.data(),datagram.size(),QHostAddress::Broadcast,3554);
    qDebug()<<"broadcast message sent";
}

void Broadcast::processDatagrams()
{
    while (hasPendingDatagrams()) {
             QByteArray datagram;
             QHostAddress address;
             datagram.resize(pendingDatagramSize());
             readDatagram(datagram.data(), datagram.size(),&address);
             qDebug()<<"Received broadcast:"<<datagram<<" "<<address;
    }
}
