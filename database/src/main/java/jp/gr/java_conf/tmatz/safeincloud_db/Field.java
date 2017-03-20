package jp.gr.java_conf.tmatz.safeincloud_db;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

public class Field {

    private String mTitle;
    private String mTitleLower;
    private String mValue;
    private String mValueLower;
    private String mType;

    enum Type {
        __unknown__,
        date,
        email,
        login,
        number,
        one_time_password,
        password,
        phone,
        pin,
        secret,
        text,
        website,
    }

    public Field() {
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = StringUtils.trimToEmpty(title);
        mTitleLower = mTitle.toLowerCase();
    }

    public String getTitleLower() {
        return mTitleLower;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = StringUtils.trimToEmpty(value);
        mValueLower = mValue.toLowerCase();
    }

    public String getValueLower() {
        return mValueLower;
    }

    public String getType() {
        return mType;
    }

    public boolean isSecret() {
        switch (getType()) {
            case "one_time_password":
            case "password":
            case "pin":
            case "secret":
                return true;
            default:
                return false;
        }
    }

    public void setType(String type) {
        mType = StringUtils.trimToEmpty(type);
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("field " + getTitle() + " " + getType() + " " + getValue());
    }
}
