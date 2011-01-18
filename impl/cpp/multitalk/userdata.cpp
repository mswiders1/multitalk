#include "userdata.h"

UserData::UserData()
{

}

bool UserData::operator==(UserData const& a)
{
    if(uid==a.uid&&username==a.username&&ip==a.ip)
        return true;
    else
        return false;
}
