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
    QString receiver;
    qint32 msg_id;
    QList<int> time_vec;
    QString content;
    qlonglong sequence;

    bool operator==(Message const& a);
};

#endif // MESSAGE_H
