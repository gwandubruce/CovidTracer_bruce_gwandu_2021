package app.bandemic.strict.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import java.util.Arrays;

public class BleAdvertiser {
    private static final String LOG_TAG = "BleAdvertiser";
    private final Context context;

    private byte[] broadcastData;
    private  BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private AdvertiseCallback bluetoothAdvertiseCallback;
    private BluetoothGattServer mBluetoothGattServer;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BleAdvertiser(BluetoothManager bluetoothManager, Context context) {
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        }
        this.bluetoothManager = bluetoothManager;
        this.context = context;
    }

    public void setBroadcastData(byte[] broadcastData) {
        this.broadcastData = broadcastData;

        //Restart advertising so that mac address changes
        //Otherwise the device could be recognized that way even though the UUID has changed
        if(bluetoothAdvertiseCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                restartAdvertising();
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void restartAdvertising() {
        stopAdvertising();
        startAdvertising();
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    public void startAdvertising() {
        Log.i(LOG_TAG, "Starting Advertising");
        AdvertiseSettings settings = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setConnectable(true)
                    .setTimeout(0)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .build();
        }

        AdvertiseData data = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            data = new AdvertiseData.Builder()
                    .setIncludeTxPowerLevel(true)
                    .addServiceUuid(new ParcelUuid(BandemicProfile.BANDEMIC_SERVICE))
                    .setIncludeDeviceName(false)
                    .build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdvertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    Log.i(LOG_TAG, "Advertising onStartSuccess");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    super.onStartFailure(errorCode);
                    Log.e(LOG_TAG, "Advertising onStartFailure: " + errorCode);
                    // TODO
                }
            };
        }


        //Set fixed device name to avoid being recognizable except though UUID
        //Name is not sent in advertisement anyway but can be requested though GATT Server
        //and I don't see any way to disable that
        bluetoothAdapter.setName("Phone");

        // TODO: check if null when launching with Bluetooth disabled

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeAdvertiser.startAdvertising(settings, data, bluetoothAdvertiseCallback);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothGattServer = bluetoothManager.openGattServer(context, mGattServerCallback);
        }
        if (mBluetoothGattServer == null) {
            Log.w(LOG_TAG, "Unable to create GATT server");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothGattServer.addService(BandemicProfile.createBandemicService());
        }
    }
    @RequiresPermission("android.permission.BLUETOOTH_ADMIN") // yaita zvekuwedzerwa............................
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("NewApi")
    public void stopAdvertising() {
        Log.d(LOG_TAG, "Stopping advertising");
        if (bluetoothAdvertiseCallback != null) {
            bluetoothLeAdvertiser.stopAdvertising(bluetoothAdvertiseCallback);
            bluetoothAdvertiseCallback = null;
        }
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.close();
            mBluetoothGattServer = null;
        }
    }

    private BluetoothGattServerCallback mGattServerCallback;

    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mGattServerCallback = new BluetoothGattServerCallback() {

                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(LOG_TAG, "BluetoothDevice CONNECTED: " + device);
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(LOG_TAG, "BluetoothDevice DISCONNECTED: " + device);
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                        BluetoothGattCharacteristic characteristic) {
                    if (BandemicProfile.HASH_OF_UUID.equals(characteristic.getUuid())) {
                        Log.i(LOG_TAG, "Read Hash of UUID");
                        Log.i(LOG_TAG, "Offset: " + offset);
                        mBluetoothGattServer.sendResponse(device,
                                requestId,
                                BluetoothGatt.GATT_SUCCESS,
                                offset,
                                Arrays.copyOfRange(broadcastData, offset, broadcastData.length));
                    } else {
                        // Invalid characteristic
                        Log.w(LOG_TAG, "Invalid Characteristic Read: " + characteristic.getUuid());
                        mBluetoothGattServer.sendResponse(device,
                                requestId,
                                BluetoothGatt.GATT_FAILURE,
                                0,
                                null);
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                                    BluetoothGattDescriptor descriptor) {
                    Log.w(LOG_TAG, "Unknown descriptor read request");
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattDescriptor descriptor,
                                                     boolean preparedWrite, boolean responseNeeded,
                                                     int offset, byte[] value) {
                    Log.w(LOG_TAG, "Unknown descriptor write request");
                    if (responseNeeded) {
                        mBluetoothGattServer.sendResponse(device,
                                requestId,
                                BluetoothGatt.GATT_FAILURE,
                                0,
                                null);
                    }
                }
            };
        }
    }
}
