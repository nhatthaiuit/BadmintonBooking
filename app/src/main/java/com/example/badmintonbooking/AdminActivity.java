package com.example.badmintonbooking;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerAdminBookings;
    private RadioGroup radioGroupStatus;

    private ArrayList<AdminBooking> bookingList;
    private AdminBookingAdapter adapter;

    private FirebaseFirestore db;
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();

        recyclerAdminBookings = findViewById(R.id.recyclerAdminBookings);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);

        bookingList = new ArrayList<>();

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

            loadBookings();
        });

        loadBookings();
    }

    private void loadBookings() {
        db.collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();

                        String branchName = document.getString("branchName");
                        String date = document.getString("date");
                        String status = document.getString("status");
                        Long totalPriceLong = document.getLong("totalPrice");

                        if (branchName == null) branchName = "Unknown Branch";
                        if (date == null) date = "Unknown Date";
                        if (status == null) status = "unknown";

                        if (!currentFilter.equals("all") && !currentFilter.equals(status)) {
                            continue;
                        }

                        long totalPrice = totalPriceLong != null ? totalPriceLong : 0;

                        ArrayList<String> selectedTimes = new ArrayList<>();

                        Object selectedTimesObj = document.get("selectedTimes");

                        if (selectedTimesObj instanceof ArrayList) {
                            ArrayList<?> rawList = (ArrayList<?>) selectedTimesObj;

                            for (Object item : rawList) {
                                selectedTimes.add(item.toString());
                            }
                        }

                        AdminBooking booking = new AdminBooking(
                                id,
                                branchName,
                                date,
                                selectedTimes,
                                totalPrice,
                                status
                        );

                        bookingList.add(booking);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void cancelBooking(String bookingId) {
        db.collection("bookings")
                .document(bookingId)
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    loadBookings();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cancel failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}