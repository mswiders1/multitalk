#ifndef MULTITALKWINDOW_H
#define MULTITALKWINDOW_H

#include <QMainWindow>
#include <QLabel>
#include "connectdialog.h"
#include "broadcast.h"
#include "tcpserver.h"
#include "userdata.h"
#include "message.h"



namespace Ui {
    class MultitalkWindow;
}

class MultitalkWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MultitalkWindow(QWidget *parent = 0);
    ~MultitalkWindow();
    QString username;
    QString uid;
    QList<UserData> users;
private:
    Ui::MultitalkWindow *ui;
    QLabel* statusBarLabel;
    Broadcast* broadcast;
    QString ipAddress;
    QString macAddress;
    QString connectIp;
    TcpServer* tcpServer;
    long livSequence;
    QTimer* livTimer;


signals:
    void connectToNetworkAccepted();
    void sendMessageToNetwork(Message msg);

private slots:
    void connectToNetwork();
    void setConnectIp(QString ip);
    void setNick(QString newNick);
    void connectToAddress(QHostAddress address);
    void handleReceivedMessage(Message msg);
    void clientDisconnected(QString uid);
    void sendLogMessage();
    void sendLivMessage();
    void sendOutMessage();
};

#endif // MULTITALKWINDOW_H
