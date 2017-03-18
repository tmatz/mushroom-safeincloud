package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utilities {
    public static void setDigest(Bundle bundle, String tag) {
        if (bundle.containsKey(tag)) {
            bundle.remove(tag);
        }

        Parcel p = Parcel.obtain();
        p.writeBundle(bundle);
        byte[] data = p.marshall();
        p.recycle();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(data);
            bundle.putString(tag, Base64.encodeToString(digest, Base64.DEFAULT));
        } catch (NoSuchAlgorithmException e) {
            bundle.putString(tag, Base64.encodeToString(data, Base64.DEFAULT));
        }
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
