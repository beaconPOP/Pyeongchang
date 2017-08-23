package com.zerobin.www.beacon_client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.beaconpop.pyeongchang.R;

/**
 * Created by Byun YB on 2017-05-23.
 */

public class Intro extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        //1초 후에 main으로 전환
        //delay
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    Intent i = new Intent(Intro.this, com.example.becomebeacon.beaconlocker.LoginActivity.class);
                    startActivity(i);
                    finish(); // Activity 종료
                }catch(Exception ignore) {}
            }
        }.start();
    }

}
