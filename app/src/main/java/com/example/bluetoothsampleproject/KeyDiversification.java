package com.example.bluetoothsampleproject;

import static com.example.bluetoothsampleproject.Utils.byteArrayToHexString;
import static com.example.bluetoothsampleproject.Utils.hexStringToByteArray;

import java.math.BigInteger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class KeyDiversification {

    public static String commonInKeyGeneration(String masterKey, String constRb) {

        byte[] encryptedData = new byte[0];
        try {
            encryptedData = SimpleCrypto.encrypt(hexStringToByteArray(masterKey), hexStringToByteArray(constRb));
        } catch (Exception e) {
            e.printStackTrace();
        }
        BigInteger encryptedBigintData = new BigInteger(encryptedData);
        String encryptedBigintDataBinary = encryptedBigintData.toString(2);
        if (encryptedBigintDataBinary.length() != 128) {
            int iteration = 128 - encryptedBigintDataBinary.length();
            for (int i = 0; i < iteration; i++) {
                encryptedBigintDataBinary = '0' + encryptedBigintDataBinary;
            }
        }
        return encryptedBigintDataBinary;
    }

    public static String generateKeyOne(String masterKey) {
        String constRb = "00000000000000000000000000000000";

        String k1 = "";
        String hexString = "";
        try {
            String encryptedBigintDataBinary = commonInKeyGeneration(masterKey, constRb);
            if (encryptedBigintDataBinary.charAt(0) == '0') {
                int encryptedBigintDataBinaryLength = encryptedBigintDataBinary.length();
                String leftShift = encryptedBigintDataBinary.substring(1, encryptedBigintDataBinaryLength) + '0';

                hexString = new BigInteger(leftShift, 2).toString(16);
                k1 = hexString;

            } else {
                k1 = hexString;
            }
            int reducedValue;
            if (k1.length() > 32) {
                reducedValue = k1.length() - 32;
                k1 = k1.substring(reducedValue, k1.length());
            } else if (k1.length() < 32) {
                reducedValue = 32 - k1.length();
                for (int i = 0; i < reducedValue; i++) {
                    k1 = "0" + k1;
                }
            }

            //----------number xor with 0 is number--------------

        } catch (Exception e) {
            e.printStackTrace();
        }
        return k1;
    }

    public static String generateKeyTwo(String masterKey) {

        String constRb = "00000000000000000000000000000000";

        String hexString;
        String k2;

        String keyOne = generateKeyOne(masterKey);
        byte[] k1Byte = hexStringToByteArray(keyOne);
        BigInteger k1ByteBigInt = new BigInteger(k1Byte);
        String k1ByteBigIntString = k1ByteBigInt.toString(2);

        if (k1ByteBigIntString.length() != 128) {
            int iteration = 128 - k1ByteBigIntString.length();
            for (int i = 0; i < iteration; i++) {
                k1ByteBigIntString = '0' + k1ByteBigIntString;
            }
        }

        if (k1ByteBigIntString.charAt(0) == '0') {
            int encryptedBigintDataBinaryLength = k1ByteBigIntString.length();
            String leftShift = k1ByteBigIntString.substring(1, encryptedBigintDataBinaryLength) + '0';
            hexString = new BigInteger(leftShift, 2).toString(16);
            k2 = hexString;
        } else {
            byte[] leftShiftDataK2 = leftshift_onebit(hexStringToByteArray(keyOne));
            byte[] result = xor_128(leftShiftDataK2, constRb.getBytes());
            k2 = byteArrayToHexString(result);
        }
        return k2;
    }


    public static String keyDiversification(String masterKey, String uida) {
        String fullKeyAHexString = "";
        boolean isPadding = false;
        String M = "01" + uida;
        String padding = "80000000000000000000000000000000000000000000";
        String D = M + padding;
        String xorString = "";
        //last 16 bytes xor with k2,
        String keyTwo = generateKeyTwo(masterKey);
        String dK12 = D.substring(0, 32) + keyTwo;
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(hexStringToByteArray(masterKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(1, skeySpec);
            fullKeyAHexString = byteArrayToHexString(cipher.doFinal(hexStringToByteArray(dK12)));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return fullKeyAHexString;
    }


    static public byte[] leftshift_onebit(byte[] input) {
        byte[] output = new byte[16];
        byte overflow = 0;
        for (int i = 15; i >= 0; i--) {
            output[i] = (byte) (input[i] << 1);
            output[i] |= overflow;
            overflow = (byte) (input[i] < 0 ? 1 : 0);
        }
        return output;
    }

    static public byte[] xor_128(byte[] a, byte[] b) {
        byte[] out = new byte[16];

        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ b[i]);
        }
        return out;
    }

}
