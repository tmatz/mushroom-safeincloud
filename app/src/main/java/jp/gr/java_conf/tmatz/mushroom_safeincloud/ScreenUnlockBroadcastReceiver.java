package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenUnlockBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = ScreenUnlockBroadcastReceiver.class.getSimpleName();

    public void onReceive(Context c, Intent i) {
        Logger.i(TAG, "onReceive" , i.getAction());
        PocketLock.setPocketLock(null);
    }
}
