package com.example.badmintonbooking;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // You can replace this with your actual Mock API URL from mockapi.io or similar
    private static final String BASE_URL = "https://664a78ab-mock-api.mockapi.io/api/v1/"; 
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
