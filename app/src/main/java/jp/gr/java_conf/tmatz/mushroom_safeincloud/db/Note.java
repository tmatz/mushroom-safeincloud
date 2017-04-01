package jp.gr.java_conf.tmatz.mushroom_safeincloud.db;

import java.io.PrintStream;

public class Note {

    private String mText;

    public Note() {
    }

    public String getText() {
        return mText;
    }

    public void setText(String value) {
        mText = value;
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("notes " + getText());
    }
}
