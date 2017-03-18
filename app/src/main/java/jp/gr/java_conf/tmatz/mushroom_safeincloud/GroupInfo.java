package jp.gr.java_conf.tmatz.mushroom_safeincloud;

class GroupInfo implements Comparable<GroupInfo> {
    private String mId;
    private String mTitle;

    GroupInfo(String id, String title) {
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
    public int compareTo(GroupInfo another) {
        return this.toString().compareTo(another.toString());
    }
}
