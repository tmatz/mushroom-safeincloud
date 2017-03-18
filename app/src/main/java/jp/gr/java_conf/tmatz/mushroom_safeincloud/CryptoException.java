package jp.gr.java_conf.tmatz.mushroom_safeincloud;

public class CryptoException extends RuntimeException {
    private static final long serialVersionUID = 30950614149791578L;

    private int mId;

    public CryptoException(int id, Throwable e) {
        super(e);
        mId = id;
    }

    public CryptoException(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }
}
