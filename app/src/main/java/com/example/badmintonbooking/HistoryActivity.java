package com.example.badmintonbooking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<String> historyList;
    private ArrayList<String> bookingIdList;
    private ArrayList<String> statusList;

    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView imgBack = findViewById(R.id.imgBackHistory);
        listView = findViewById(R.id.listViewHistory);

        imgBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        historyList = new ArrayList<>();
        bookingIdList = new ArrayList<>();
        statusList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (bookingIdList.isEmpty() || position >= bookingIdList.size()) {
                return;
            }

            String bookingId = bookingIdList.get(position);
            String status = statusList.get(position);

            if ("cancelled".equalsIgnoreCase(status)) {
                Toast.makeText(this, "This booking is already cancelled", Toast.LENGTH_SHORT).show();
                return;
            }

            showCancelDialog(bookingId);
        });

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
                .get(Source.SERVER)
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    historyList.clear();
                    bookingIdList.clear();
                    statusList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();

                        String branchName = document.getString("branchName");
                        if (branchName == null || branchName.isEmpty()) {
                            branchName = "Unknown Branch";
                        }

                        String date = document.getString("date");
                        if (date == null || date.isEmpty()) {
                            date = "Unknown Date";
                        }

                        Long totalPrice = document.getLong("totalPrice");

                        String paymentMethod = document.getString("paymentMethod");
                        if (paymentMethod == null || paymentMethod.isEmpty()) {
                            paymentMethod = "Not recorded";
                        }

                        String status = document.getString("status");
                        if (status == null || status.isEmpty()) {
                            status = "confirmed";
                        }

                        Object selectedTimesObj = document.get("selectedTimes");
                        StringBuilder timesText = new StringBuilder();

                        if (selectedTimesObj instanceof ArrayList) {
                            ArrayList<?> selectedTimes = (ArrayList<?>) selectedTimesObj;

                            for (Object time : selectedTimes) {
                                timesText.append("- ").append(time.toString()).append("\n");
                            }
                        } else {
                            timesText.append("No slots recorded\n");
                        }

                        String formattedPrice = totalPrice != null
                                ? String.format("%,d", totalPrice).replace(',', '.') + " VND"
                                : "0 VND";

                        String item =
                                "Branch: " + branchName + "\n" +
                                        "Date: " + date + "\n" +
                                        "Slots:\n" + timesText +
                                        "Total: " + formattedPrice + "\n" +
                                        "Payment: " + paymentMethod + "\n" +
                                        "Status: " + status + "\n\n" +
                                        "Tap to cancel booking";

                        historyList.add(item);
                        bookingIdList.add(documentId);
                        statusList.add(status);
                    }

                    if (historyList.isEmpty()) {
                        historyList.add("No booking history yet.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showCancelDialog(String bookingId) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> cancelBooking(bookingId))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelBooking(String bookingId) {
        db.collection("bookings")
                .document(bookingId)
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    loadBookingHistory();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cancel failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}