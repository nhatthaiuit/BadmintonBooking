package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

        // Setup Logout
        TextView textViewLogout = findViewById(R.id.textViewLogout);
        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear login status
                SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                // Go back to Login
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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
                if (response.isSuccessful() && response.body() != null) {
                    List<Court> courts = response.body();
                    
                    // Set adapter with data
                    adapter = new CourtAdapter(courts);
                    recyclerView.setAdapter(adapter);
                } else {
                    // Mạng lỗi hoặc link API chết -> Load dữ liệu giả
                    loadDummyData();
                }
            }

            @Override
            public void onFailure(Call<List<Court>> call, Throwable t) {
                Log.e("HomeActivity", "API Call Failed: " + t.getMessage());
                // Mạng lỗi hoặc link API chết -> Load dữ liệu giả
                loadDummyData();
            }
        });
    }

    private void loadDummyData() {
        Toast.makeText(this, "API Failed. Using Offline Mock Data", Toast.LENGTH_SHORT).show();
        java.util.List<Court> dummyCourts = new java.util.ArrayList<>();
        
        Court c1 = new Court();
        c1.setId(1);
        c1.setName("Yonex Badminton Court");
        c1.setAddress("123 Sport Street, District 1");
        c1.setPricePerHour(15.0);
        c1.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Badminton_court.png/640px-Badminton_court.png");
        dummyCourts.add(c1);

        Court c2 = new Court();
        c2.setId(2);
        c2.setName("Victor Arena");
        c2.setAddress("456 Pro Ave, District 3");
        c2.setPricePerHour(12.5);
        c2.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Badminton_court.png/640px-Badminton_court.png");
        dummyCourts.add(c2);

        Court c3 = new Court();
        c3.setId(3);
        c3.setName("Lining Center");
        c3.setAddress("789 Champion Blvd, District 7");
        c3.setPricePerHour(18.0);
        c3.setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Badminton_court.png/640px-Badminton_court.png");
        dummyCourts.add(c3);

        adapter = new CourtAdapter(dummyCourts);
        recyclerView.setAdapter(adapter);
    }
}
