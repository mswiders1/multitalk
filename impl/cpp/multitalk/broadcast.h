#ifndef BROADCAST_H
#define BROADCAST_H

#include <QtNetwork/QUdpSocket>

class Broadcast : public QUdpSocket
{
    Q_OBJECT
public:
    explicit Broadcast(QObject *parent = 0);

signals:
private slots:
    void processDatagrams();

public slots:
    void sendBroadcast();

};

#endif // BROADCAST_H
