package com.example.badmintonbooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerAdminBookings;
    private RadioGroup radioGroupStatus;
    private Button btnAdminLogout;
    private EditText etSearchCode;

    private List<AdminBooking> fullBookingList;
    private ArrayList<AdminBooking> bookingList;
    private AdminBookingAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration adminListener;

    private String currentFilter = "all";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerAdminBookings = findViewById(R.id.recyclerAdminBookings);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);
        etSearchCode = findViewById(R.id.etSearchCode);

        btnAdminLogout.setOnClickListener(v -> {
            auth.signOut();

            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        fullBookingList = new ArrayList<>();
        bookingList = new ArrayList<>();

        etSearchCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        adapter = new AdminBookingAdapter(bookingList, booking -> {
            cancelBooking(booking.id);
        });

        recyclerAdminBookings.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdminBookings.setAdapter(adapter);

        radioGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbConfirmed) {
                currentFilter = "confirmed";
            } else if (checkedId == R.id.rbCancelled) {
                currentFilter = "cancelled";
            } else {
                currentFilter = "all";
            }

            applyFilters();
        });

        loadBookings();
    }

    private void loadBookings() {
        adminListener = db.collection("bookings")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to load bookings: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots == null) return;

                    fullBookingList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();

                        String branchName = document.getString("branchName");
                        String date = document.getString("date");
                        String status = document.getString("status");

                        String bookingCode = document.getString("bookingCode");
                        if (bookingCode == null) bookingCode = "No Code";

                        Long totalPriceLong = document.getLong("totalPrice");

                        if (branchName == null) branchName = "Unknown Branch";
                        if (date == null) date = "Unknown Date";
                        if (status == null) status = "unknown";

                        long totalPrice = totalPriceLong != null ? totalPriceLong : 0;

                        ArrayList<String> selectedTimes = new ArrayList<>();
                        Object selectedTimesObj = document.get("selectedTimes");

                        if (selectedTimesObj instanceof ArrayList) {
                            ArrayList<?> rawList = (ArrayList<?>) selectedTimesObj;
                            for (Object item : rawList) {
                                selectedTimes.add(item.toString());
                            }
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

                        AdminBooking booking = new AdminBooking(
                                id,
                                bookingCode,
                                branchName,
                                date,
                                selectedTimes,
                                totalPrice,
                                status,
                                timestamp
                        );

                        fullBookingList.add(booking);
                    }

                    applyFilters();
                });
    }

    private void applyFilters() {
        bookingList.clear();

        for (AdminBooking booking : fullBookingList) {
            // 1. Check status filter
            boolean matchesStatus = currentFilter.equals("all") || currentFilter.equals(booking.status);

            // 2. Check search query
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    booking.bookingCode.toLowerCase().contains(currentSearchQuery);

            if (matchesStatus && matchesSearch) {
                bookingList.add(booking);
            }
        }

        // Sort locally by timestamp descending
        Collections.sort(bookingList, (b1, b2) -> Long.compare(b2.timestamp, b1.timestamp));

        adapter.notifyDataSetChanged();
    }

    private void cancelBooking(String bookingId) {
        db.collection("bookings")
                .document(bookingId)
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    // No need to reload, addSnapshotListener handles it
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cancel failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adminListener != null) {
            adminListener.remove();
            adminListener = null;
        }
    }
}