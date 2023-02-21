package com.example.bluetoothsampleproject;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESAuthentication {
    private static final String AES = "AES";
    private static final String AES_MODE = "AES/CBC/PKCS5Padding";

    public static byte[] generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return nonce;
    }

    public static byte[] encryptAES(byte[] data, byte[] key) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, AES);
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(AES_MODE);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Log.d("log_w", "NoSuchAlgorithmException:  " + e);
            throw new RuntimeException(e);
        }
        try {
            String text = new String(secretKeySpec.getEncoded(), StandardCharsets.UTF_8);
            Log.d("log_w", "secret key :  " + text);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        } catch (InvalidKeyException e) {
            Log.d("log_w", "ENCRYPT_MODE:  " + e);
            throw new RuntimeException(e);
        }
        try {
            return cipher.doFinal(data);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Log.d("log_w", "cipher: :  " + e);
            throw new RuntimeException(e);
        }
    }


    public static byte[] decryptAES(byte[] data, byte[] key) {
        Cipher cipher;
        byte[] ciphertext;

        try {
            cipher = Cipher.getInstance("ECB");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Log.d("log_w", "1  " + e);
            throw new RuntimeException(e);
        }
        try {
            SecureRandom rnd = new SecureRandom();
            byte[] iv = new byte[cipher.getBlockSize()];
            rnd.nextBytes(iv);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivParams);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            Log.d("log_w", "2  " + e);
            throw new RuntimeException(e);
        }
        try {
            ciphertext = cipher.doFinal(data);
            Log.d("log_w", "text  " + ciphertext);
            return cipher.doFinal(data);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Log.d("log_w", "3   " + e);
            throw new RuntimeException(e);
        }
    }

    public static boolean authenticate(byte[] sharedSecretKey, byte[] serverKey) {
        byte[] nonce = generateNonce();
        byte[] encryptedNonce;
        Log.d("log_w", "nonce " + Arrays.toString(nonce));
        try {
            encryptedNonce = encryptAES(nonce, sharedSecretKey);
            String text = new String(encryptedNonce, StandardCharsets.UTF_8);
            Log.d("log_w", "nonce " + text);
        } catch (Exception e) {
            Log.d("log_w", "exception 1" + e);
            throw new RuntimeException(e);

        }
        byte[] decryptedNonce;
        try {
            decryptedNonce = decryptAES(encryptedNonce, serverKey);
        } catch (Exception e) {
            Log.d("log_w", "exception 2" + e);
            throw new RuntimeException(e);
        }
        return Arrays.equals(nonce, decryptedNonce);
    }


    public static String decrypt(String value) {
        String key = "8619C154D893C733D2888CE3937AF017";
        String initVector;
        try {
            initVector = String.valueOf(16);
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return initVector + Arrays.toString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}


