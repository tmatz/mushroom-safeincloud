package jp.gr.java_conf.tmatz.safeincloud_db;

import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;

/**
 * Created by matsuda on 17/03/05.
 */

public class Note {

    private final String mText;

    public Note(String text)
    {
        mText = StringUtils.trimToEmpty(text);
    }

    public String getText()
    {
        return mText;
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("notes " + getText());
    }
}
