package jp.gr.java_conf.tmatz.mushroom_safeincloud;

public class GroupInfo implements Comparable<GroupInfo> {
    private String mId;
    private String mTitle;

    public GroupInfo(String id, String title) {
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
    public int compareTo(GroupInfo another) {
        return this.toString().compareTo(another.toString());
    }
}
