package com.example.flask_test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class ServiceGenerator {

    private static final String BASE_URL = "http://127.0.0.1:5000/";

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}