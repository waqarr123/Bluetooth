package com.example.bluetoothsampleproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends Activity {
    private static final String UUID_ID = "";
    Button bluetoothButton;
    private ArrayList<BleDeviceInfo> mBleDevices;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 0x88899;
    private static final long SCAN_PERIOD = 10000;
    private boolean scanning;
    String UIDA = "081122334455667788";
    String MKEY = "630A2FB8AB1615F4736C254D602A394E";
    private byte[] randA;
    private byte[] randA_;
    private byte[] randB;
    private byte[] randB_ = new byte[]{};
    private byte[] KEYA;
    int receiveDeviceId = -1;
    private boolean isFirst = true;
    BluetoothGattCharacteristic writeCharacteristic, writeCharacteristicProperty;
    BluetoothGattCharacteristic notificationCharacteristic;
    private int mDeviceIdOld = -1;
    public boolean connected = false;
    private Handler mHandler;
    List<BluetoothGattService> service;
    Timer mTimer;
    private AddDeviceDialog mAddDeviceDialog;
    public BluetoothLeService bluetoothLeService;
    BluetoothAdapter bluetoothAdapter;
    BleDeviceInfo mLastConnectDevInfo;
    TextView deviceName, deviceProperties, dataFromBle;
    Button getData;
    Date mToastDisconnectDate;

    EditText command;
    Step _step = Step.NONE;


    enum Step {
        GET, NONE, CHAL, RESP, DONE
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceName = findViewById(R.id.name);
        bluetoothButton = findViewById(R.id.bluetoothButton);
        dataFromBle = findViewById(R.id.dataFromBle);
        getData = findViewById(R.id.buttonDevice);
        deviceProperties = findViewById(R.id.address);
        command = findViewById(R.id.command);
        try {
            KEYA = EncryptionDecryption.KEYA(UIDA, MKEY);
            Log.d("log_w", "KEYA: " + byteArrayToHexString(KEYA));
        } catch (Exception e) {
            Log.d("log_w", "exception" + e);
            throw new RuntimeException(e);
        }

        if (Build.VERSION.SDK_INT >= 31) {
            // Android 12 (S)
            this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1);
        } else if (Build.VERSION.SDK_INT >= 23) {
            // Android 6 (M)
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        InitBle();
        mAddDeviceDialog = new AddDeviceDialog(MainActivity.this, mBleDevices);
        bluetoothButton.setOnClickListener(view -> AddDevice());
        getData.setOnClickListener(v -> {
            Intent intent = new Intent(this, TestAESActivity.class);
            startActivity(intent);
            service = bluetoothLeService.getSupportedGattServices();
//            for (BluetoothGattService gattService : service) {
//
//                List<BluetoothGattCharacteristic> gattCharacteristics =
//                        gattService.getCharacteristics();
//                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                    if (gattCharacteristic.getUuid().toString().equals(GattAttributes.DEVICE_WRITE)) {
//                        writeCharacteristic = gattCharacteristic;
//                        Log.d("log_w", "write" + writeCharacteristic.getUuid());
//                    } else if (gattCharacteristic.getUuid().toString().equals(GattAttributes.DEVICE_NOTIFICATION)) {
//                        notificationCharacteristic = gattCharacteristic;
//                        bluetoothLeService.setCharacteristicNotification(notificationCharacteristic, true);
//                        Log.d("log_w", "notification" + notificationCharacteristic.getUuid());
//                    }
//                }
////                for (BluetoothGattService gattService1 : service) {
////                    List<BluetoothGattCharacteristic> gattChar = gattService1.getCharacteristics();
////                    for (BluetoothGattCharacteristic gattChar1 : gattChar) {
////                        writeCharacteristic = gattChar1;
////                        notificationCharacteristic = gattChar1;
////                        bluetoothLeService.setCharacteristicNotification(notificationCharacteristic, true);
////                    }
////                }
//
//            }
//            sendByteData(command.getText().toString());
        });
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        checkPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, REQUEST_ENABLE_LOCATION);
        } else {
            startInit();
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    void startInit() {
        // Ensures Bluetooth is enabled on the device.
        if (!bluetoothAdapter.isEnabled()) {

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        mBleDevices = new ArrayList<>();
        mAddDeviceDialog = new AddDeviceDialog(MainActivity.this, mBleDevices);
        Window window = mAddDeviceDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        mAddDeviceDialog.SetOnConfirmInterface(this::ConnectDevice);
        scanLeDevice(true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        registerReceiver(mGattUpdateReceiver, intentFilter);
        mTimer = new Timer();
        mTimer.schedule(new MyTimerTask(), 20000, 20000);
    }

    private void AddDevice() {
        scanLeDevice(true);
        mAddDeviceDialog.show();
    }

    class MyTimerTask extends TimerTask {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            if (mLastConnectDevInfo != null) if (!connected) {
                if (mBleDevices != null && mBleDevices.size() > 0 && !mLastConnectDevInfo.GetDeviceName().equals("")) {
                    ConnectDevice(mLastConnectDevInfo);
                }
//                    scanLeDevice(true);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(() -> {
                scanning = false;
                bluetoothAdapter.stopLeScan(mLeScanCallback);
                //invalidateOptionsMenu();
            }, SCAN_PERIOD);

            scanning = true;
            mBleDevices.clear();
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            scanning = false;
            bluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //invalidateOptionsMenu();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void InitBle() {
        // TODO Auto-generated method stub
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.  Then you can
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "your_device_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "your_device_bluetooth_not_supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize()) {
                finish();
            }
            if (!connected) {
                bluetoothLeService.close();
                ConnectDevice(mLastConnectDevInfo);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };


    void ConnectDevice(BleDeviceInfo info) {
        if (info != null && bluetoothLeService != null) {
            if (mLastConnectDevInfo != null && mLastConnectDevInfo.GetMacAddress().equals(info.GetMacAddress()) && connected) {
                return;
            }
            bluetoothLeService.disconnect();
            bluetoothLeService.close();
            mLastConnectDevInfo = info;

            runOnUiThread(() -> {
                deviceName.setText(info.GetDeviceName());
                deviceProperties.setText(info.GetMacAddress());
            });

            bluetoothLeService.connect(info.GetMacAddress());
        }
    }

    // Device scan callback.
    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(() -> {
                if (device == null) return;
                boolean exit = false;
                if (device.getName() != null && !device.getName().equals("")) {
                    for (BleDeviceInfo info : mBleDevices) {
                        if (info.GetDeviceName().equals(device.getName()) && info.GetMacAddress().equals(device.getAddress())) {
                            exit = true;
                        }
                    }
                    if (!exit) {
                        mBleDevices.add(new BleDeviceInfo(device.getName(), device.getAddress()));
                    }
                    if (mAddDeviceDialog != null) mAddDeviceDialog.UpdateDevicesList(mBleDevices);
                }
            });
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothLeService != null && connected) {
            bluetoothLeService.disconnect();
        }
        unbindService(mServiceConnection);
        bluetoothLeService = null;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mDeviceIdOld = -1;
                isFirst = true;
                connected = true;
                Log.d("log_w", "connected" + action);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(MainActivity.this, "Device disconnect", Toast.LENGTH_SHORT).show();
                mDeviceIdOld = -1;
                isFirst = true;
                connected = false;
                GlobalStaticData.getInstance().setCurrentConnectDevInfo(null);
                Log.d("log_w", "disConnected" + action);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                GetGattServices(bluetoothLeService.getSupportedGattServices());
                connected = true;
                Toast.makeText(context, "SERVICES_DISCOVERED", Toast.LENGTH_LONG).show();
                GlobalStaticData.getInstance().setCurrentConnectDevInfo(mLastConnectDevInfo);
                Log.d("log_w", "SERVICES_DISCOVERED" + action);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                ReceiveData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Log.d("log_w", "data from BLE: " + byteArrayToHexString(data));
                byte[] value = StringToByteArray(UIDA);
                if (_step == Step.NONE) {
                    Log.d("log_w", "Sending UIDA" + UIDA);
                    SetStep(Step.GET);
                    sendStrData(value);
                } else if (_step == Step.GET) {
                    String Response = String.format("%02X", data[0]);
                    Log.d("log_w", "Response Type :" + Response);
                    if (data[0] == 0x41) {
                        SetStep(Step.CHAL);
                        Log.d("log_w", "Step.CHAL :");
                    } else {
                        SetStep(Step.GET);
                        Log.d("log_w", "Step.GET :");
                    }
                } else if (_step == Step.CHAL) {
                    Log.d("log_w", "Generating Mobile Challenge---------------------------------");
                    byte[] srcb;
                    srcb = data;
                    byte[] dstb;
                    dstb = Arrays.copyOfRange(srcb, 1, srcb.length);
                    randB = dstb;
                    Log.d("log_w", "length---------------------------------" + dstb.length);
                    randB_ = ByteBuffer.allocate(randB.length + 1).put(randB, 0, randB.length - 1).put(randB[0]).array();
                    Log.d("log_w", "RANDB_ length: " + Arrays.toString(randB_));
                    String RANDA = UniqueId.getUniqueKey(8);
                    randA = RANDA.getBytes();
                    Log.d("log_w", "RANDA length: " + RANDA);
                    randA_ = ByteBuffer.allocate(randA.length + 1).put(randA, 1, randA.length - 1).put(randA[0]).array();
                    byte[] temp;
                    temp = ByteBuffer.allocate(randA.length + randB_.length).put(randA).put(randB_).array();
                    Log.d("log_w", "temp length: " + temp.length);
                    Log.d("log_w", "temp before : " + Arrays.toString(temp));
                    byte[] encrypted;
                    try {
                        encrypted = EncryptionDecryption.encrypt(KEYA, temp, "ECB");
                        Log.d("log_w", "encrypted before : " + byteArrayToHexString(encrypted));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    temp = new byte[]{};
                    temp = ByteBuffer.allocate(temp.length - 1 + encrypted.length).put(temp).put(encrypted).array();
                    Log.d("log_w", "temp after : " + Arrays.toString(temp));
                    sendByteData(temp);
                } else if (_step == Step.RESP) {
                    Log.d("log_w", "Authentication---------------------------------");
                    if (data[0] == 0x43) {
                        Log.d("log_w", String.format("Device Response :%s", Arrays.toString(data)));
                        byte[] response;
                        response = data;
                        byte[] new_response = Arrays.copyOfRange(response, 1, response.length);
                        byte[] decrypt;
                        try {
                            decrypt = EncryptionDecryption.decrypt(KEYA, new byte[16], new_response);
                        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                                 InvalidKeyException | InvalidAlgorithmParameterException |
                                 IllegalBlockSizeException | BadPaddingException e) {
                            throw new RuntimeException(e);
                        }
                        Log.d("log_w", String.format("Device decrypt :%s", Arrays.toString(decrypt)));
                        byte[] A_ = Arrays.copyOfRange(decrypt, 0, 8);
                        // StatusMessage = "RandA* :" + byt
                    }
                }


            }
        }

        public byte[] StringToByteArray(String s) throws IllegalArgumentException {
            s = s.replace(" ", "");
            if ((s.length() & 1) == 1)
                throw new IllegalArgumentException("Odd string length when even string length is required.");
            int nBytes = s.length() / 2;
            byte[] a = new byte[nBytes];
            for (int i = 0; i < nBytes; i++)
                a[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
            return a;
        }


        public void SetStep(Step newStep) {
            _step = newStep;

        }

        String HexToBinary(String Hex) {
            String bin = new BigInteger(Hex, 16).toString(2);
            int inb = Integer.parseInt(bin);
            bin = String.format(Locale.getDefault(), "%08d", inb);
            return bin;
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void GetGattServices(List<BluetoothGattService> supportedGattServices) {
            if (supportedGattServices == null) return;

            for (BluetoothGattService gattService : supportedGattServices) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
//                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                    Log.d("log_w", "Characteristic: " + gattCharacteristic.getUuid());
//                }
                service = supportedGattServices;
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    //rx for write 002
                    if (gattCharacteristic.getUuid().toString().equals(GattAttributes.DEVICE_WRITE)) {
                        Log.d("log_w", "Write Characteristic: " + gattCharacteristic.getUuid());
                        writeCharacteristic = gattCharacteristic;
                    }
                    if (gattCharacteristic.getUuid().toString().equals(GattAttributes.DEVICE_WRITE_PROPERTY)) {
                        Log.d("log_w", "Write Characteristic: " + gattCharacteristic.getUuid());
                        writeCharacteristicProperty = gattCharacteristic;
                    }
                    // tx for notify 003
                    else if (gattCharacteristic.getUuid().toString().equals(GattAttributes.DEVICE_NOTIFICATION)) {
                        notificationCharacteristic = gattCharacteristic;
                        bluetoothLeService.setCharacteristicNotification(notificationCharacteristic, true);
                        Log.d("log_w", "Notification Characteristic: " + gattCharacteristic.getUuid());
                        Log.d("log_w", "Notification Descriptor: " + gattCharacteristic.getDescriptors().get(0).getUuid());
                    }
                }
//                for (BluetoothGattService gattService1 : supportedGattServices) {
//                    List<BluetoothGattCharacteristic> gattChar = gattService1.getCharacteristics();
//                    for (BluetoothGattCharacteristic gattChar1 : gattChar) {
//                        writeCharacteristic = gattChar1;
//                        notificationCharacteristic = gattChar1;
//                        bluetoothLeService.setCharacteristicNotification(notificationCharacteristic, true);
//                    }
//           /     }

            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        private void ReceiveData(byte[] bs) {
            if (bs == null) return;
            if (mDeviceIdOld <= 0 && bs.length > 0) {
                mDeviceIdOld = 1;
                receiveDeviceId = bs[0];
                isFirst = false;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendStrData(byte[] str) {
        if (writeCharacteristicProperty != null && bluetoothLeService != null && connected) {
            writeCharacteristicProperty.setValue(str);
            bluetoothLeService.writeCharacteristic(writeCharacteristicProperty);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendByteData(byte[] bs) {
        if (writeCharacteristic != null && bluetoothLeService != null && connected) {
            writeCharacteristic.setValue(bs);

            bluetoothLeService.writeCharacteristic(writeCharacteristic);
        } else if (bluetoothLeService != null) {
            mToastDisconnectDate = new Date();
            long diff = new Date().getTime() - mToastDisconnectDate.getTime();
            if (!scanning && !connected && diff > 35 * 1000) {
                Toast.makeText(this, "No device connection, please add device", Toast.LENGTH_SHORT).show();
                mToastDisconnectDate = new Date();
            }

        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String byteArrayToHexString(byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim().replace(" ", "");
    }

}

