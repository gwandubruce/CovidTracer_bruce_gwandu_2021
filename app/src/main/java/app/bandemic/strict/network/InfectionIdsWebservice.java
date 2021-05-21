package app.bandemic.strict.network;

import retrofit2.Call;
import retrofit2.http.GET;
// This web service should be the one we focus on during testing as it gets the json with infected ids
public interface InfectionIdsWebservice {



    @GET("IDs.json")
    Call<InfectedUUIDResponse> getInfectedUUIDResponse();
}
