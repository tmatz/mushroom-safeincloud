package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.os.*;
import android.util.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

class PocketLock {
    private static final String TAG = "PocketLock";
    private static final String PROVIDER = "BC";
    private static final int SALT_LENGTH = 20;
    private static final int IV_LENGTH = 16;
    private static final int PBE_ITERATION_COUNT = 100;
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    private static final String HASH_ALGORITHM = "SHA-512";
    private static final String PBE_ALGORITHM = "PBEWithSHA256And256BitAES-CBC-BC";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "AES";
    private static final String HASHFILE_NAME = "hash.txt";

    private static PocketLock sPocketLock;

    private String mPasswordHash;
    private String mPasswordSalt;
    private String mEncryptionSalt;
    private String mPackageName;
    private SecretKey mSecretKey;

    private static void expire() {
        Log.i(TAG, "expire");
        setPocketLock(null);
    }

    static synchronized PocketLock getPocketLock(String packageName) {
        if (LockTimer.isExpired()) {
            expire();
            return null;
        }
        if (sPocketLock != null) {
            if (packageName == null) {
                if (sPocketLock.mPackageName == null) {
                    return sPocketLock;
                }
            } else {
                if (packageName.equals(sPocketLock.mPackageName)) {
                    return sPocketLock;
                }
            }
        }
        return null;
    }

    static synchronized void setPocketLock(PocketLock lock) {
        sPocketLock = lock;
    }

    PocketLock(String packageName) throws CryptoException {
        if (!Utilities.isExternalStorageReadable()) {
            throw new CryptoException(R.string.exception_storage_not_readable);
        }

        File dir = Environment.getExternalStorageDirectory();
        File hashFile = new File(dir, HASHFILE_NAME);
        if (!hashFile.exists()) {
            throw new CryptoException(R.string.exception_file_not_found);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(hashFile))) {
            mPasswordHash = br.readLine();
            br.readLine(); // version
            br.readLine(); // encryptionMethod
            mPasswordSalt = br.readLine();
            mEncryptionSalt = br.readLine();
            mPackageName = packageName;
        } catch (Exception e) {
            throw new CryptoException(R.string.exception_file_can_not_read);
        }
    }

    @SuppressWarnings("unused")
    String getPackageName() {
        return mPackageName;
    }

    void unlock(String password) throws CryptoException {
        mSecretKey = getDatabaseSecretKey(password);
    }

    String decrypt(String encrypted) throws CryptoException {
        try {
            return decrypt(mSecretKey, encrypted);
        } catch (Exception e) {
            throw new CryptoException(R.string.exception_decryption_failed, e);
        }
    }

    private SecretKey getDatabaseSecretKey(String password) throws CryptoException {
        String genPasswordHash;
        try {
            genPasswordHash = getHash(password, mPasswordSalt);
        } catch (Exception e) {
            throw new CryptoException(R.string.exception_decryption_failed, e);
        }

        if (!genPasswordHash.equals(mPasswordHash)) {
            throw new CryptoException(R.string.exception_wrong_password);
        }

        try {
            return getSecretKey(password, mEncryptionSalt);
        } catch (Exception e) {
            throw new CryptoException(R.string.exception_decryption_failed, e);
        }
    }

    @SuppressWarnings("unused")
    static String encrypt(SecretKey secret, String cleartext) throws Exception {
        byte[] iv = generateIv();
        String ivHex = HexEncoder.toHex(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher encryptionCipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
        encryptionCipher.init(Cipher.ENCRYPT_MODE, secret, ivSpec);
        byte[] encryptedText = encryptionCipher.doFinal(cleartext.getBytes("UTF-8"));
        String encryptedHex = HexEncoder.toHex(encryptedText);
        return ivHex + encryptedHex;

    }

    private static String decrypt(SecretKey secret, String encrypted) throws Exception {
        Cipher decryptionCipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER);
        String ivHex = encrypted.substring(0, IV_LENGTH * 2);
        String encryptedHex = encrypted.substring(IV_LENGTH * 2);
        IvParameterSpec ivSpec = new IvParameterSpec(HexEncoder.toByte(ivHex));
        decryptionCipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
        byte[] decryptedText = decryptionCipher.doFinal(HexEncoder.toByte(encryptedHex));
        return new String(decryptedText, "UTF-8");
    }

    private static SecretKey getSecretKey(String password, String salt) throws Exception {
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), HexEncoder.toByte(salt), PBE_ITERATION_COUNT, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBE_ALGORITHM, PROVIDER);
        SecretKey tmp = factory.generateSecret(pbeKeySpec);
        return new SecretKeySpec(tmp.getEncoded(), SECRET_KEY_ALGORITHM);
    }

    private static String getHash(String password, String salt) throws Exception {
        String input = password + salt;
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
        byte[] out = md.digest(input.getBytes("UTF-8"));
        return HexEncoder.toHex(out);
    }

    @SuppressWarnings("unused")
    private static String generateSalt() throws Exception {
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return HexEncoder.toHex(salt);
    }

    private static byte[] generateIv() throws Exception {
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);
        return iv;
    }

    private static class HexEncoder {
        static byte[] toByte(String hex) {
            byte[] bytes = new byte[hex.length() / 2];
            for (int index = 0; index < bytes.length; ++index) {
                bytes[index] = (byte) Integer.parseInt(hex.substring(index * 2, (index + 1) * 2), 16);
            }
            return bytes;
        }

        static String toHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(String.format("%02X", aByte));
            }
            return sb.toString();
        }
    }
}
