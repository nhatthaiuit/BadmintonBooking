package com.example.badmintonbooking;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> historyList;
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView imgBack = findViewById(R.id.imgBackHistory);
        ListView listView = findViewById(R.id.listViewHistory);

        imgBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        historyList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);

        loadBookingHistory();
    }

    private void loadBookingHistory() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String courtName = document.getString("courtName");

                        if (courtName == null) {
                            courtName = document.getString("branch");
                        }

                        if (courtName == null) {
                            courtName = "Unknown Court";
                        }
                        String date = document.getString("date");
                        Long totalPrice = document.getLong("totalPrice");

                        Object selectedTimesObj = document.get("selectedTimes");

                        StringBuilder timesText = new StringBuilder();

                        if (selectedTimesObj instanceof ArrayList) {
                            ArrayList<?> selectedTimes = (ArrayList<?>) selectedTimesObj;

                            for (Object time : selectedTimes) {
                                timesText.append("- ").append(time.toString()).append("\n");
                            }
                        }

                        String formattedPrice = totalPrice != null
                                ? String.format("%,d", totalPrice).replace(',', '.') + " VND"
                                : "0 VND";

                        String item =
                                "Court: " + courtName + "\n" +
                                        "Date: " + date + "\n" +
                                        "Slots:\n" + timesText +
                                        "Total: " + formattedPrice;

                        historyList.add(item);
                    }

                    if (historyList.isEmpty()) {
                        historyList.add("No booking history yet.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}