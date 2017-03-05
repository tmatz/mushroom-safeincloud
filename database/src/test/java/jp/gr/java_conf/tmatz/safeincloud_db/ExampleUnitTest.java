package jp.gr.java_conf.tmatz.safeincloud_db;

import android.util.Xml;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ExampleUnitTest {
    @Test
    public void openDatabase() throws Exception {
        InputStream is = new BufferedInputStream(
                new FileInputStream("database/src/androidTest/res/raw/safeincloud.db"));
        DataInputStream dis = new DataInputStream(is);

        short magic = dis.readShort();
        if (magic != 1285) {
            throw new RuntimeException("unexpected magic value");
        }

        byte sver = dis.readByte();
        if (sver != 1)
        {
            throw new RuntimeException("unexpected sver value");
        }

        byte[] salt = this.readByteArray(dis);
        byte[] iv = this.readByteArray(dis);
        byte[] salt2 = this.readByteArray(dis);
        byte[] block = this.readByteArray(dis);

        String passwordString = "password";
        byte[] password = new byte[passwordString.length()];
        for (int i = 0; i < password.length; i++) {
            password[i] = (byte)passwordString.charAt(i);
        }
        Key key = this.GenerateKey(password, salt, 10000);

        Cipher cipher = this.getCipher(key, iv);
        byte[] secrets = cipher.doFinal(block);

        DataInputStream dis2 = new DataInputStream(
                new ByteArrayInputStream(secrets));

        byte[] iv2 = this.readByteArray(dis2);

        byte[] pass2 = this.readByteArray(dis2);

        byte[] check = this.readByteArray(dis2);

        dis2.close();

        Key key3 = this.GenerateKey(pass2, salt2, 1000);

        if (!Arrays.equals(check, key3.getEncoded())) {
            throw new Exception();
        }

        cipher = this.getCipher(new SecretKeySpec(pass2, "AES"), iv2);

        InputStream content =
                new InflaterInputStream(
                        new BufferedInputStream(
                                new CipherInputStream(dis, cipher)));

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(content, null);
        Database database = readDatabase(parser);

        database.dump(System.out, 0);

        dis.close();
    }

    private Database readDatabase(XmlPullParser parser)
    {
        Database database = new Database();

        try {
            parser.require(XmlPullParser.START_DOCUMENT, null, null);

            while (true) {
                int eventType = parser.next();
                switch (eventType) {
                    case XmlPullParser.END_DOCUMENT:
                        return database;

                    case XmlPullParser.END_TAG:
                        parser.require(eventType, null, "database");
                        return database;

                    case XmlPullParser.START_TAG: {
                        int depth = parser.getDepth();
                        String name = parser.getName();
                        switch (name) {
                            case "database":
                                break;

                            case "label":
                                readLabel(parser, database);
                                break;

                            case "card":
                                readCard(parser, database);
                                break;

                            default:
                                ignoreElement(parser, name, depth);
                        }
                        break;
                    }

                    case XmlPullParser.TEXT:
                        break;
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private Label readLabel(XmlPullParser parser, Database database) throws Exception
    {
        int depth = parser.getDepth();
        Label label = new Label(
                parser.getAttributeValue(null, "name"),
                parser.getAttributeValue(null, "id"),
                parser.getAttributeValue(null, "type"));
        database.addLabel(label);
        ignoreElement(parser, "label", depth);
        return label;
    }

    private Card readCard(XmlPullParser parser, Database database) throws Exception
    {
        String template = StringUtils.trimToEmpty(parser.getAttributeValue(null, "template"));
        boolean isTemplate = template.equals("true");

        if (isTemplate)
        {
            ignoreElement(parser, "card", parser.getDepth());
            return null;
        }
        else
        {
            Card card = new Card(parser.getAttributeValue(null, "title"));
            database.addCard(card);

            while (true)
            {
                switch (parser.nextTag())
                {
                    case XmlPullParser.START_TAG: {
                        String name = parser.getName();
                        switch (name) {
                            case "field":
                                readField(parser, database, card);
                                break;

                            case "notes":
                                readNote(parser, database, card);
                                break;

                            case "label_id":
                                readLabelId(parser, database, card);
                                break;

                            default:
                                ignoreElement(parser, name, parser.getDepth());
                                break;
                        }
                        break;
                    }

                    default:
                        parser.require(XmlPullParser.END_TAG, null, "card");
                        return card;
                }
            }
        }
    }

    private Field readField(XmlPullParser parser, Database database, Card card) throws Exception
    {
        String name = parser.getAttributeValue(null, "name");
        String type = parser.getAttributeValue(null, "type");
        String text = parser.nextText();
        Field field = new Field(name, text, type);
        card.addField(field);
        return field;
    }

    private Note readNote(XmlPullParser parser, Database database, Card card) throws Exception
    {
        Note note = new Note(parser.nextText());
        card.addNote(note);
        return note;
    }

    private LabelId readLabelId(XmlPullParser parser, Database database, Card card) throws Exception
    {
        LabelId labelId = new LabelId(parser.nextText());
        card.addLabelId(labelId);
        return labelId;
    }

    private void ignoreElement(XmlPullParser parser, String name, int depth) throws Exception
    {
        while (true)
        {
            switch (parser.nextTag()) {
                case XmlPullParser.END_DOCUMENT:
                    return;

                case XmlPullParser.END_TAG:
                    if (name.equals(parser.getName()) && depth == parser.getDepth()) {
                        return;
                    }
                    break;

                case XmlPullParser.START_TAG:
                    break;

                default:
                    break;
            }
        }
    }

    private byte[] readByteArray(DataInputStream is, int length) throws IOException
    {
        byte[] buffer = new byte[length];
        is.readFully(buffer);
        return buffer;
    }

    private byte[] readByteArray(DataInputStream is) throws IOException
    {
        byte length = is.readByte();
        return this.readByteArray(is, length);
    }

    private Key getKey(char[] password, byte[] salt, int iterationCount) throws Exception
    {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, 256);
        return keyFactory.generateSecret(keySpec);
    }

    private Cipher getCipher(Key key, byte[] iv) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        key = new SecretKeySpec(key.getEncoded(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher;
    }

    private void print(String title, char[] value) {
        System.out.print(title);
        System.out.print(":");
        for (char c: value) {
            System.out.format(" %02x", (int)c);
        }
        System.out.println();
    }

    private Key GenerateKey(
            final byte[] password,
            final byte[] salt,
            int iterationCount)
            throws RuntimeException
    {
        final int keyLength = 32;

        byte[] passwordInternal = password.clone();
        byte[] buffer = null;
        try
        {
            SecretKeySpec keySpec = new SecretKeySpec(passwordInternal, "HmacSHA1");
            Mac prf;
            try {
                prf = Mac.getInstance("HmacSHA1");
                prf.init(keySpec);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            int macLength = prf.getMacLength(); // 20 for SHA1
            int bufferLength = Math.max(keyLength, macLength) + macLength - 1;
            int macCount = bufferLength / macLength;
            byte initialMac[] = new byte[salt.length + 4]; // salt || INT(i + 1)
            System.arraycopy(salt, 0, initialMac, 0, salt.length);

            buffer = new byte[bufferLength];
            for (int i = 0; i < macCount; i++) {
                macSetIndex(initialMac, i + 1);

                byte mac[] = initialMac;
                for (int j = 0; j < iterationCount; j++) {
                    mac = prf.doFinal(mac);
                    macXor(buffer, i * macLength, mac);
                }
            }

            return new SecretKeySpec(Arrays.copyOf(buffer, keyLength), "AES");
        }
        finally {
            Arrays.fill(passwordInternal, (byte)0);
            if (buffer != null)
            {
                Arrays.fill(buffer, (byte)0);
            }
        }
    }

    private void macXor(byte[] dest, int offset, byte[] src) {
        for (int i = 0; i < src.length; i++) {
            dest[i + offset] ^= src[i];
        }
    }

    private void macSetIndex(byte[] dest, int i) {
        dest[dest.length - 4] = (byte) (i >> 24);
        dest[dest.length - 3] = (byte) (i >> 16);
        dest[dest.length - 2] = (byte) (i >> 8);
        dest[dest.length - 1] = (byte) (i);
    }
}
