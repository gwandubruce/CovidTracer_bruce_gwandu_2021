package app.bandemic.strict.network;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

import app.bandemic.strict.database.OwnUUID;
@IgnoreExtraProperties
public class OwnUUIDResponse {
    public OwnUUIDResponse() {
    }

    public OwnUUIDResponse(List<OwnUUID> data) {
        this.data = data;
    }

    public List<OwnUUID> data;


    public List<OwnUUID> getData() {
        return data;
    }

    public void setData(List<OwnUUID> data) {
        this.data = data;
    }
}
