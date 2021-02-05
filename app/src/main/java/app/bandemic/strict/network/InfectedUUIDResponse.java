package app.bandemic.strict.network;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.List;

import app.bandemic.strict.database.InfectedUUID;
@IgnoreExtraProperties
public class InfectedUUIDResponse {
    public InfectedUUIDResponse() {
    }

    public InfectedUUIDResponse(List<InfectedUUID> data) {
        this.data = data;
    }

    public List<InfectedUUID> data;

    public List<InfectedUUID> getData() {
        return data;
    }

    public void setData(List<InfectedUUID> data) {
        this.data = data;
    }
}
