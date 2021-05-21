package app.bandemic.strict.service;

import android.os.Handler;
import android.util.Log;

import androidx.collection.CircularArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.bandemic.strict.database.Beacon; // entity class
import app.bandemic.strict.repository.BroadcastRepository; // broadcast repo
import okio.ByteString;

public class BeaconCache {
    private static final String LOG_TAG = "BeaconCache";
    private final int MOVING_AVERAGE_LENGTH = 7;

    private final BroadcastRepository broadcastRepository;
    private final Handler serviceHandler;
    private final Map<ByteString, CacheEntry> cache = new HashMap<>();//-----------------------------------------------------------

    private List<NearbyDevicesListener> nearbyDevicesListeners = new ArrayList<>();//-----------------------------------------------

    public List<NearbyDevicesListener> getNearbyDevicesListeners() {
        return nearbyDevicesListeners;
    }

    public void setNearbyDevicesListeners(List<NearbyDevicesListener> nearbyDevicesListeners) {
        this.nearbyDevicesListeners = nearbyDevicesListeners;
    }

    public interface NearbyDevicesListener {
        void onNearbyDevicesChanged(double[] distances);
    }

    public BeaconCache(BroadcastRepository broadcastRepository, Handler serviceHandler) {
        this.broadcastRepository = broadcastRepository;
        this.serviceHandler = serviceHandler;
    }

    private void flush(ByteString hash) {
        Log.d(LOG_TAG, "Flushing distance to DB");
        CacheEntry entry = cache.get(hash);
        insertIntoDB(entry.hash, entry.lowestDistance, entry.firstReceived, entry.lastReceived - entry.firstReceived);
        cache.remove(hash);
    }

    public void flush() {
        for (ByteString hash : cache.keySet()) {
            flush(hash);
        }
    }

    public void addReceivedBroadcast(byte[] hash, double distance) {
        broadcastRepository.insertBeacon(new Beacon(     // performed drastic changes here
                hash,
                new Date(System.currentTimeMillis()),

                distance
        ));
        ByteString hashString = ByteString.of(hash);
        CacheEntry entry = cache.get(hashString);

        if (entry == null) {
            // new unknown broadcast
            entry = new CacheEntry();
            cache.put(hashString, entry); // PLACED IT RIGHT AT THE BOTTOM
            entry.hash = hash;
            entry.firstReceived = System.currentTimeMillis();
           // cache.put(hashString, entry);
        }

        entry.lastReceived = System.currentTimeMillis();

        // postpone flushing
        serviceHandler.removeCallbacks(entry.flushRunnable);
        // flush after three minutes
        long FLUSH_AFTER_MILLIS = 1000 * 60 * 3;
        serviceHandler.postDelayed(entry.flushRunnable, FLUSH_AFTER_MILLIS);

        CircularArray<Double> distances = entry.distances;
        distances.addFirst(distance);
        if (distances.size() == MOVING_AVERAGE_LENGTH) {

            //calculate moving average
            double avg = entry.getAverageDistance();
            if (avg < entry.lowestDistance) {
                //insert new lowest value to DB
                entry.lowestDistance = avg;
                //insertIntoDB(hash, avg);
            }
            distances.popLast();
        }

        sendNearbyDevices();

    }

    private void sendNearbyDevices() {
        double[] nearbyDevices = getNearbyDevices();

        for (NearbyDevicesListener listener : nearbyDevicesListeners) {
            listener.onNearbyDevicesChanged(nearbyDevices);
        }
    }

    public double[] getNearbyDevices() {
        double[] nearbyDevices = new double[cache.size()];
        int i = 0;
        for (CacheEntry entry : cache.values()) {
            nearbyDevices[i] = entry.getAverageDistance();
            i++;
        }
        return nearbyDevices;
    }

    private void insertIntoDB(byte[] hash, double distance, long startTime, long duration) {
        broadcastRepository.insertBeacon(new Beacon(
                hash,
                new Date(startTime),
                duration,
                distance
        ));
    }

    private class CacheEntry {
        long firstReceived;
        long lastReceived;
        byte[] hash;
        CircularArray<Double> distances = new CircularArray<>(MOVING_AVERAGE_LENGTH);
        double lowestDistance = Double.MAX_VALUE;
        Runnable flushRunnable = () -> flush(ByteString.of(hash));

        double getAverageDistance() {
            double avg = 0;
            for (int i = 0; i < distances.size(); i++) {
                avg += distances.get(i) / distances.size();
            }
            return avg;
        }
    }
}
