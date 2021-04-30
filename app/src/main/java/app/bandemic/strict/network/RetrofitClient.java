//package app.bandemic.strict.network;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//// masports eku communicator ne server ari pano ......ndopanebasa apa....converting json objects
//public class RetrofitClient {
//
//    private static Retrofit retrofit = null;
//    private static Retrofit retrofit2 = null;
//    private static InfectionIdsWebservice webservice = null;
//    private static OwnIdsWebService webservice2 = null;
//    // there was an online something url
//    public static final String BASE_URL = "https://covidtracker-a95fb-default-rtdb.firebaseio.com/"; // does it have to be this or "/api/v1/app/result" ? no the methods should have that one
//    public static final String BASE_URL2 = "https://covidtracker-a95fb-default-rtdb.firebaseio.com/";
//
//    // should this be synchronized?
//    public static InfectionIdsWebservice getInfectionchainWebservice() {
//        if(retrofit == null || webservice == null) {
//            Gson gson = new GsonBuilder()
//                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
//                    .registerTypeHierarchyAdapter(byte[].class, new HexStringToByteArrayTypeAdapter())
//                    .create();
//            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            OkHttpClient okHttpClient= new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();
//
//
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(okHttpClient)
//                    .build();
//            webservice = retrofit.create(InfectionIdsWebservice.class);
//        }
//        return webservice;
//    }
//    public static OwnIdsWebService postOwnIdsToWebservice() {
//        if(retrofit2 == null || webservice2 == null) {
//            Gson gson = new GsonBuilder()
//                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
//                    .registerTypeHierarchyAdapter(byte[].class, new HexStringToByteArrayTypeAdapter())
//                    .create();
//            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            OkHttpClient okHttpClient= new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();
//
//
//            retrofit2 = new Retrofit.Builder()
//                    .baseUrl(BASE_URL2)
//                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .client(okHttpClient)
//                    .build();
//            webservice2 = retrofit2.create(OwnIdsWebService.class);
//        }
//        return webservice2;
//    }
//}
