package com.example.badmintonbooking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private List<Court> courtList;

    public CourtAdapter(List<Court> courtList) {
        this.courtList = courtList;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courtList.get(position);

        holder.txtCourtName.setText(court.getName());
        holder.txtCourtLocation.setText("Khu vực: " + court.getLocation());
        holder.txtCourtPrice.setText("Giá: " + court.getPrice() + "đ / giờ");

        holder.itemView.setOnClickListener(v -> {

            com.google.firebase.firestore.FirebaseFirestore db =
                    com.google.firebase.firestore.FirebaseFirestore.getInstance();

            java.util.Map<String, Object> booking = new java.util.HashMap<>();

            booking.put("courtName", court.getName());
            booking.put("location", court.getLocation());
            booking.put("price", court.getPrice());
            booking.put("date", "2026-05-22");
            booking.put("time", "18:00");

            db.collection("bookings")
                    .add(booking)
                    .addOnSuccessListener(documentReference -> {
                        android.widget.Toast.makeText(
                                v.getContext(),
                                "Đặt sân thành công",
                                android.widget.Toast.LENGTH_SHORT
                        ).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return courtList.size();
    }

    public static class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView txtCourtName, txtCourtLocation, txtCourtPrice;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourtName = itemView.findViewById(R.id.txtCourtName);
            txtCourtLocation = itemView.findViewById(R.id.txtCourtLocation);
            txtCourtPrice = itemView.findViewById(R.id.txtCourtPrice);
        }
    }
}