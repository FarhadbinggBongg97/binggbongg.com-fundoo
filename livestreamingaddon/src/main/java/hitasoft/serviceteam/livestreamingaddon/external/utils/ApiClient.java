package hitasoft.serviceteam.livestreamingaddon.external.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static hitasoft.serviceteam.livestreamingaddon.external.utils.Constants.API_URL;


/**
 * Created by hitasoft on 12/3/18.
 */

public class ApiClient {

    private static Retrofit retrofit = null;
    private static Retrofit uploadRetrofit = null;
    private static final int HTTP_TIMEOUT_MS = 10000;

    public static ApiInterface getWebService() { return getClient().create(ApiInterface.class); }

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .addInterceptor(logging)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .writeTimeout(3, TimeUnit.MINUTES)
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = null;
                            Request original = chain.request();
                            // Request customization: add request headers
                            Request.Builder requestBuilder = original.newBuilder();
                            request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getUploadClient() {
        if (uploadRetrofit == null) {
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .callTimeout(60, TimeUnit.MINUTES)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.MINUTES)
                    .writeTimeout(3, TimeUnit.MINUTES).build();

            uploadRetrofit = new Retrofit.Builder()
                    .client(httpClient)
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return uploadRetrofit;
    }
}
