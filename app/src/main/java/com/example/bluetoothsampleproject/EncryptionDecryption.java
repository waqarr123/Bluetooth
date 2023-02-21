package com.example.bluetoothsampleproject;

import android.util.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class EncryptionDecryption {

    private static final String ALGORITHM = "AES";
    private static final String MODE = "CBC";
    private static final String PADDING = "PKCS5Padding";
    private static final String PADDING_DECRYPT = "80000000000000000000000000000000000000000000";
    private static final String TRANSFORMATION = ALGORITHM + "/" + MODE + "/" + PADDING;
    private static final String D_TRANSFORMATION = ALGORITHM + "/" + MODE + "/" + PADDING_DECRYPT;
    private static final int IV_LENGTH = 16;

    public static byte[] encrypt(byte[] key, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        Log.d("log_w", "key length" + Arrays.toString(skeySpec.getEncoded()));
        Log.d("log_w", "key master" + key.length);
        //  a4 7e d2 33 51 95 ae c5 1a 52 41 34 c3 09 fe be
        final byte[] CDRIVES = new byte[]{(byte) 0xe0, 0x4f, (byte) 0xd0,
                0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b,
                0x30, 0x30, (byte) 0x9d};
        SecretKeySpec test = new SecretKeySpec(CDRIVES, "AES");

        Log.d("log_w", "key test" + Arrays.toString(clear));
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(clear);
    }

//    public static byte[] decrypt(byte[] key, byte[] encryptedData) throws Exception {
//        byte[] iv = extractIv(encryptedData);
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);
//        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
//        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
//        byte[] decryptedData;
//        decryptedData = cipher.doFinal(extractData(encryptedData));
//        return decryptedData;
//    }

    private static byte[] generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }

    public static byte[] extractIv(byte[] encryptedData) {
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(encryptedData, 0, iv, 0, IV_LENGTH);
        return iv;
    }

    private static byte[] extractData(byte[] encryptedData) {
        int dataLength = encryptedData.length - IV_LENGTH;
        byte[] data = new byte[dataLength];
        System.arraycopy(encryptedData, IV_LENGTH, data, 0, dataLength);
        return data;
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] decrypt(byte[] key, byte[] iv, byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        try {
            return cipher.doFinal(data);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }


    //    public static byte[] AESEncrypt(byte[] key, byte[] iv, byte[] data) throws Exception {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
//        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
//        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
//        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
//        ByteArrayOutputStream cipherStream = new ByteArrayOutputStream();
//        cipherStream.write(cipher.doFinal(data));
//        outputStream.write(cipherStream.toByteArray());
//        return outputStream.toByteArray();
//    }
    public static byte[] AESEncrypt(byte[] key, byte[] iv, byte[] data, String mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/" + mode + "/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {

            cipherOutputStream.write(data);
        }
        return outputStream.toByteArray();
    }

    public static byte[] encrypt(byte[] key, byte[] data, String mode) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/" + mode + "/NoPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
            byte[] abc = {53, 106, 78, 101, 99, 82, 81, 88, -12, 47, 28, -73, 68, 31, 108, -89};
            cipherOutputStream.write(abc);
        }
        return outputStream.toByteArray();
    }

    public static byte[] Rol(byte[] b) {
        byte[] r = new byte[b.length];
        byte carry = 0;

        for (int i = b.length - 1; i >= 0; i--) {
            int u = (b[i] & 0xff) << 1;
            r[i] = (byte) ((u & 0xff) + carry);
            carry = (byte) ((u & 0xff00) >>> 8);
        }

        return r;
    }

//    public static byte[] callData(byte[] key, byte[] data) {
//        EncryptionDecryption decryption = new EncryptionDecryption();
////        byte[] abc = decryption.AES_MAC(key, data);
//        return abc;
//    }


    private byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] decrypt(byte[] key, byte[] iv, byte[] data, String algorithm, String
            padding, String mode) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
        Cipher cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(cipher.update(data));
        out.write(cipher.doFinal());
        return out.toByteArray();
    }

    //    public static byte[] KEYA(String mobileUIDA, String MasterKey) throws Exception {
//        byte[] k0 = DefaultKey(MasterKey.getBytes());
//        byte[] k1 = FirstSubkey(k0);
//        byte[] k2 = SecondSubKey(k1);
//        String finalMessage = "01" + mobileUIDA + "80000000000000000000000000000000000000000000";
//        Log.d("log_w", "value of D:" + finalMessage);
//        Log.d("log_w", "length of k2:" + k2.length);
//        Log.d("log_w", "Mobile UID:" + mobileUIDA);
//        Log.d("log_w", "Master key:" + MasterKey);
//        byte[] finalMessageBytes = finalMessage.getBytes();
//        Log.d("log_w", " finalArray length:" + finalMessageBytes.length);
//        for (int j = 0; j < k2.length; j++)
//            finalMessageBytes[finalMessageBytes.length - 16 + j] ^= k2[j];
//        byte[] DK12 = AESEncrypt(MasterKey.getBytes(), finalMessage.getBytes());
//        return Arrays.copyOfRange(DK12, DK12.length - 16, DK12.length);
//    }
    public static byte[] KEYA(String mobileUIDA, String MasterKey) {

        byte[] k0, k1, k2;

        k0 = DefaultKey(hexStringToByteArray(MasterKey));
        k1 = FirstSubkey(k0);
        k2 = SecondSubKey(k1);
        String M = "01" + mobileUIDA;
        String padding = "80000000000000000000000000000000000000000000";
        byte[] D = hexStringToByteArray(M + padding);
        for (int j = 0; j < k2.length; j++)
            D[D.length - 16 + j] ^= k2[j];
        byte[] DK12;
        try {
            DK12 = AESEncrypt(hexStringToByteArray(MasterKey), new byte[16], D, "CBC");
            Log.d("log_w", "DK12:   " + MainActivity.byteArrayToHexString(DK12));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] Diversified = Arrays.copyOfRange(DK12, DK12.length - 16, DK12.length);
        Log.d("log_w", "Master diverse:" + MainActivity.byteArrayToHexString(Diversified));
        return Diversified;
    }

    public static byte[] DefaultKey(byte[] masterKey) {
        try {
            return AESEncrypt(masterKey, new byte[16], new byte[16], "CBC");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] FirstSubkey(byte[] key0) {
        byte[] key = Rol(key0); //If the most significant bit of L is equal to 0, K1 is the left-shift of L by 1 bit.
        if ((key0[0] & 0x80) == 0x80)
            key[15] ^= 0x87; // Otherwise, K1 is the exclusive-OR of const_Rb and the left-shift of L by 1 bit.

        return key;
    }

    static public byte[] xor_128(byte[] a, byte[] b) {
        byte[] out = new byte[16];

        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

    public static byte[] SecondSubKey(byte[] key1) {
        byte[] key = Rol(key1); // If the most significant bit of K1 is equal to 0, K2 is the left-shift of K1 by 1 bit.
        if ((key1[0] & 0x80) == 0x80)
            key[15] ^= 0x87; // Otherwise, K2 is the exclusive-OR of const_Rb and the left-shift of K1 by 1 bit.

        return key;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    byte[] k0, k1, k2;

    public byte[] AES_MAC(byte[] key, byte[] data) {
        // SubKey generation
        // step 1, AES-128 with key K is applied to an all-zero input block.
        byte[] L = new byte[0];
        try {
            L = AESEncrypt(key, new byte[16], new byte[16], "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        k0 = L;
        // step 2, K1 is derived through the following operation:
        byte[] FirstSubkey = Rol(L); // If the most significant bit of L is equal to 0, K1 is the left-shift of L by 1 bit.
        if ((L[0] & 0x80) == 0x80)
            FirstSubkey[15] ^= 0x87; // Otherwise, K1 is the exclusive-OR of const_Rb and the left-shift of L by 1 bit.

        k1 = FirstSubkey;
        // step 3, K2 is derived through the following operation:
        byte[] SecondSubkey = Rol(FirstSubkey); // If the most significant bit of K1 is equal to 0, K2 is the left-shift of K1 by 1 bit.
        if ((FirstSubkey[0] & 0x80) == 0x80)
            SecondSubkey[15] ^= 0x87; // Otherwise, K2 is the exclusive-OR of const_Rb and the left-shift of K1 by 1 bit.

        k2 = SecondSubkey;

        // MAC computing
        if (((data.length != 0) && (data.length % 16 == 0))) {
            // If the size of the input message block is equal to a positive multiple of the block size (namely, 128 bits),
            // the last block shall be exclusive-OR'ed with K1 before processing
            for (int j = 0; j < FirstSubkey.length; j++)
                data[data.length - 16 + j] ^= FirstSubkey[j];
        } else {
            // Otherwise, the last block shall be padded with 10^i
            byte[] padding = new byte[16 - data.length % 16];
            padding[0] = (byte) 0x80;


            data = new byte[data.length + padding.length];

            // and exclusive-OR'ed with K2
            for (int j = 0; j < SecondSubkey.length; j++)
                data[data.length - 16 + j] ^= SecondSubkey[j];
        }

        // The result of the previous process will be the input of the last encryption.
        byte[] encResult = new byte[0];
        try {
            encResult = AESEncrypt(key, new byte[16], new byte[16], "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        byte[] HashValue = Arrays.copyOfRange(encResult, encResult.length - 16, encResult.length);

        return HashValue;
    }

}

