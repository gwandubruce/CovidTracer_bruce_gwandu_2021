package app.bandemic.strict.network;

import java.util.List;

import app.bandemic.strict.database.OwnUUID;

public class OwnUUIDResponse {
    public List<OwnUUID> data;

    public List<OwnUUID> getData() {
        return data;
    }

    public void setData(List<OwnUUID> data) {
        this.data = data;
    }
}
