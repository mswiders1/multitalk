#ifndef MULTITALKWINDOW_H
#define MULTITALKWINDOW_H

#include <QMainWindow>
#include <QLabel>
#include "connectdialog.h"
#include "broadcast.h"
#include "tcpserver.h"
#include "userdata.h"

namespace Ui {
    class MultitalkWindow;
}

class TcpServer;

class MultitalkWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MultitalkWindow(QWidget *parent = 0);
    ~MultitalkWindow();
    QString nick;
    QString uid;
private:
    Ui::MultitalkWindow *ui;
    QLabel* statusBarLabel;
    Broadcast* broadcast;
    QString ipAddress;
    QString macAddress;
    QString connectIp;
    TcpServer* tcpServer;
    QList<UserData> users;
signals:
    void connectToNetworkAccepted();

private slots:
    void connectToNetwork();
    void setConnectIp(QString ip);
    void setNick(QString newNick);
    void connectToAddress(QHostAddress address);
    void receiveHIIMessage(QString uid,QString nick);
    void clientDisconnected(QString uid);
};

#endif // MULTITALKWINDOW_H
