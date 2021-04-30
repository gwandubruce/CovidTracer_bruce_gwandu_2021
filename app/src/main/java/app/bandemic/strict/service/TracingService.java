package app.bandemic.strict.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import app.bandemic.R;
import app.bandemic.strict.database.OwnUUID;
import app.bandemic.strict.repository.BroadcastRepository;
import app.bandemic.strict.service.BeaconCache.NearbyDevicesListener;
import app.bandemic.ui.MainActivity;

import static android.bluetooth.BluetoothAdapter.*;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class TracingService extends Service {

    public TracingService(){

    }
    private static final String LOG_TAG = "TracingService";
    private static final String DEFAULT_NOTIFICATION_CHANNEL = "ContactTracing";
    private static final int NOTIFICATION_ID = 1;

    public static final int BLUETOOTH_SIG = 2220;
    public static final int HASH_LENGTH = 26;
    public static final int BROADCAST_LENGTH = HASH_LENGTH + 1;
    private static final int UUID_VALID_TIME = 1000 * 60 * 30; //ms * sec * 30 min

    private Looper serviceLooper;
    private Handler serviceHandler;
    private BleScanner bleScanner;
    private BleAdvertiser bleAdvertiser;
    // -----------------------------------
    public static BeaconCache beaconCache;

    private UUID currentUUID;

    private BroadcastRepository broadcastRepository;

    private final IBinder mBinder = new TracingServiceBinder();

    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_STARTING = 1;
    public static final int STATUS_BLUETOOTH_NOT_ENABLED = 2;
    public static final int STATUS_LOCATION_NOT_ENABLED = 3;

    private int serviceStatus = STATUS_STARTING;
   private BluetoothManager bluetoothManager ;
   private BluetoothAdapter bluetoothAdapter;
    private static List<NearbyDevicesListener> listener;
    static List<ServiceStatusListener> serviceStatusListeners = new ArrayList<>();
    private LocationManager locationManager ;
    private BroadcastReceiver stateReceiver;

    public TracingService(BeaconCache beaconCache) {
        TracingService.beaconCache = beaconCache;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        broadcastRepository = new BroadcastRepository(this.getApplication());
        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        bleAdvertiser=new BleAdvertiser(bluetoothManager,this);


        stateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                assert action != null;
                if (action.equals(ACTION_STATE_CHANGED)) {
                    final int bluetoothState = intent.getIntExtra(EXTRA_STATE, ERROR);

                    //There are also TURNING_ON and TURNING_OFF states, skip those
                    if (bluetoothState == STATE_ON || bluetoothState == STATE_OFF) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            tryStartingBluetooth();
                        }
                    }
                }

                if (action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        tryStartingBluetooth();
                    }
                }
            }
        };


        HandlerThread thread = new HandlerThread("TrackerHandler", Thread.NORM_PRIORITY);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new Handler(serviceLooper);  //...............ndachazvichinja
        beaconCache = new BeaconCache(broadcastRepository, serviceHandler);
        bleScanner = new BleScanner(bluetoothAdapter, beaconCache, this);
        listener=beaconCache.getNearbyDevicesListeners();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        }
        registerReceiver(stateReceiver, filter);
    }

    public  class TracingServiceBinder extends Binder {
//private final static String demobho="Tahwinha"; bho zvekuti,coz its a final within an inner class
//private int bruce; yakabvuma when class was static
        public  TracingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TracingService.this;
        }

        public TracingServiceBinder() {
        }

        public int getServiceStatus() {
            return serviceStatus;
        }

        public void addServiceStatusListener(ServiceStatusListener listener) {
            serviceStatusListeners.add(listener);
        }

        public void removeServiceStatusListener(ServiceStatusListener listener) {

            serviceStatusListeners.remove(listener);
        }

        public double[] getNearbyDevices() {
            return beaconCache.getNearbyDevices();
        }

        public void addNearbyDevicesListener() {

            beaconCache.getNearbyDevicesListeners().addAll(listener);
        }

        public void removeNearbyDevicesListener(NearbyDevicesListener listener) {

            beaconCache.getNearbyDevicesListeners().remove(listener);
        }
    }


//    public int getServiceStatus() {
//        return serviceStatus;
//    }
//
//    public void addServiceStatusListener(ServiceStatusListener listener) {
//        serviceStatusListeners.add(listener);
//    }
//
//    public void removeServiceStatusListener(ServiceStatusListener listener) {
//
//        serviceStatusListeners.remove(listener);
//    }
//
//    public double[] getNearbyDevices() {
//        return beaconCache.getNearbyDevices();
//    }
//
//    public void addNearbyDevicesListener() {
//
//        beaconCache.getNearbyDevicesListeners().addAll(listener);
//    }
//
//    public void removeNearbyDevicesListener(NearbyDevicesListener listener) {
//
//        beaconCache.getNearbyDevicesListeners().remove(listener);
//    }


    public interface ServiceStatusListener {
        void serviceStatusChanged(int serviceStatus);
    }

    private void setServiceStatus(int serviceStatus) {
        this.serviceStatus = serviceStatus;
        for (ServiceStatusListener listener : serviceStatusListeners) {
            listener.serviceStatusChanged(serviceStatus);
        }
    }

    private final Runnable regenerateUUID = () -> {
        Log.i(LOG_TAG, "Regenerating UUID");

        currentUUID = UUID.randomUUID();
       long time = System.currentTimeMillis();

        broadcastRepository.insertOwnUUID(new OwnUUID(currentUUID, new Date(time)));

        // Convert the UUID to its SHA-256 hash-------------------------------------------------------------------------------------------
        ByteBuffer inputBuffer = ByteBuffer.wrap(new byte[/*Long.BYTES*/ 8 * 2]);
        inputBuffer.putLong(0, currentUUID.getMostSignificantBits());
        inputBuffer.putLong(4, currentUUID.getLeastSignificantBits());

        byte[] broadcastData;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            broadcastData = digest.digest(inputBuffer.array());
            broadcastData = Arrays.copyOf(broadcastData, BROADCAST_LENGTH);
            broadcastData[HASH_LENGTH] = getTransmitPower();
        } catch (NoSuchAlgorithmException e) {
            Log.wtf(LOG_TAG, "Algorithm not found", e);
            throw new RuntimeException(e);
        }

       // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleAdvertiser.setBroadcastData(broadcastData);
        //}

        serviceHandler.removeCallbacks(this.regenerateUUID);
        serviceHandler.postDelayed(this.regenerateUUID, UUID_VALID_TIME);
    };

    private byte getTransmitPower() {
        // TODO look up transmit power for current device
        return (byte) -65;
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        broadcastRepository = new BroadcastRepository(this.getApplication());
//        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
//        HandlerThread thread = new HandlerThread("TrackerHandler", Thread.NORM_PRIORITY);
//        thread.start();
//
//        // Get the HandlerThread's Looper and use it for our Handler
//        serviceLooper = thread.getLooper();
//        serviceHandler = new Handler(serviceLooper);
//        beaconCache = new BeaconCache(broadcastRepository, serviceHandler);
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            filter.addAction(LocationManager.MODE_CHANGED_ACTION);
//        }
//        registerReceiver(stateReceiver, filter);
//    }

    @TargetApi(26)
    private void createChannel(NotificationManager notificationManager) {
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel mChannel = new NotificationChannel(DEFAULT_NOTIFICATION_CHANNEL, DEFAULT_NOTIFICATION_CHANNEL, importance);
        mChannel.setDescription(getText(R.string.notification_channel).toString());
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(mChannel);
    }

    private void runAsForgroundService() {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel(notificationManager);

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this,
                DEFAULT_NOTIFICATION_CHANNEL)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setVibrate(null)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

   public void stopBluetooth() {
        if (bleAdvertiser != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bleAdvertiser.stopAdvertising();
            }
            bleAdvertiser = null;
        }
        if (bleScanner != null) {
            bleScanner.stopScanning();
            bleScanner = null;
        }

        //Stop regenerating UUIDs while bluetooth is not running
        serviceHandler.removeCallbacks(this.regenerateUUID);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
   public void tryStartingBluetooth() {
        Log.i(LOG_TAG, "Try starting bluetooth advertisement + scanning");
        if (serviceStatus == STATUS_RUNNING) {
            Log.i(LOG_TAG, "Bluetooth is already running, restarting");
            //If bluetooth is already running, stop it again and try to restart
            //This is done to check that bluetooth and location is enabled again and
            //set an error state otherwise

            stopBluetooth();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
           bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        }
       assert bluetoothManager != null;
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Log.i(LOG_TAG, "Bluetooth not enabled");
            setServiceStatus(STATUS_BLUETOOTH_NOT_ENABLED);
            return;
        }


        assert locationManager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //I have read this is not actually required on all devices, but I have not found a way
            //to check if it is required.
            //If location is not enabled the BLE scan fails silently (scan callback is never called)
            if (!locationManager.isLocationEnabled()) {
                Log.i(LOG_TAG, "Location not enabled (API>=P check)");
                setServiceStatus(STATUS_LOCATION_NOT_ENABLED);
                return;
            }
        } else {
            //Not sure if this is the correct check, gps is not really required, but passive provider
            //does not seem to be enough
            if (!locationManager.getProviders(true).contains(LocationManager.GPS_PROVIDER)) {
                Log.i(LOG_TAG, "Location not enabled (API<P check)");
                setServiceStatus(STATUS_LOCATION_NOT_ENABLED);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          bleScanner = new BleScanner(bluetoothAdapter, beaconCache, this);
        }
       bleAdvertiser = new BleAdvertiser(bluetoothManager, this);

        //TODO this can lead to UUID being regenerated more often, do a check somewhere for that
        new Thread(regenerateUUID).start(); // tomboisa start() timboona...tadzisa regenerateUUID.run();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleAdvertiser.startAdvertising();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScanner.startScanning();
        }
        setServiceStatus(STATUS_RUNNING);
    }

//    private final BroadcastReceiver stateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//
//            assert action != null;
//            if (action.equals(ACTION_STATE_CHANGED)) {
//                final int bluetoothState = intent.getIntExtra(EXTRA_STATE, ERROR);
//
//                //There are also TURNING_ON and TURNING_OFF states, skip those
//                if (bluetoothState == STATE_ON || bluetoothState == STATE_OFF) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                        tryStartingBluetooth();
//                    }
//                }
//            }
//
//            if (action.equals(LocationManager.MODE_CHANGED_ACTION)) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    tryStartingBluetooth();
//                }
//            }
//        }
//    };

    @Override
    public void onDestroy() {
        stopBluetooth();
        beaconCache.flush();
        unregisterReceiver(stateReceiver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runAsForgroundService();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            tryStartingBluetooth();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
