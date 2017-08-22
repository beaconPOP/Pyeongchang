package com.zerobin.www.beacon_client;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Byun YB on 2017-05-13.
 */

public class User {
    private String email;
    private String uid;
    String nickname;
    List<Object> coupon = new ArrayList<Object>();
    public List<Object> getCoupon() {
        return coupon;
    }
    public void setCoupon(List<Object> coupon) {
        this.coupon = coupon;
    }

    User(String email, String uuid){
        this.email = email;
        this.uid = uuid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

}