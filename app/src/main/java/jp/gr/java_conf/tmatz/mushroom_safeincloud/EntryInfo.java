package jp.gr.java_conf.tmatz.mushroom_safeincloud;

class EntryInfo implements Comparable<EntryInfo> {
    private String mId;
    private String mTitle;

    EntryInfo(String id, String title) {
        mId = id;
        mTitle = title;
    }

    String getId() {
        return mId;
    }

    String getTitle() {
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
