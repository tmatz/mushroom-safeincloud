package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.Card;
import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.Database;
import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.Field;
import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.Label;
import jp.gr.java_conf.tmatz.mushroom_safeincloud.db.LabelId;

class PocketDatabase {
    static final String DATABASE_NAME = "wallet.db";
    static final String TABLE_ENTRIES = "entries";
    static final String TABLE_FIELDS = "fields";
    static final String TABLE_GROUPS = "groups";
    static final String TABLE_GROUPFIELDS = "groupfields";
    static final String COL_ID = "_id";
    static final String COL_TITLE = "title";
    static final String COL_NOTE = "note";
    static final String COL_ENTRY_ID = "emtry_id";
    static final String COL_GROUP_ID = "group_id";
    static final String COL_VALUE = "value";
    static final String COL_GROUPFIELD_ID = "groupfield_id";
    static final String COL_ICON = "icon";
    static final String COL_IS_HIDDEN = "is_hodden";

    private static SQLiteDatabase sDatabase;

    static boolean isReadable() {
        if (!Utilities.isExternalStorageReadable()) {
            return false;
        }

        File dbFile = new File(
                Environment.getExternalStorageDirectory(),
                PocketDatabase.DATABASE_NAME);

        return dbFile.exists() && !dbFile.canRead();
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

    synchronized static List<GroupInfo> readGroups(Context context, PocketLock pocketLock) {
        Validate.notNull(context);
        Validate.notNull(pocketLock);

        ArrayList<GroupInfo> items = new ArrayList<>();

        Database database = pocketLock.getDatabase();
        if (database == null) {
            return items;
        }

        for (Label label : database.getLabels()) {
            items.add(new GroupInfo(label.getId(), label.getName()));
        }

        items.add(0, new GroupInfo("", context.getString(R.string.all_entries)));

        return items;

//        SQLiteDatabase database = PocketDatabase.openDatabase();
//        if (database == null)
//        {
//            return null;
//        }
//
//        ArrayList<GroupInfo> items = new ArrayList<>();
//
//        Cursor c = database.rawQuery("select _id, title from groups", null);
//        while (c.moveToNext())
//        {
//            items.add(new GroupInfo(c.getString(0), pocketLock.decrypt(c.getString(1))));
//        }
//        c.close();
//
//        Collections.sort(items);
//
//        items.add(0, new GroupInfo("", context.getString(R.string.all_entries)));
//        return items;
    }

    synchronized static List<EntryInfo> readEntries(Context context, PocketLock pocketLock, String groupId) {
        Validate.notNull(context);
        Validate.notNull(pocketLock);

        List<EntryInfo> items = new ArrayList<>();

        Database database = pocketLock.getDatabase();
        if (database == null) {
            return items;
        }

        List<Card> cards = database.getCards();
        if (groupId == null || groupId.isEmpty()) {
            for (int i = 0; i < cards.size(); i++) {
                Card card = cards.get(i);
                if (!card.isTemplate()) {
                    items.add(new EntryInfo(String.valueOf(i), card.getTitle()));
                }
            }
        } else {
            for (int i = 0; i < cards.size(); i++) {
                Card card = cards.get(i);
                if (!card.isTemplate()) {
                    for (LabelId labelId : card.getLabelIds()) {
                        if (labelId.getId().equals(groupId)) {
                            items.add(new EntryInfo(String.valueOf(i), card.getTitle()));
                            break;
                        }
                    }
                }
            }
        }

        return items;

//        SQLiteDatabase database = openDatabase();
//        if (database == null) {
//            return null;
//        }
//
//        List<EntryInfo> items = new ArrayList<>();
//
//        Cursor c;
//        if (groupId == null) {
//            c = database.rawQuery(
//                    "select _id, title from entries",
//                    null);
//        } else {
//            c = database.rawQuery(
//                    "select _id, title from entries where group_id = ?",
//                    new String[]{ groupId });
//        }
//
//        if (c.moveToFirst()) {
//            do {
//                items.add(new EntryInfo(c.getString(0), pocketLock.decrypt(c.getString(1))));
//            } while (c.moveToNext());
//        }
//
//        c.close();
//
//        Collections.sort(items);
//
//        return items;
    }

    synchronized static List<FieldInfo> readFields(Context context, PocketLock pocketLock, String entryId) {
        Validate.notNull(context);
        Validate.notNull(pocketLock);
        Validate.notNull(entryId);

        List<FieldInfo> items = new ArrayList<>();

        Database database = pocketLock.getDatabase();
        if (database == null) {
            return items;
        }

        Card card = database.getCards().get(Integer.parseInt(entryId));
        List<Field> fields = card.getFields();

        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            items.add(new FieldInfo(i, field.getTitle(), field.getValue(), field.isSecret()));
        }

        return items;

//        SQLiteDatabase database = PocketDatabase.openDatabase();
//        if (database == null) {
//            return null;
//        }
//
//        List<FieldInfo> items = new ArrayList<>();
//
//        {
//            Cursor c = database.rawQuery(
//                    "select _id, title, value, is_hidden from fields where entry_id = ?",
//                    new String[] { entryId });
//
//            while (c.moveToNext()) {
//                FieldInfo data = new FieldInfo(
//                        c.getInt(0),
//                        pocketLock.decrypt(c.getString(1)),
//                        pocketLock.decrypt(c.getString(2)),
//                        c.getInt(3) != 0);
//
//                if (!TextUtils.isEmpty(data.getValue())) {
//                    items.add(data);
//                }
//            }
//            c.close();
//        }
//
//        Collections.sort(items);
//
//        {
//            Cursor c = database.rawQuery(
//                    "select _id, notes from entries where _id = ?",
//                    new String[] { entryId });
//            if (c.moveToFirst()) {
//                FieldInfo data = new FieldInfo(
//                        -1,
//                        context.getResources().getString(R.string.notes),
//                        pocketLock.decrypt(c.getString(1)),
//                        false);
//
//                if (!TextUtils.isEmpty(data.getValue())) {
//                    items.add(data);
//                }
//            }
//            c.close();
//        }
//
//        return items;
    }
}
