package jp.gr.java_conf.tmatz.mushroom_safeincloud;

class FieldInfo implements Comparable<FieldInfo> {
    private int mId;
    private String mTitle;
    private String mValue;
    private boolean mIsHidden;

    FieldInfo(int id, String title, String value, boolean isHidden) {
        mId = id;
        mTitle = title;
        mValue = value;
        mIsHidden = isHidden;
    }

    int getId() {
        return mId;
    }

    String getTitle() {
        return mTitle;
    }

    String getValue() {
        return mValue;
    }

    boolean isHidden() {
        return mIsHidden;
    }

    public String toString() {
        return mTitle;
    }

    @Override
    public int compareTo(FieldInfo another) {
        return this.toString().compareTo(another.toString());
    }
}
