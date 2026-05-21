package com.example.badmintonbooking;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView imgBack = findViewById(R.id.imgBackHistory);
        ListView listView = findViewById(R.id.listViewHistory);

        imgBack.setOnClickListener(v -> finish());

        // Dummy data for history
        ArrayList<String> historyList = new ArrayList<>();
        historyList.add("Branch 1 (Go Vap Dist)\nDate: 15/05/2026\nCourt 2 (18:00-19:00)\nTotal: 100.000 VND");
        historyList.add("Branch 2 (Binh Thanh Dist)\nDate: 10/05/2026\nCourt 1 (19:00-21:00)\nTotal: 200.000 VND");
        historyList.add("Branch 1 (Go Vap Dist)\nDate: 02/05/2026\nCourt 5 (06:00-08:00)\nTotal: 200.000 VND");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);
    }
}
