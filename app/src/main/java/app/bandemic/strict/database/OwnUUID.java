package app.bandemic.strict.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

@Entity
public class OwnUUID {
    @NonNull
    @PrimaryKey
    private UUID ownUUID;
    private Date timestamp;

    public OwnUUID() {
    }

    @NonNull
    public UUID getOwnUUID() {
        return ownUUID;
    }

    public void setOwnUUID(@NonNull UUID ownUUID) {
        this.ownUUID = ownUUID;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public OwnUUID(@NonNull UUID ownUUID, Date timestamp) {
        this.ownUUID = ownUUID;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OwnUUID{" +
                "ownUUID=" + ownUUID +
                ", timestamp=" + timestamp +
                '}';
    }
}
