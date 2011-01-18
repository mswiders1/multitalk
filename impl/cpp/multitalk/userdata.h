#ifndef USERDATA_H
#define USERDATA_H

#include <QString>

class UserData
{
public:
    UserData();
    QString uid;
    QString username;
    QString ip;
    bool operator==(UserData const& a);
};


#endif // USERDATA_H
