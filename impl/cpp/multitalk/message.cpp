#include "message.h"

Message::Message()
{

}


bool Message::operator==(Message const& a)
{
    if(type==a.type&&uid==a.uid&&username==a.username&&vector==a.vector&&ip_address==a.ip_address
       &&mac==a.mac&&vec==a.vec&&sender==a.sender&&receiver==a.receiver&&msg_id==a.msg_id&&
       time_vec==a.time_vec&&content==a.content&&sequence==a.sequence)
        return true;
    else
        return false;

}

