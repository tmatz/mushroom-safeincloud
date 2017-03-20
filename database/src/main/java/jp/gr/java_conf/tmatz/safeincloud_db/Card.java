package jp.gr.java_conf.tmatz.safeincloud_db;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Card {

    enum Symbol {
        credit_card,
        web_site,
        email,
        key,
        lock,
        password,
        insurance,
        membership,
        bank,
        id,
        social_security,
        router,
        network,
        cd,
        passport,
        visa,
        laptop,
        f,
        g,
        t,
    }

    private String mTitle; // title
    private int mId; // id
    private String mSymbol; // symbol
    private String mColor; // color
    private boolean mIsTemplate; // template
    private String mType; // type
    private boolean mStar; // star
    private long mTimestamp; // time_stamp

    private final List<Field> mFields = new ArrayList<>();
    private final List<Note> mNotes = new ArrayList<>();
    private final List<LabelId> mLabelIds = new ArrayList<>();

    public Card() {
    }

    public boolean isTemplate() {
        return mIsTemplate;
    }

    public void setTemplate(boolean isTemplate) {
        mIsTemplate = isTemplate;
    }

    public void addField(Field field) {
        mFields.add(field);
    }

    public void addNote(Note note) {
        mNotes.add(note);
    }

    public void addLabelId(LabelId labelId) {
        mLabelIds.add(labelId);
    }

    public List<Field> getFields() {
        return mFields;
    }

    public List<Note> getNotes() {
        return mNotes;
    }

    public List<LabelId> getLabelIds() {
        return mLabelIds;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("card " + getTitle());

        for (Field field: mFields)
        {
            field.dump(out, depth + 1);
        }

        for (Note note: mNotes)
        {
            note.dump(out, depth + 1);
        }

        for (LabelId labelId: mLabelIds)
        {
            labelId.dump(out, depth + 1);
        }
    }
}
