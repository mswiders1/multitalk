#ifndef TCPCONNECTION_H
#define TCPCONNECTION_H

#include <QTcpSocket>
#include "multitalkwindow.h"
#include "message.h"

class MultitalkWindow;

class TcpConnection : public QTcpSocket
{
    Q_OBJECT
public:
    explicit TcpConnection(QObject *parent);
    QString clientUid;
private:
    bool headerRead;
    int numberOfBytesToRead;
    MultitalkWindow *main;
    void parseMessage(QByteArray& data);
signals:
    void connectionDisconnected(TcpConnection* connection);
    void receivedMessageFromNetwork(Message msg);
public slots:
    void connectionClosed();
    void sendMessageToNetwork(Message msg);
private slots:
    void dataWaiting();

};

#endif // TCPCONNECTION_H
