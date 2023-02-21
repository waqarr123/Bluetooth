package com.example.bluetoothsampleproject;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;

public class TestAESActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_aesactivity);

        byte[] message = "081122334455667788".getBytes();
        byte[] key = "8619C154D893C733D2888CE3937AF017".getBytes();
        try {
//
//            String text1 = new String(EncryptionDecryption.callData(key, message), StandardCharsets.UTF_8);
//            Log.d("log_w", "java value: " + text1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}