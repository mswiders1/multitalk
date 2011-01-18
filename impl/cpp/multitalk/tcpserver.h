#ifndef TCPSERVER_H
#define TCPSERVER_H

#include <QTcpServer>
#include <QList>
#include "tcpconnection.h"
#include "multitalkwindow.h"
#include "message.h"

class MultitalkWindow;
class TcpConnection;

class TcpServer : public QTcpServer
{
    Q_OBJECT
public:
    explicit TcpServer(QObject *parent);
    void connectToClient(QHostAddress address,Message msg);
private:
    QList<TcpConnection*> connectionList;
signals:
    void clientDisconnected(QString uid);
    void receivedMessageFromNetwork(Message msg);
    void sendMessageToNetwork(Message msg);
public slots:

private slots:
    void incomingConnection(int socketDescriptor);
    void disconnectedConnection(TcpConnection* connection);
};

#endif // TCPSERVER_H
