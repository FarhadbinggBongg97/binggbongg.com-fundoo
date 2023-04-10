package com.app.binggbongg.utils;

import com.app.binggbongg.model.GetSet;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hitasoft on 12/3/18.
 */

public class ApiClient {

    private static Retrofit retrofit = null;
    private static final int HTTP_TIMEOUT_MS = 10000;
    private static final int HTTP_READ_TIMEOUT = 30000;

    public static Retrofit getClient() {

        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .readTimeout(HTTP_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .addInterceptor(logging)
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(@NotNull Chain chain) throws IOException {
                            Request request = null;
                            Request original = chain.request();
                            // Request customization: add request headers
                            Request.Builder requestBuilder = original.newBuilder()
                                    .addHeader(Constants.TAG_AUTHORIZATION,
                                            GetSet.getAuthToken() != null ? GetSet.getAuthToken() : "");

                            request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.API_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
