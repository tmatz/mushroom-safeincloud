package jp.gr.java_conf.tmatz.safeincloud_db;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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
public class ExampleUnitTest {
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

        // char[] password = "test".toCharArray();
        char[] password = {0x74, 0x65, 0x73, 0x74};
        Key key = this.getKey(password, salt, 10000);
        // System.out.println("xxx: " + Hex.encodeHexString(key.getEncoded()));

        byte[] iv = this.readByteArray(dis);
        System.out.println("iv: " + Hex.encodeHexString(iv));

        byte[] salt2 = this.readByteArray(dis);
        System.out.println("checkSalt: " + Hex.encodeHexString(salt2));

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

        byte[] key3 = this.GenerateKey(pass2, salt2, 1000);
        System.out.println("key3: " + Hex.encodeHexString(key3));

        if (!Arrays.equals(check, key3)) {
            throw new Exception();
        }

        cipher = this.getCipher(new SecretKeySpec(pass2, "AES"), iv2);

        Reader content = new InputStreamReader(
                new InflaterInputStream(
                        new BufferedInputStream(
                                new CipherInputStream(dis, cipher))));

        char[] buffer = new char[1024];
        int readLength = content.read(buffer);
        {
            if (readLength == buffer.length) {
                System.out.print(buffer);
            } else {
                System.out.print(Arrays.copyOf(buffer, readLength));
            }
        }

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

    private byte[] GenerateKey(
            final byte[] password,
            final byte[] salt,
            int iterationCount)
    {
        int keyLength = 32;

        byte[] passwordInternal = password.clone();
        byte[] saltInternal = salt.clone();

        SecretKeySpec keySpec = new SecretKeySpec(passwordInternal, "HmacSHA1");
        Mac prf = null;
        try {
            prf = Mac.getInstance("HmacSHA1");
            prf.init(keySpec);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        int hLen = prf.getMacLength(); // 20 for SHA1
        int l = Math.max(keyLength, hLen); // 1 for 128bit (16-byte) keys
        byte T[] = new byte[l * hLen];
        int ti_offset = 0;
        for (int i = 1; i <= l; i++) {
            F(T, ti_offset, prf, saltInternal, iterationCount, i);
            ti_offset += hLen;
        }

        return Arrays.copyOf(T, keyLength);
    }

    private void F(
            byte[] dest,
            int offset,
            Mac prf,
            byte[] salt,
            int c,
            int blockIndex)
    {
        final int hLen = prf.getMacLength();
        byte U_r[] = new byte[hLen];
        // U0 = S || INT (i);
        byte U_i[] = new byte[salt.length + 4];
        System.arraycopy(salt, 0, U_i, 0, salt.length);
        INT(U_i, salt.length, blockIndex);
        for (int i = 0; i < c; i++) {
            U_i = prf.doFinal(U_i);
            XOR(U_r, U_i);
        }

        System.arraycopy(U_r, 0, dest, offset, hLen);
    }

    private void XOR(byte[] dest, byte[] src) {
        for (int i = 0; i < dest.length; i++) {
            dest[i] ^= src[i];
        }
    }

    private void INT(byte[] dest, int offset, int i) {
        dest[offset + 0] = (byte) (i >> 24);
        dest[offset + 1] = (byte) (i >> 16);
        dest[offset + 2] = (byte) (i >> 8);
        dest[offset + 3] = (byte) (i);
    }
}
