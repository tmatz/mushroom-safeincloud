package jp.gr.java_conf.tmatz.safeincloud_db;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

/**
 * Created by matsuda on 17/03/05.
 */

public class Field {
    private final String mTitle;
    private final String mTitleLower;
    private final String mValue;
    private final String mValueLower;
    private final String mType;

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

    public Field(String title, String value, String type)
    {
        mTitle = StringUtils.trimToEmpty(title);
        mTitleLower = mTitle.toLowerCase();
        mValue = StringUtils.trimToEmpty(value);
        mValueLower = mValue.toLowerCase();
        mType = StringUtils.trimToEmpty(type);
    }

    public String getTitle()
    {
        return mTitle;
    }

    public String getTitleLower()
    {
        return mTitleLower;
    }

    public String getValue()
    {
        return mValue;
    }

    public String getValueLower()
    {
        return mValueLower;
    }

    public String getType()
    {
        return mType;
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("field " + getTitle() + " " + getType() + " " + getValue());
    }
}
