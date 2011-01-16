#ifndef TCPSERVER_H
#define TCPSERVER_H

#include <QTcpServer>
#include <QList>
#include "tcpconnection.h"
#include "multitalkwindow.h"

class MultitalkWindow;
class TcpConnection;

class TcpServer : public QTcpServer
{
    Q_OBJECT
public:
    explicit TcpServer(QObject *parent,MultitalkWindow *main_);
    void connectToClient(QHostAddress address);
private:
    QList<TcpConnection*> connectionList;
    MultitalkWindow* main;
signals:

public slots:

private slots:
    void incomingConnection(int socketDescriptor);
    void disconnectedConnection(TcpConnection* connection);
};

#endif // TCPSERVER_H
