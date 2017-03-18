package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.util.Log;

class LockTimer {
    private static final String TAG = LockTimer.class.getSimpleName();
    private static final int EXPIRE_DELAY = 300 * 1000;
    private static final boolean sExpireTimeEnable = true;

    private static long sExpireTime;

    static synchronized void resetTimer() {
        Log.i(TAG, "resetTimer");
        sExpireTime = 0;
    }

    static synchronized void startTimer() {
        Log.i(TAG, "startTimer");
        sExpireTime = System.currentTimeMillis() + EXPIRE_DELAY;
    }

    static synchronized boolean isExpired() {
        return sExpireTimeEnable &&
                sExpireTime != 0 &&
                System.currentTimeMillis() > sExpireTime;
    }
}
