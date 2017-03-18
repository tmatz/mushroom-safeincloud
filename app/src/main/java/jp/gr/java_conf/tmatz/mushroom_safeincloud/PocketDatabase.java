package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;

import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PocketDatabase {
    public static final String DATABASE_NAME = "wallet.db";
    public static final String TABLE_ENTRIES = "entries";
    public static final String TABLE_FIELDS = "fields";
    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_GROUPFIELDS = "groupfields";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_NOTE = "note";
    public static final String COL_ENTRY_ID = "emtry_id";
    public static final String COL_GROUP_ID = "group_id";
    public static final String COL_VALUE = "value";
    public static final String COL_GROUPFIELD_ID = "groupfield_id";
    public static final String COL_ICON = "icon";
    public static final String COL_IS_HIDDEN = "is_hodden";

    private static SQLiteDatabase sDatabase;

    public static boolean isReadable() {
        if (!Utilities.isExternalStorageReadable()) {
            return false;
        }

        File dbFile = new File(
                Environment.getExternalStorageDirectory(),
                PocketDatabase.DATABASE_NAME);
        if (!dbFile.exists() || !dbFile.canRead()) {
            return false;
        }

        return true;
    }

    private synchronized static SQLiteDatabase openDatabase() {
        if (sDatabase != null) {
            return sDatabase;
        }

        if (Utilities.isExternalStorageReadable()) {
            File dbFile = new File(
                    Environment.getExternalStorageDirectory()
                    , PocketDatabase.DATABASE_NAME);
            if (dbFile.exists()) {
                sDatabase = SQLiteDatabase.openDatabase(
                        dbFile.getAbsolutePath(),
                        null,
                        SQLiteDatabase.OPEN_READONLY);
            }
        }
        return sDatabase;
    }

    public synchronized static List<GroupInfo> readGroups(Context context, PocketLock pocketLock) {
        Validate.notNull(context);
        Validate.notNull(pocketLock);

        SQLiteDatabase database = PocketDatabase.openDatabase();
        if (database == null)
        {
            return null;
        }

        ArrayList<GroupInfo> items = new ArrayList<GroupInfo>();

        Cursor c = database.rawQuery("select _id, title from groups", null);
        while (c.moveToNext())
        {
            items.add(new GroupInfo(c.getString(0), pocketLock.decrypt(c.getString(1))));
        }
        c.close();

        Collections.sort(items);

        items.add(0, new GroupInfo("", context.getString(R.string.all_entries)));
        return items;
    }

    public synchronized static List<EntryInfo> readEntries(Context context, PocketLock pocketLock, String groupId) {
        Validate.notNull(context);
        Validate.notNull(pocketLock);

        SQLiteDatabase database = openDatabase();
        if (database == null) {
            return null;
        }

        List<EntryInfo> items = new ArrayList<>();

        Cursor c;
        if (groupId == null) {
            c = database.rawQuery(
                    "select _id, title from entries",
                    null);
        } else {
            c = database.rawQuery(
                    "select _id, title from entries where group_id = ?",
                    new String[]{ groupId });
        }

        if (c.moveToFirst()) {
            do {
                items.add(new EntryInfo(c.getString(0), pocketLock.decrypt(c.getString(1))));
            } while (c.moveToNext());
        }

        c.close();

        Collections.sort(items);

        return items;
    }

    public synchronized static List<FieldInfo> readFields(Context context, PocketLock pocketLock, String entryId) {
        Validate.notNull(context);
        Validate.notNull(pocketLock);
        Validate.notNull(entryId);

        SQLiteDatabase database = PocketDatabase.openDatabase();
        if (database == null) {
            return null;
        }

        List<FieldInfo> items = new ArrayList<>();

        {
            Cursor c = database.rawQuery(
                    "select _id, title, value, is_hidden from fields where entry_id = ?",
                    new String[] { entryId });

            while (c.moveToNext()) {
                FieldInfo data = new FieldInfo(
                        c.getInt(0),
                        pocketLock.decrypt(c.getString(1)),
                        pocketLock.decrypt(c.getString(2)),
                        c.getInt(3) != 0);

                if (!TextUtils.isEmpty(data.getValue())) {
                    items.add(data);
                }
            }
            c.close();
        }

        Collections.sort(items);

        {
            Cursor c = database.rawQuery(
                    "select _id, notes from entries where _id = ?",
                    new String[] { entryId });
            if (c.moveToFirst()) {
                FieldInfo data = new FieldInfo(
                        -1,
                        context.getResources().getString(R.string.notes),
                        pocketLock.decrypt(c.getString(1)),
                        false);

                if (!TextUtils.isEmpty(data.getValue())) {
                    items.add(data);
                }
            }
            c.close();
        }

        return items;
    }
}
