#include "multitalkwindow.h"
#include "ui_multitalkwindow.h"
#include "connectdialog.h"
#include <QNetworkInterface>
#include <QList>
#include <QCryptographicHash>
#include "userdata.h"
#include <QTimer>


MultitalkWindow::MultitalkWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MultitalkWindow)
{
    ui->setupUi(this);
    ui->sendMsg->setEnabled(false);
    ui->sendMsgAll->setEnabled(false);
    statusBarLabel=new QLabel(this);
    statusBarLabel->setText("Not Connected");
    statusBar()->addPermanentWidget(statusBarLabel);
    broadcast=new Broadcast(this);
    connect(this,SIGNAL(connectToNetworkAccepted()),broadcast,SLOT(startListening()));
    //connect(this,SIGNAL(connectToNetworkAccepted()),broadcast,SLOT(sendBroadcast()));
    connect(broadcast,SIGNAL(gotConnectionRequest(QHostAddress)),this,SLOT(connectToAddress(QHostAddress)));
    connect(this,SIGNAL(sendMessageToNetwork(Message)),this,SLOT(handleReceivedMessage(Message)));
    connect(ui->sendMsgAll,SIGNAL(clicked()),this,SLOT(sendMsgAllClicked()));
    connect(ui->sendMsg,SIGNAL(clicked()),this,SLOT(sendMsgClicked()));
    QList<QNetworkInterface> interfaces=QNetworkInterface::allInterfaces();
    QList<QNetworkInterface>::const_iterator i;
    for(i=interfaces.constBegin();i!=interfaces.constEnd();++i)
    {
         if((i->flags()|QNetworkInterface::IsPointToPoint|QNetworkInterface::CanMulticast)==(QNetworkInterface::IsUp|QNetworkInterface::IsRunning|QNetworkInterface::CanBroadcast|QNetworkInterface::IsPointToPoint|QNetworkInterface::CanMulticast))
            break;
    }
    macAddress=i->hardwareAddress();
    ipAddress=i->addressEntries().begin()->ip().toString();
    qDebug()<<macAddress<<ipAddress;
    tcpServer=NULL;
    livSequence=0;
}

MultitalkWindow::~MultitalkWindow()
{
    sendOutMessage();
    delete ui;
    //delete statusBarLabel;
   // delete broadcast;
}

void MultitalkWindow::connectToNetwork()
{
    qDebug()<<"connectToNetworkCalled";
    ConnectDialog* connectDialog=new ConnectDialog(this);
    connectDialog->deleteLater();
    connect(connectDialog,SIGNAL(ipChanged(QString)),this,SLOT(setConnectIp(QString)));
    connect(connectDialog,SIGNAL(nickChanged(QString)),this,SLOT(setNick(QString)));
    if(!connectDialog->exec())
        qDebug()<<"cancel";
    else
    {
        if(tcpServer!=NULL)
        {
            ui->sendMsg->setEnabled(false);
            ui->sendMsgAll->setEnabled(false);
            statusBarLabel->setText("Not Connected");
            sendOutMessage();
            delete tcpServer;
        }
        users.clear();
        matrix.clear();
        ui->listWidget->clear();
        messageHistory.clear();
        uid=newUid;
        tcpServer=new TcpServer(this);
        connect(tcpServer,SIGNAL(receivedMessageFromNetwork(Message)),this,SLOT(handleReceivedMessage(Message)));
        connect(tcpServer,SIGNAL(clientDisconnected(QString)),this,SLOT(clientDisconnected(QString)));
        QTimer::singleShot(5000,this,SLOT(sendLogMessage()));
        if(connectIp!="")
        {
            connectToAddressP2P(QHostAddress(connectIp));
        }
        else
        {
            QTimer::singleShot(0,broadcast,SLOT(sendBroadcast()));
            QTimer::singleShot(1000,broadcast,SLOT(sendBroadcast()));
            QTimer::singleShot(2000,broadcast,SLOT(sendBroadcast()));
        }
        connect(this,SIGNAL(sendMessageToNetwork(Message)),tcpServer,SIGNAL(sendMessageToNetwork(Message)));
        livTimer=new QTimer(this);
        emit connectToNetworkAccepted();
    }
}

void MultitalkWindow::setNick(QString newNick)
{
    username=newNick;
    QString text=macAddress+ipAddress+username;
    newUid=QString(QCryptographicHash::hash(text.toAscii(),QCryptographicHash::Sha1).toBase64());
    qDebug()<<"uid:"<<uid;
}

void MultitalkWindow::setConnectIp(QString ip)
{
    connectIp=ip;
}

void MultitalkWindow::connectToAddress(QHostAddress address)
{
    if(address==QHostAddress(ipAddress))
    {
        qDebug()<<"not connecting to myself";
        return;
    }
    Message msg;
    msg.type="HII";
    msg.uid=uid;
    msg.username=username;
    msg.vector=users;
    tcpServer->connectToClient(address,msg);
}

void MultitalkWindow::connectToAddressP2P(QHostAddress address)
{
    if(address==QHostAddress(ipAddress))
    {
        qDebug()<<"not connecting to myself";
        return;
    }
    Message msg;
    msg.type="P2P";
    tcpServer->connectToClient(address,msg);
}

void MultitalkWindow::clientDisconnected(QString uid)
{
    qDebug()<<"removing disconnected client:"<<uid;
    QList<UserData>::iterator i;
    int pos=0;
    for(i=users.begin();i!=users.end();++i)
    {
        if(i->uid==uid)
            break;
        pos++;
    }
    if(i!=users.end())
    {
        qDebug()<<"removing:"<<pos;
        QListWidgetItem* item=ui->listWidget->takeItem(pos);
        if(item!=0)
            delete item;
        users.removeAt(pos);
        matrix.removeAt(pos);
        for(int i=0;i<matrix.size();i++)
            matrix[i].removeAt(pos);
    }

    QLinkedList<Message>::iterator msg;
    for(msg=messageHistory.begin();msg!=messageHistory.end();msg++)
    {
        if(msg->uid==uid&&msg->type=="LOG")
            break;
    }
    if(msg!=messageHistory.end())
        messageHistory.removeOne(*msg);
}

void MultitalkWindow::handleReceivedMessage(Message msg)
{
    if(messageHistory.contains(msg))
    {
        if(msg.type!="LIV")
            qDebug()<<"already got this message, ignoring type:"<<msg.type;
        return;
    }
    else
    {
        if(msg.type!="HII"&&msg.type!="MTX"&&msg.type!="P2P")
        {
            storeMessage(msg);
            if((msg.type=="LOG"||msg.type=="LIV")&&msg.uid==uid)
            {
                qDebug()<<"not resending message - message from myself";
            }
            else
            {
                emit sendMessageToNetwork(msg);
            }
        }
    }
    if(msg.type!="LIV")
        qDebug()<<"Multitalkwindow got message type:"<<msg.type;
    if(msg.type=="HII")
    {
        QList<UserData>::iterator myList;
        QList<UserData>::iterator remoteList;
        for(remoteList=msg.vector.begin();remoteList!=msg.vector.end();remoteList++)
        {
            qDebug()<<"got hii user:"<<remoteList->username;
            for(myList=users.begin();myList!=users.end();myList++)
            {
                if(myList->uid==remoteList->uid)
                    break;
            }
            if(myList==users.end())
            {
                QListWidgetItem *newItem= new QListWidgetItem(ui->listWidget);
                newItem->setText(remoteList->username);
                ui->listWidget->setCurrentItem(newItem);
                users.append(*remoteList);
                for(int i=0;i<matrix.size();i++)
                {
                    matrix[i].append(0);
                }
                QList<int> newVector;
                for(int i=0;i<users.size();i++)
                    newVector.append(0);
                matrix.append(newVector);
                connectToAddressP2P(QHostAddress(remoteList->ip));
            }
        }
    } else if(msg.type=="LOG")
    {
        QList<UserData>::iterator i;
        for(i=users.begin();i!=users.end();++i)
        {
            if(i->uid==msg.uid)
                break;
        }
        if(i==users.end())
        {
            QListWidgetItem *newItem= new QListWidgetItem(ui->listWidget);
            newItem->setText(msg.username);
            ui->listWidget->addItem(newItem);
            UserData userData;
            userData.uid=msg.uid;
            userData.username=msg.username;
            userData.ip=msg.ip_address;
            int myPos=-1;
            for(int i=0;i<users.size();i++)
            {
                if(users[i].uid==uid)
                    myPos=i;
            }
            users.append(userData);

            for(int i=0;i<matrix.size();i++)
            {
                matrix[i].append(0);
            }

            QList<int> newVector;


            for(int i=0;i<users.size();i++)
            {
                if(myPos!=-1)
                    newVector.append(matrix[myPos][i]);
                else
                    newVector.append(0);
            }


            matrix.append(newVector);
            qDebug()<<"MATRIX:";
            qDebug()<<matrix;
        }
        else
            qDebug()<<"client already exists";

        QLinkedList<Message>::iterator msgHist;
        for(msgHist=messageHistory.begin();msgHist!=messageHistory.end();msgHist++)
        {
            if(msgHist->uid==msg.uid&&msgHist->type=="OUT")
                break;
        }
        if(msgHist!=messageHistory.end())
            messageHistory.removeOne(*msgHist);

        if(msg.peerAddress==QHostAddress(msg.ip_address))
        {
            Message reply;
            reply.peerAddress=msg.peerAddress;
            reply.type="MTX";
            reply.mac=matrix;
            for(int i=0;i<users.size();i++)
                reply.vec.append(users[i].uid);

            tcpServer->sendMessageToPeer(reply);
        }

    } else if(msg.type=="OUT")
    {
        clientDisconnected(msg.uid);
    } else if(msg.type=="LIV")
    {
        //qDebug()<<"client alive:"<<msg.uid;
    } else if(msg.type=="P2P")
    {
        Message reply;
        reply.type="HII";
        reply.uid=uid;
        reply.username=username;
        reply.vector=users;
        reply.peerAddress=msg.peerAddress;
        tcpServer->sendMessageToPeer(reply);
    } else if(msg.type=="MTX")
    {
        qDebug()<<matrix;
        for(int i=0;i<matrix.size();i++)
        {
            int posOfCurrentUserj=-1;
            for(int j=0;j<msg.vec.size();j++)
                if(msg.vec[j]==users[i].uid)
                    posOfCurrentUserj=j;
            if(posOfCurrentUserj!=-1)
                for(int i2=0;i2<matrix[i].size();i2++)
                {
                    int posOfCurrentUserj2=-1;
                    for(int j2=0;j2<msg.vec.size();j2++)
                    {
                        if(msg.vec[j2]==users[i2].uid)
                            posOfCurrentUserj2=j2;
                    }
                    if(posOfCurrentUserj2!=-1)
                        if(matrix[i][i2]<msg.mac[posOfCurrentUserj][posOfCurrentUserj2])
                            matrix[i][i2]=msg.mac[posOfCurrentUserj][posOfCurrentUserj2];
                }
        }

        int myPos=-1;
        for(int i=0;i<users.size();i++)
        {
            if(users[i].uid==uid)
                myPos=i;
        }

        if(myPos==-1)
        {
            qDebug()<<"not found myself in users list, giving up";
            return;
        }

        for(int i=0;i<matrix.size();i++)
        {
            for(int i2=0;i2<matrix[i].size();i2++)
            {
                if(matrix[myPos][i2]<matrix[i][i2])
                    matrix[myPos][i2]=matrix[i][i2];
            }
        }
        qDebug()<<matrix;
    } else if(msg.type=="GET")
    {
        QLinkedList<Message>::iterator msgHist;
        for(msgHist=messageHistory.begin();msgHist!=messageHistory.end();msgHist++)
        {
            if(msgHist->type=="MSG"&&msgHist->uid==msg.uid&&msgHist->msg_id==msg.msg_id)
            {
                qDebug("found wanted message, sending to network");
                emit sendMessageToNetwork(*msgHist);
                break;
            }
        }
    } else if(msg.type=="MSG")
    {
        int userPos=-1;
        for(int i=0;i<users.size();i++)
        {
            if(users[i].uid==msg.sender)
                userPos=i;
        }

        int myPos=-1;
        for(int i=0;i<users.size();i++)
        {
            if(users[i].uid==uid)
                myPos=i;
        }

        if(myPos==-1)
        {
            qDebug()<<"not found myself in users list, giving up";
            return;
        }

        if(userPos==-1)
        {
            qDebug()<<"not found sender in users list, giving up";
            return;
        }

        qDebug()<<matrix;



        if(matrix[myPos][userPos]==msg.msg_id)
        {
            matrix[myPos][userPos]++;
        }
        else
        {
            if(matrix[myPos][userPos]>msg.msg_id)
                qDebug()<<"already know of this message giving up";
            else
            {
                qDebug()<<"don't have previous messages sending GET message, removing current message";
                QLinkedList<Message>::iterator msgWait;
                for(msgWait=messageWaiting.begin();msgWait!=messageWaiting.end();msgWait++)
                {
                    if(msgWait->sender==msg.sender&&matrix[myPos][userPos]==msgWait->msg_id)
                    {
                        handleReceivedMessage(*msgWait);
                    }
                }
                messageHistory.removeOne(msg);
                messageWaiting.append(msg);
                sendGetMessage(msg.sender,matrix[myPos][userPos]);
            }
            return;
        }
        if(userPos!=myPos)
            matrix[userPos][userPos]++;
        for(int i=0;i<matrix[userPos].size();i++)
        {
            int posOfCurrentUser=-1;
            qDebug()<<msg.vec;
            qDebug()<<users.size();
            for(int i2=0;i2<msg.vec.size();i2++)
                if(msg.vec[i2]==users[i].uid)
                    posOfCurrentUser=i2;
            if(posOfCurrentUser!=-1)
            {
                if(msg.time_vec[posOfCurrentUser]>matrix[userPos][i])
                    matrix[userPos][i]=msg.time_vec[posOfCurrentUser];
            }
        }

        qDebug()<<matrix;
        if(msg.receiver=="")
            ui->log->appendPlainText("TO ALL FROM:"+users[userPos].username+" MESSAGE:"+msg.content);
        else if(msg.receiver==uid)
            ui->log->appendPlainText("TO:"+username+" FROM:"+users[userPos].username+" MESSAGE:"+msg.content);

        QLinkedList<Message>::iterator msgWait;
        for(msgWait=messageWaiting.begin();msgWait!=messageWaiting.end();msgWait++)
        {
            if(msgWait->sender==msg.sender&&matrix[myPos][userPos]==msgWait->msg_id)
            {
                handleReceivedMessage(*msgWait);
                messageHistory.removeOne(msg);
            }
        }
    }

}

void MultitalkWindow::storeMessage(Message msg)
{
    messageHistory.push_front(msg);
    if(messageHistory.size()>1000)
        messageHistory.removeLast();
}

void MultitalkWindow::sendLogMessage()
{
    Message msg;
    msg.type="LOG";
    msg.uid=uid;
    msg.username=username;
    msg.ip_address=ipAddress;
    //storeMessage(msg);
    emit sendMessageToNetwork(msg);

    livTimer->setInterval(10000);
    livTimer->start();
    ui->sendMsg->setEnabled(true);
    ui->sendMsgAll->setEnabled(true);
    statusBarLabel->setText("Connected");
    connect(livTimer,SIGNAL(timeout()),this,SLOT(sendLivMessage()));
}

void MultitalkWindow::sendLivMessage()
{
    Message msg;
    msg.type="LIV";
    msg.uid=uid;
    msg.ip_address=ipAddress;
    msg.sequence=livSequence++;
    //storeMessage(msg);
    emit sendMessageToNetwork(msg);
}

void MultitalkWindow::sendOutMessage()
{
    Message msg;
    msg.type="OUT";
    msg.uid=uid;
    //storeMessage(msg);
    emit sendMessageToNetwork(msg);
}

void MultitalkWindow::sendMsgMessage(QString content,QString receiverUid)
{
    Message msg;
    msg.type="MSG";
    msg.sender=uid;
    msg.receiver=receiverUid;

    int myPos=-1;
    for(int i=0;i<users.size();i++)
    {
        msg.vec.append(users[i].uid);
        if(users[i].uid==uid)
            myPos=i;
    }

    msg.time_vec=matrix[myPos];
    msg.msg_id=matrix[myPos][myPos];
    msg.content=content;

    int userPos=-1;
    for(int i=0;i<users.size();i++)
    {
        if(users[i].uid==msg.receiver)
            userPos=i;
    }
    if(userPos==-1)
        ui->log->appendPlainText("TO ALL MESSAGE:"+msg.content);
    else
        ui->log->appendPlainText("TO:"+users[userPos].username+" MESSAGE:"+msg.content);
    emit sendMessageToNetwork(msg);
}

void MultitalkWindow::sendGetMessage(QString uid,qint32 msg_id)
{
    Message msg;
    msg.type="GET";
    msg.uid=uid;
    msg.msg_id=msg_id;
    storeMessage(msg);
    emit sendMessageToNetwork(msg);
}

void MultitalkWindow::sendMsgAllClicked()
{
    sendMsgMessage(ui->message->text(),"");
}

void MultitalkWindow::sendMsgClicked()
{
    int selectedItem=ui->listWidget->currentRow();
    if(selectedItem!=-1)
        sendMsgMessage(ui->message->text(),users.at(selectedItem).uid);
}
