package jp.gr.java_conf.tmatz.safeincloud_db;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DatabaseReader {

    public InputStream read(InputStream inputStream) throws Exception {
        DataInputStream dis = new DataInputStream(inputStream);

        short magic = dis.readShort();
        if (magic != 1285) {
            throw new RuntimeException("unexpected magic value");
        }

        byte sver = dis.readByte();
        if (sver != 1) {
            throw new RuntimeException("unexpected sver value");
        }

        byte[] salt = this.readByteArray(dis);
        byte[] iv = this.readByteArray(dis);
        byte[] salt2 = this.readByteArray(dis);
        byte[] block = this.readByteArray(dis);

        String passwordString = "password";
        byte[] password = new byte[passwordString.length()];
        for (int i = 0; i < password.length; i++) {
            password[i] = (byte) passwordString.charAt(i);
        }
        Key key = this.getKey(password, salt, 10000);

        Cipher cipher = this.getCipher(key, iv);
        byte[] secrets = cipher.doFinal(block);

        DataInputStream dis2 = new DataInputStream(
                new ByteArrayInputStream(secrets));

        byte[] iv2 = this.readByteArray(dis2);

        byte[] pass2 = this.readByteArray(dis2);

        byte[] check = this.readByteArray(dis2);

        dis2.close();

        Key key3 = this.getKey(pass2, salt2, 1000);

        if (!Arrays.equals(check, key3.getEncoded())) {
            throw new Exception();
        }

        cipher = this.getCipher(new SecretKeySpec(pass2, "AES"), iv2);

        InputStream content =
                new InflaterInputStream(
                        new BufferedInputStream(
                                new CipherInputStream(dis, cipher)));

        return content;
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

    private Cipher getCipher(Key key, byte[] iv) throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        key = new SecretKeySpec(key.getEncoded(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher;
    }

    private Key getKey(
            final byte[] password,
            final byte[] salt,
            int iterationCount)
            throws RuntimeException
    {
        final int keyLength = 32;

        byte[] passwordInternal = password.clone();
        byte[] buffer = null;
        try {
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
        } finally {
            Arrays.fill(passwordInternal, (byte) 0);
            if (buffer != null) {
                Arrays.fill(buffer, (byte) 0);
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
