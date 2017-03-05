package jp.gr.java_conf.tmatz.safeincloud_db;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

/**
 * Created by matsuda on 17/03/05.
 */

public class Label {

    private final String mName;
    private final String mId;
    private final String mType;

    public Label(String name, String id, String type)
    {
        mName = StringUtils.trimToEmpty(name);
        mId = StringUtils.trimToEmpty(id);
        mType = StringUtils.trimToEmpty(type);
    }

    public String getName() {
        return mName;
    }

    public String getId() {
        return mId;
    }

    public String getType() {
        return mType;
    }

    public boolean isWebAccounts() {
        return mType.equals("web_accounts");
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("label " + getName() + " " + getType());
    }
}
