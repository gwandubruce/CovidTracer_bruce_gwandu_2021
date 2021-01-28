package app.bandemic.strict.network;

import java.util.List;

import app.bandemic.strict.database.InfectedUUID;

public class InfectedUUIDResponse {
    public List<InfectedUUID> data;

    public List<InfectedUUID> getData() {
        return data;
    }

    public void setData(List<InfectedUUID> data) {
        this.data = data;
    }
}
