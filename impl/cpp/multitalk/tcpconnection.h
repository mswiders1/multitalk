#ifndef TCPCONNECTION_H
#define TCPCONNECTION_H

#include <QTcpSocket>
#include <QHostAddress>
#include "message.h"

class MultitalkWindow;

class TcpConnection : public QTcpSocket
{
    Q_OBJECT
public:
    explicit TcpConnection(QObject *parent);
    QString clientUid;
    QHostAddress connectAddress;
private:
    bool headerRead;
    int numberOfBytesToRead;
    void parseMessage(QByteArray& data);
signals:
    void connectionDisconnected(TcpConnection* connection);
    void receivedMessageFromNetwork(Message msg);
public slots:
    void connectionClosed();
    void sendMessageToNetwork(Message msg);
    void connectionError(QAbstractSocket::SocketError error);
private slots:
    void dataWaiting();

};

#endif // TCPCONNECTION_H
