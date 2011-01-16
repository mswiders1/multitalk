#ifndef BROADCAST_H
#define BROADCAST_H

#include <QtNetwork/QUdpSocket>

class Broadcast : public QUdpSocket
{
    Q_OBJECT
public:
    explicit Broadcast(QObject *parent = 0);

signals:
    void gotConnectionRequest(QHostAddress address);
private slots:
    void processDatagrams();

public slots:
    void sendBroadcast();
    void startListening();

};

#endif // BROADCAST_H
