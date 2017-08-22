package com.zerobin.www.beacon_client;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Byun YB on 2017-05-10.
 */

//팝업이 전송됐을때 사용자 휴대폰의 화면이 꺼져있다면 화면을 킨다.
public class PushWakeLock {
    private static PowerManager.WakeLock sCpuWakeLock;

    static void acquireCpuWakeLock(Context context) {
        if (sCpuWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "PowerManagerPowerManagerPowerManager");
        sCpuWakeLock.acquire();
    }

    static void releaseCpuLock(){
        if(sCpuWakeLock != null){
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
    }
}
