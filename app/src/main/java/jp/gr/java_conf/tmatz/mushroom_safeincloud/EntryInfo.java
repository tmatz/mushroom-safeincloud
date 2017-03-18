package jp.gr.java_conf.tmatz.mushroom_safeincloud;

public class EntryInfo implements Comparable<EntryInfo> {
    private String mId;
    private String mTitle;

    public EntryInfo(String id, String title) {
        mId = id;
        mTitle = title;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String toString() {
        return mTitle;
    }

    @Override
    public int compareTo(EntryInfo another) {
        return this.toString().compareTo(another.toString());
    }
}
