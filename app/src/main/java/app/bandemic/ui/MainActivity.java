package app.bandemic.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import app.bandemic.R;
import app.bandemic.fragments.ErrorMessageFragment;
import app.bandemic.fragments.NearbyDevicesFragment;
import app.bandemic.strict.service.BeaconCache;
import app.bandemic.strict.service.TracingService;

import app.bandemic.viewmodel.MainActivityViewModel;


@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity {

    boolean mBound = false;
    private static final String TAG = "MainActivity";
    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final String PREFERENCE_DATA_OK = "data_ok";
    private MainActivityViewModel mViewModel;
    private NearbyDevicesFragment nearbyDevicesFragment;
    private TracingService myService ;
    private TracingService.TracingServiceBinder serviceBinder;
    private ServiceConnection connection;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent intent = new Intent(MainActivity.this, Route.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

        // Bind to LocalService
//        Intent intent = new Intent(this, TracingService.class);
//        startService(intent);
//        bindService(intent, connection, Context.BIND_AUTO_CREATE);
//        //onShareStatusClick();


        connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                Log.i(TAG, "Service connected");
                //service = (TracingService.TracingServiceBinder) service;
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                TracingService.TracingServiceBinder binder = (TracingService.TracingServiceBinder) service;
                myService = binder.getService(); // should this go ?
                mBound = true;
                serviceBinder = (TracingService.TracingServiceBinder) service;



                serviceBinder.addServiceStatusListener(serviceStatusListener);
                serviceStatusListener.serviceStatusChanged(serviceBinder.getServiceStatus());

                serviceBinder.addNearbyDevicesListener();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(TAG, "Service disconnected");
                serviceBinder = null;          // tichada kupaona
                myService=null;
            }
        };
//        Intent intent=new Intent(this,TracingService.class);
//        startService(intent);
//        bindService(intent,connection,Context.BIND_AUTO_CREATE);

        nearbyDevicesFragment=new NearbyDevicesFragment();
        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        myService=new TracingService();
//        nearbyDevicesListener = distances -> runOnUiThread(() -> {
//            if (nearbyDevicesFragment != null) {
//                nearbyDevicesFragment.model.distances.setValue(distances);
//            }
//        });
//        serviceStatusListener = serviceStatus -> {
//            Log.i(TAG, "Service status: " + serviceStatus);
//            runOnUiThread(() -> {
//                if (serviceStatus == TracingService.STATUS_RUNNING) {
//                    if (nearbyDevicesFragment == null) {
//                        nearbyDevicesFragment = new NearbyDevicesFragment();
//                    }
//                    if (nearbyDevicesFragment.model != null) {
//                        nearbyDevicesFragment.model.distances.setValue(serviceBinder.getNearbyDevices());
//                    }
//                    nearbyDevicesFragment.skipAnimations();
//
//                    getSupportFragmentManager().beginTransaction()
//                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                            .replace(R.id.fragment_nearby_devices, nearbyDevicesFragment)
//                            .commit();
//                } else {
//                    String errorMessage = "";
//                    if (serviceStatus == TracingService.STATUS_BLUETOOTH_NOT_ENABLED) {
//                        errorMessage = getString(R.string.error_bluetooth_not_enabled);
//                    } else if (serviceStatus == TracingService.STATUS_LOCATION_NOT_ENABLED) {
//                        errorMessage = getString(R.string.error_location_not_enabled);
//                    }
//                    ErrorMessageFragment errorMessageFragment = ErrorMessageFragment.newInstance(errorMessage);
//                    getSupportFragmentManager().beginTransaction()
//                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                            .replace(R.id.fragment_nearby_devices, errorMessageFragment)
//                            .commit();
//                }
//            });
//        };

        SwipeRefreshLayout refreshLayout = findViewById(R.id.main_swipe_refresh_layout);
        refreshLayout.setOnRefreshListener(() -> {
            mViewModel.onRefresh();
        });
        mViewModel.eventRefresh().observe(this, refreshing -> {
            refreshLayout.setRefreshing(refreshing);
        });

        checkPermissions();

        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
       // if(sharedPref.getBoolean(PREFERENCE_DATA_OK, false)) {
          //  startActivity(new Intent(this, Instructions.class));
            Intent i=new Intent(this,Instructions.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
       // }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        else {
            startTracingService();
        }
    }

    private void startTracingService() {
        Intent intent = new Intent(this, TracingService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findViewById(R.id.card_permission_required).setVisibility(View.GONE);
                    // Start background service
                    startTracingService();
                } else {
                    // Don't start the discovery service
                    findViewById(R.id.card_permission_required).setVisibility(View.VISIBLE);
                    findViewById(R.id.ask_permission).setOnClickListener(view -> checkPermissions());
                }
                return;
            }
        }
    }
//    @Override
//    public void onStart(){
//        super.onStart();
//
//        Intent intent=new Intent(MainActivity.this,Instructions.class);
//        startActivity(intent);
//
//    }

    private TracingService.ServiceStatusListener serviceStatusListener = serviceStatus -> {
        Log.i(TAG, "Service status: " + serviceStatus);
        runOnUiThread(() -> {
            if (serviceStatus == TracingService.STATUS_RUNNING) {
                if (nearbyDevicesFragment == null) {
                    nearbyDevicesFragment = new NearbyDevicesFragment();
                }
                if (nearbyDevicesFragment.model != null) {
                    nearbyDevicesFragment.model.distances.setValue(serviceBinder.getNearbyDevices());
                }
                nearbyDevicesFragment.skipAnimations();

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.fragment_nearby_devices, nearbyDevicesFragment)
                        .commit();
            } else {
                String errorMessage = "";
                if (serviceStatus == TracingService.STATUS_BLUETOOTH_NOT_ENABLED) {
                    errorMessage = getString(R.string.error_bluetooth_not_enabled);
                } else if (serviceStatus == TracingService.STATUS_LOCATION_NOT_ENABLED) {
                    errorMessage = getString(R.string.error_location_not_enabled);
                }
                ErrorMessageFragment errorMessageFragment = ErrorMessageFragment.newInstance(errorMessage);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.fragment_nearby_devices, errorMessageFragment)
                        .commit();
            }
        });
    };

    BeaconCache.NearbyDevicesListener nearbyDevicesListener = new BeaconCache.NearbyDevicesListener() {
        @Override
        public void onNearbyDevicesChanged(double[] distances) {
            runOnUiThread(() -> {
                if (nearbyDevicesFragment != null) {
                    nearbyDevicesFragment.model.distances.setValue(distances);
                }
            });
        }
    };
// ndakachinja kubva kuna onStop to onDestroy
    @Override
    protected void onDestroy() {

        serviceBinder.removeNearbyDevicesListener(nearbyDevicesListener);
        serviceBinder.removeServiceStatusListener(serviceStatusListener);
        unbindService(connection);
        super.onDestroy();

    }
//    @Override
//    public void onRestart(){
//        super.onRestart();
//        Toast.makeText(MainActivity.this,"Welcome...!", Toast.LENGTH_LONG).show();
//        startActivity(new Intent(MainActivity.this,Instructions.class));
//
//
//    }


//    private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            Log.i(TAG, "Service connected");
//            serviceBinder = (TracingService.TracingServiceBinder) service;
//
//            serviceBinder.addServiceStatusListener(serviceStatusListener);
//            serviceStatusListener.serviceStatusChanged(serviceBinder.getServiceStatus());
//
//            serviceBinder.addNearbyDevicesListener(nearbyDevicesListener);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            Log.i(TAG, "Service disconnected");
//            serviceBinder = null;
//        }
//    };

}
