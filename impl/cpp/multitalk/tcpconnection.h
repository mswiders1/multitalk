#ifndef TCPCONNECTION_H
#define TCPCONNECTION_H

#include <QTcpSocket>
#include "multitalkwindow.h"

class MultitalkWindow;

class TcpConnection : public QTcpSocket
{
    Q_OBJECT
public:
    explicit TcpConnection(QObject *parent,MultitalkWindow *main_);
private:
    QString clientUid;
    bool headerRead;
    int numberOfBytesToRead;
    MultitalkWindow *main;

signals:
    void connectionDisconnected(TcpConnection* connection);
    void gotHIIMessage(QString uid,QString nick);
public slots:
    void connectionClosed();
private slots:
    void dataWaiting();
};

#endif // TCPCONNECTION_H
