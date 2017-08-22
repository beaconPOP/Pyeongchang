package com.zerobin.www.beacon_client;

import com.beaconpop.pyeongchang.R;

/**
 * Created by seonyeong on 2017-05-05.
 */

public class Chat {

    public String Content;
    public String UserID;
    public String Img_num;
    public int img1=R.drawable.a1;
    public int img2=R.drawable.a3;
    public int img3=R.drawable.a5;

             public Chat()
             {
                 // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
             }
             public Chat(String content, String userid, String img_num)
             {
                 this.Content = content;
                 this.UserID=userid;
                 this.Img_num=img_num;
             }

             public int getImg1()
             {
                 return this.img1;
             }


/*
    public String getContent() {return Content;}

    public void setContent(String content) {
        Content = content;
    }


    public String getUserID() {
        return UserID;
    }


    public void setUserID(String userID) {
        UserID = userID;
    }

       */

}
