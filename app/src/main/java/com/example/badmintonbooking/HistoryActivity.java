package com.example.badmintonbooking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements BookingHistoryAdapter.OnCancelClickListener {

    private List<BookingHistory> historyList;
    private BookingHistoryAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration historyListener;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageView imgBack = findViewById(R.id.imgBackHistory);
        recyclerView = findViewById(R.id.recyclerViewHistory);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        imgBack.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        historyList = new ArrayList<>();
        adapter = new BookingHistoryAdapter(this, historyList, this);
        recyclerView.setAdapter(adapter);

        loadBookingHistory();
    }

    private void loadBookingHistory() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        historyListener = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load history: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    historyList.clear();

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
                        String formattedPrice = totalPrice != null
                                ? String.format("%,d", totalPrice).replace(',', '.') + " VND"
                                : "0 VND";

                        String paymentMethod = document.getString("paymentMethod");
                        if (paymentMethod == null || paymentMethod.isEmpty()) {
                            paymentMethod = "Not recorded";
                        }
                        String bookingCode = document.getString("bookingCode");
                        if (bookingCode == null) bookingCode = "No Code";

                        String status = document.getString("status");
                        if (status == null || status.isEmpty()) {
                            status = "confirmed";
                        }

                        Object selectedTimesObj = document.get("selectedTimes");
                        StringBuilder timesText = new StringBuilder();

                        if (selectedTimesObj instanceof ArrayList) {
                            ArrayList<?> selectedTimes = (ArrayList<?>) selectedTimesObj;
                            for (Object time : selectedTimes) {
                                timesText.append("• ").append(time.toString()).append("\n");
                            }
                        } else {
                            timesText.append("No slots recorded");
                        }

                        long timestamp = 0;
                        try {
                            if (document.contains("createdAt") && document.get("createdAt") != null) {
                                Object createdAtObj = document.get("createdAt");
                                if (createdAtObj instanceof com.google.firebase.Timestamp) {
                                    timestamp = ((com.google.firebase.Timestamp) createdAtObj).getSeconds();
                                }
                            } else if (bookingCode != null && bookingCode.startsWith("BK-")) {
                                timestamp = Long.parseLong(bookingCode.substring(3));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        historyList.add(new BookingHistory(
                                documentId,
                                bookingCode,
                                branchName,
                                date,
                                timesText.toString().trim(),
                                paymentMethod,
                                status,
                                formattedPrice,
                                timestamp
                        ));
                    }

                    // Sort by timestamp descending
                    Collections.sort(historyList, (b1, b2) -> Long.compare(b2.getTimestamp(), b1.getTimestamp()));

                    if (historyList.isEmpty()) {
                        Toast.makeText(this, "No booking history yet.", Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onCancelClick(BookingHistory booking) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel booking at " + booking.getBranchName() + " on " + booking.getDate() + "?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> cancelBooking(booking.getBookingId()))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelBooking(String bookingId) {
        db.collection("bookings")
                .document(bookingId)
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    // No need to reload, addSnapshotListener will handle it automatically
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cancel failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (historyListener != null) {
            historyListener.remove();
            historyListener = null;
        }
    }
}