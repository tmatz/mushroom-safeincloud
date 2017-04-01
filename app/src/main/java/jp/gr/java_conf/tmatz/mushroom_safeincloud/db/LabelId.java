package jp.gr.java_conf.tmatz.mushroom_safeincloud.db;

import java.io.PrintStream;

public class LabelId {

    private String mId;

    public LabelId() {
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("label_id " + getId());
    }
}
