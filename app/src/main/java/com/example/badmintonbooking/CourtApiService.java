package com.example.badmintonbooking;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface CourtApiService {
    // Assuming a mock API endpoint
    @GET("courts")
    Call<List<Court>> getCourts();
}
