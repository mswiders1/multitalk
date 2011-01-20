#ifndef MULTITALKWINDOW_H
#define MULTITALKWINDOW_H

#include <QMainWindow>
#include <QLabel>
#include <QLinkedList>
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
    virtual ~MultitalkWindow();
    QString username;
    QString uid;
    QString newUid;
    QList<UserData> users;
    QLinkedList<Message> messageHistory;
    QList<QList<int> > matrix;

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
    void storeMessage(Message msg);

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
    void sendMsgMessage(QString content,QString receiverUid);
    void sendMsgClicked();
    void sendMsgAllClicked();

};

#endif // MULTITALKWINDOW_H
