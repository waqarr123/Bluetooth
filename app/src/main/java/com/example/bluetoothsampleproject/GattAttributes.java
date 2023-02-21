package com.example.bluetoothsampleproject;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    // public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    // public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
   /* public static String DEVICE_WRITE_DATA_SERVER ="0000ffe5-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_WRITE_DATA_DEVICE ="0000ffe9-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_READ_DATA_SERVER ="0000ffe0-0000-1000-8000-00805f9b34fb";
   public static String DEVICE_READ_DATA_DEVICE ="0000ffe4-0000-1000-8000-00805f9b34fb";
*/
    public static String DEVICE_WRITE_DATA_SERVER = "0000ffd0-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_WRITE_DATA_DEVICE = "0000ffd1-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_READ_DATA_SERVER = "0000fff0-0000-1000-8000-00809f9b34fb";
    public static String DEVICE_READ_DATA_DEVICE = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static String DEVICE_NOTIFICATION = "540810c2-d573-11e5-ab30-625662870761";
    public static String DEVICE_WRITE = "54080bd6-d573-11e5-ab30-625662870761";
    public static String DEVICE_WRITE_PROPERTY = "e68a5c09-aef8-4447-8f10-f3339898dee9";


    static {
        // Services.

        //    /attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        //   attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put(DEVICE_WRITE_DATA_SERVER, "Uart Write Service");
        attributes.put(DEVICE_READ_DATA_SERVER, "Uart Read Service");
        //Characteristics.
        //  / attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        //   attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(DEVICE_WRITE_DATA_DEVICE, "Uart Write Device");
        attributes.put(DEVICE_READ_DATA_DEVICE, "Uart Read Device");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}