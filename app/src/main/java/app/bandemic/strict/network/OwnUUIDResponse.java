package app.bandemic.strict.network;

import androidx.annotation.Keep;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.List;

import app.bandemic.strict.database.OwnUUID;
@Keep
@IgnoreExtraProperties
public class OwnUUIDResponse implements Serializable {
    private List<OwnUUID> data;

    public OwnUUIDResponse() {
    }

    public void setData(List<OwnUUID> data) {
        this.data = data;
    }

    public List<OwnUUID> getData() {
        return data;
    }
}
