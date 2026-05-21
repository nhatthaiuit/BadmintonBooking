package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourtAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewCourts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch data from API
        fetchCourts();
    }

    private void fetchCourts() {
        CourtApiService apiService = RetrofitClient.getClient().create(CourtApiService.class);
        Call<List<Court>> call = apiService.getCourts();
        
        call.enqueue(new Callback<List<Court>>() {
            @Override
            public void onResponse(Call<List<Court>> call, Response<List<Court>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<Court> courts = response.body();
                    
                    // Set adapter with data
                    adapter = new CourtAdapter(courts);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load data, using dummy data", Toast.LENGTH_SHORT).show();
                    showDummyData();
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Log.e("HomeActivity", "API Call Failed: " + t.getMessage());
                Toast.makeText(HomeActivity.this, "Network error, using dummy data", Toast.LENGTH_SHORT).show();
                showDummyData();
            }
        });
    }

    private void showDummyData() {
        List<Court> dummyCourts = new java.util.ArrayList<>();
        
        Court c1 = new Court();
        c1.setName("Yonex Badminton Court");
        c1.setAddress("123 Sport Street, District 1");
        c1.setPricePerHour(15.0);
        c1.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Badminton_court.svg/800px-Badminton_court.svg.png");
        dummyCourts.add(c1);

        Court c2 = new Court();
        c2.setName("Victor Arena");
        c2.setAddress("456 Pro Ave, District 3");
        c2.setPricePerHour(12.5);
        c2.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Badminton_court.svg/800px-Badminton_court.svg.png");
        dummyCourts.add(c2);
        
        Court c3 = new Court();
        c3.setName("Lining Center");
        c3.setAddress("789 Champion Blvd, District 7");
        c3.setPricePerHour(18.0);
        c3.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1d/Badminton_court.svg/800px-Badminton_court.svg.png");
        dummyCourts.add(c3);

        adapter = new CourtAdapter(dummyCourts);
        recyclerView.setAdapter(adapter);
    }
}
