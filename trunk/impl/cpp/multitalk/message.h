#ifndef MESSAGE_H
#define MESSAGE_H
#include "userdata.h"

#include <QString>
#include <QList>

class Message
{
public:
    Message();
    QString type;
    QString uid;
    QString username;
    QList<UserData> vector;
    QString ip_address;
    QList<QList<int> > mac;
    QList<QString> vec;
    QString sender;
    QString reciver;
    int msg_id;
    QList<int> time_vec;
    QString content;
};

#endif // MESSAGE_H
