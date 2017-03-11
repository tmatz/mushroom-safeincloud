package jp.gr.java_conf.tmatz.safeincloud_db;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

public class Label {

    private static final String WEB_ACCOUNTS = "web_accounts";

    private String mName;
    private String mId;
    private String mType;

    public Label() {
    }

    public Label(String name, String id, String type) {
        mName = StringUtils.trimToEmpty(name);
        mId = StringUtils.trimToEmpty(id);
        mType = StringUtils.trimToEmpty(type);
    }

    public String getName() {
        return mName;
    }

    public void setName(String value) {
        mName = value;
    }

    public String getId() {
        return mId;
    }

    public void setId(String value) {
        mId = value;
    }

    public String getType() {
        return mType;
    }

    public void setType(String value) {
        mType = value;
    }

    public boolean isWebAccounts() {
        return mType.equals(WEB_ACCOUNTS);
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("label " + getName() + " " + getId() + " " + getType());
    }
}
