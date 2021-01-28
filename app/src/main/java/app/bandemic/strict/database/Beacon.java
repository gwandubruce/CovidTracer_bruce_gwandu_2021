package app.bandemic.strict.database;

import android.util.Log;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Entity
public class Beacon {
    private static final String LOG_TAG = "Beacon";

    @PrimaryKey(autoGenerate = true)
    public int id = 0;
    public byte[] receivedHash;
    public byte[] receivedDoubleHash;
    public Date timestamp;
    public double distance;
    public long duration;

    public Beacon(byte[] receivedHash, Date timestamp, long duration, double distance) {
        this.receivedHash = receivedHash;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.wtf(LOG_TAG, e);
            throw new RuntimeException(e);
        }
        this.receivedDoubleHash = digest.digest(receivedHash);
        this.timestamp = timestamp;
        this.duration = duration;
        this.distance = distance;
    }
// constructor added kuti code isaputike...........................................................
    public Beacon(byte[] hash, Date date, int duration) {

        this.receivedHash = hash;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Log.wtf(LOG_TAG, e);
            throw new RuntimeException(e);
        }
        this.receivedDoubleHash = digest.digest(receivedHash);
        this.timestamp = date;
        this.duration = duration;

    }
}
