package jp.gr.java_conf.tmatz.safeincloud_db;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.Key;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void openDatabase() throws Exception {
        InputStream is = new BufferedInputStream(
                new FileInputStream("database/src/androidTest/res/raw/safeincloud.db"));
        DataInputStream dis = new DataInputStream(is);

        short magic = dis.readShort();
        System.out.println("magic: " + magic);

        byte sver = dis.readByte();
        System.out.println("sver: " + sver);

        byte[] salt = this.readByteArray(dis);
        System.out.println("salt: " + Hex.encodeHexString(salt));

        String password = "test";
        Key key = this.getKey(password.toCharArray(), salt, 10000);
        // System.out.println("xxx: " + Hex.encodeHexString(key.getEncoded()));

        byte[] iv = this.readByteArray(dis);
        System.out.println("iv: " + Hex.encodeHexString(iv));

        byte[] salt2 = this.readByteArray(dis);
        System.out.println("salt2: " + Hex.encodeHexString(salt2));

        byte[] block = this.readByteArray(dis);
        System.out.println("block: " + Hex.encodeHexString(block));

        Cipher cipher = this.getCipher(key, iv);
        byte[] secrets = cipher.doFinal(block);

        DataInputStream dis2 = new DataInputStream(
                new ByteArrayInputStream(secrets));

        byte[] iv2 = this.readByteArray(dis2);
        System.out.println("iv2: " + Hex.encodeHexString(iv2));

        byte[] pass2 = this.readByteArray(dis2);
        System.out.println("pass2: " + Hex.encodeHexString(pass2));

        byte[] check = this.readByteArray(dis2);
        System.out.println("check: " + Hex.encodeHexString(check));

        dis2.close();

        Key key2 = this.getKey(StringUtils.newStringUsAscii(pass2).toCharArray(), salt2, 1000);
        System.out.println("key2: " + Hex.encodeHexString(key2.getEncoded()));

        if (!Arrays.equals(check, key2.getEncoded()))
        {
            throw new Exception();
        }

        key2 = new SecretKeySpec(pass2, "AES");
        cipher = this.getCipher(key2, iv2);

//        byte[] buffer = new byte[20];
//        int readLength = dis.read(buffer);
//        while (readLength > 0)
//        {
//            byte[] output = cipher.update(buffer, 0, readLength);
//
//            System.out.println(Hex.encodeHexString(output));
//
//            readLength = dis.read(buffer);
//        }
//
//        byte[] output = cipher.doFinal();
//        System.out.println(Hex.encodeHexString(output));

//        byte[] gzipHeader =
//        {
//            (byte)0x1f, // ID1
//            (byte)0x8b, // ID2
//            (byte)0x08, // CM
//            (byte)0x00, // FLG
//            (byte)0x00, // MTIME
//            (byte)0x00,
//            (byte)0x00,
//            (byte)0x00,
//            (byte)0x00, // XLF
//            (byte)0x00, // OS
//        };
//
        Reader content = new InputStreamReader(
            new InflaterInputStream(
                new BufferedInputStream(
                    new CipherInputStream(dis, cipher))));

        char[] buffer = new char[1024];
        int readLength = content.read(buffer);
        {
            if (readLength == buffer.length)
            {
                System.out.print(buffer);
            }
            else {
                System.out.print(Arrays.copyOf(buffer, readLength));
            }
        }

//        InputStream content =
//                new CipherInputStream(dis, cipher);
//
//        byte[] buffer = new byte[20];
//        int readLength = content.read(buffer);
//        while (readLength > 0)
//        {
//            if (readLength == buffer.length)
//            {
//                System.out.println(Hex.encodeHexString(buffer));
//            }
//            else
//            {
//                System.out.println(Hex.encodeHexString(Arrays.copyOf(buffer, readLength)));
//            }
//            readLength = content.read(buffer);
//        }

        dis.close();
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
        Key key = keyFactory.generateSecret(keySpec);
        key = new SecretKeySpec(key.getEncoded(), "AES");
        return key;
    }

    private Cipher getCipher(Key key, byte[] iv) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher;
    }

}