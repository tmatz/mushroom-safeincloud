package jp.gr.java_conf.tmatz.mushroom_safeincloud.db;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final List<Card> mCards = new ArrayList<>();
    private final List<Label> mLabels = new ArrayList<>();

    public Database()
    {}

    public List<Card> getCards()
    {
        return mCards;
    }

    public void addCard(Card card) {
        mCards.add(card);
    }

    public List<Label> getLabels()
    {
        return mLabels;
    }

    public void addLabel(Label label) {
        mLabels.add(label);
    }

    public void dump(PrintStream out, int depth) {
        for (int i = 0; i < depth; i++) {
            out.print("  ");
        }
        out.println("database");
        for (Card card: mCards)
        {
            card.dump(out, depth + 1);
        }
        for (Label label: mLabels)
        {
            label.dump(out, depth + 1);
        }
    }
}
