package com.example.becomebeacon.beaconlocker;

import java.util.Date;

/**
 * Created by Ryu on 2017-06-04.
 */

public class FindMessage implements Comparable <FindMessage>
{

    public String message;
    public String devAddress;
    public boolean isChecked;
    public String keyValue;
    public String sendUid;
    public Date date;

    public FindMessage()
    {
        sendUid=LoginActivity.getUser().getUid();
        this.message ="";
        this.devAddress ="";
        isChecked=false;
        keyValue="";
        date=null;
    }


    @Override
    public int compareTo(FindMessage other)
    {
        return date.compareTo(other.date);
    }


    public String toString()
    {
        return "message: "+message+" ischecked :"+isChecked;
    }

}
