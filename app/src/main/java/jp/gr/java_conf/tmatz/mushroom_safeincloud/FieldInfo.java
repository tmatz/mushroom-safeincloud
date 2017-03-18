package jp.gr.java_conf.tmatz.mushroom_safeincloud;

public class FieldInfo implements Comparable<FieldInfo> {
    private int mId;
    private String mTitle;
    private String mValue;
    private boolean mIsHidden;

    public FieldInfo(int id, String title, String value, boolean isHidden) {
        mId = id;
        mTitle = title;
        mValue = value;
        mIsHidden = isHidden;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getValue() {
        return mValue;
    }

    public boolean isHidden() {
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
