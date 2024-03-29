package app.bandemic.strict.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import java.util.Collections;

import static app.bandemic.strict.service.BandemicProfile.HASH_OF_UUID;

public class BleScanner {
    private static final String LOG_TAG = "BleScanner";
    private final BluetoothLeScanner bluetoothLeScanner;
    private final BluetoothAdapter bluetoothAdapter;
    private final BeaconCache beaconCache;
    private final Context context;
    private static int txPower;
    private static int rssi;
    private String deviceAddress;

    private ScanCallback bluetoothScanCallback;

    // Cache the hash of uuid returned by the device so we don't have to connect again every time ..................................
    private final LruCache<String, byte[]> macAddressCache = new LruCache<>(100);

    // Remember start time of connections so we don't start multiple connections per device.........................................
    private final LruCache<String, Long> connStartedTimeMap = new LruCache<>(100);

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BleScanner(BluetoothAdapter bluetoothAdapter, BeaconCache beaconCache, Context context) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        this.beaconCache = beaconCache;
        this.context = context;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startScanning() {
        Log.d(LOG_TAG, "Starting scan");

        bluetoothScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                Log.d(LOG_TAG, "onScanResult");

                ScanRecord record = result.getScanRecord();  // IN BleScanner ...can we manipulate this record


                // if there is no record, discard this packet......................................................................
                if (record == null) {
                    return;
                }

                //TODO The values here seem wrong
                txPower = -67;//record.getTxPowerLevel();
               // txPower = record.getTxPowerLevel(); //changed fro -65 to -75

                rssi = result.getRssi();

                BluetoothDevice device = result.getDevice();

                deviceAddress = device.getAddress();

                Log.i(LOG_TAG, "Found device with rssi=" + rssi + " txPower="+txPower);


                double distance = Math.pow(10d, ((double) txPower - rssi) / (10 * 2));

                //Cache UUID results for mac addresses we have already connected to
                // so we don't have to build up connections again.
                //Only send updated distance to BeaconCache
                byte[] hashOfUUIDCached = macAddressCache.get(deviceAddress);
                if (hashOfUUIDCached != null) {
                    Log.i(LOG_TAG, "Address seen already: " + deviceAddress + " New distance: " + distance);
                    beaconCache.addReceivedBroadcast(hashOfUUIDCached, distance);

                } else {

                    //Only start connection for the same device max every 5 sec so that we don't .......................................
                    // have multiple connections for the same device running .........................................................
                    Long connStartedTime = connStartedTimeMap.get(deviceAddress);
                    if (connStartedTime == null || (SystemClock.elapsedRealtime() - connStartedTime) > 5000) {
                        connStartedTimeMap.put(deviceAddress, SystemClock.elapsedRealtime());

                        Log.i(LOG_TAG, "Address not seen yet: " + deviceAddress + " Distance: " + distance);
                        BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
                            @Override
                            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                                Log.i(LOG_TAG, "State changed to " + newState);
                                if (newState == BluetoothGatt.STATE_CONNECTED&& status==BluetoothGatt.GATT_SUCCESS) { // added GATT_SUCCESS
                                    gatt.discoverServices();

                                }else if(status!=BluetoothGatt.GATT_SUCCESS){ // added this guy

                                    gatt.disconnect();
                                }
                            }

                            @Override
                            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                                Log.i(LOG_TAG, "Characteristic read " + characteristic.getUuid());
                                if (characteristic.getUuid().compareTo(HASH_OF_UUID) == 0) {
                                   byte[] hashOfUUID = characteristic.getValue();     // Bhora riripano...byte array rahakwa
                                   // byte[] hashOfUUID=record.getServiceData(new ParcelUuid(BandemicProfile.BANDEMIC_SERVICE));
                                    Log.i(LOG_TAG, "Read hash of uuid characteristic: " + bytesToHex(hashOfUUID));
                                    macAddressCache.put(deviceAddress, hashOfUUID);   // using mac address we input hash into map-bruce
                                    beaconCache.addReceivedBroadcast(hashOfUUID, distance);
                                    gatt.setCharacteristicNotification(characteristic,true); // not sure if it is right to add this guy-bruce
                                    BluetoothGattDescriptor desc=characteristic.getDescriptor(null); // are these necessary..if it fails try changing HASH_OF_UUID to BANDEMIC_SERVICE
                                    desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);// // are these necessary..
                                    gatt.writeDescriptor(desc); // // are these necessary..
                                    gatt.close();
                                }
                            }

                            @Override
                            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                                Log.i(LOG_TAG, "Services Discovered");
                                BluetoothGattCharacteristic characteristic = gatt.getService(BandemicProfile.BANDEMIC_SERVICE).getCharacteristic(HASH_OF_UUID);
                                if (characteristic != null) {
                                    Log.i(LOG_TAG, "Read characteristic...");
                                    gatt.readCharacteristic(characteristic);
                                } else {
                                    Log.i(LOG_TAG, "=============================");
                                    Log.e(LOG_TAG, "Did not find expected characteristic");
                                    Log.i(LOG_TAG, "Found these instead:");
                                    for (BluetoothGattService service : gatt.getServices()) {
                                        Log.i(LOG_TAG, "Service: " + service.getUuid());
                                        for (BluetoothGattCharacteristic gattCharacteristic : service.getCharacteristics()) {
                                            Log.i(LOG_TAG, "    Characteristic: " + gattCharacteristic.getUuid());
                                        }
                                    }
                                    Log.i(LOG_TAG, "=============================");
                                    gatt.close();
                                }
                            }

                            @Override
                            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

                                super.onCharacteristicChanged(gatt, characteristic);
                                if (characteristic.getUuid().compareTo(HASH_OF_UUID) == 0) {
                                    byte[] hashOfUUID = characteristic.getValue();     // Bhora riripano...byte array rahakwa
                                    Log.i(LOG_TAG, "Read hash of uuid characteristic: " + bytesToHex(hashOfUUID));
                                    macAddressCache.put(deviceAddress, hashOfUUID);   // using mac address we input hash into map-bruce
                                    beaconCache.addReceivedBroadcast(hashOfUUID, distance);
                                    gatt.setCharacteristicNotification(characteristic,true);}


                            }
                        };
                        device.connectGatt(context, true, gattCallback); // CHANGED FROM FALSE TO TRUE
                    }
                }

            }
        };

        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(BandemicProfile.BANDEMIC_SERVICE))
                .build();

        ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settingsBuilder.setLegacy(true);
        }

        bluetoothLeScanner.startScan(Collections.singletonList(filter), settingsBuilder.build(), bluetoothScanCallback);
    }

    public static byte getTxPower() {
        return (byte)txPower;
    }

    public static int getRssi() {
        return rssi;
    }

    public void stopScanning() {
        Log.d(LOG_TAG, "Stopping scanning");

        if (bluetoothScanCallback != null && bluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bluetoothLeScanner.stopScan(bluetoothScanCallback);
            }
        }

    }
}
