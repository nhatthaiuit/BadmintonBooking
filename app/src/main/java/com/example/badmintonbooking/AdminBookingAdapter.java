package com.example.badmintonbooking;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    public interface OnCancelClick {
        void onCancel(AdminBooking booking);
    }

    private ArrayList<AdminBooking> bookings;
    private OnCancelClick onCancelClick;

    public AdminBookingAdapter(ArrayList<AdminBooking> bookings, OnCancelClick onCancelClick) {
        this.bookings = bookings;
        this.onCancelClick = onCancelClick;
    }

    @NonNull
    @Override
    public AdminBookingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookingAdapter.ViewHolder holder, int position) {
        AdminBooking booking = bookings.get(position);

        holder.tvBranch.setText("Branch: " + booking.branchName);
        holder.tvDate.setText("Date: " + booking.date);

        StringBuilder slotsText = new StringBuilder();
        if (booking.selectedTimes != null) {
            for (String slot : booking.selectedTimes) {
                slotsText.append("- ").append(slot).append("\n");
            }
        }

        holder.tvSlots.setText("Slots:\n" + slotsText.toString().trim());

        String formattedPrice = String.format("%,d", booking.totalPrice).replace(',', '.') + " VND";
        holder.tvTotal.setText("Total: " + formattedPrice);
        holder.tvStatus.setText("Status: " + booking.status);

        if ("cancelled".equals(booking.status)) {
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        holder.btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Cancel Booking")
                    .setMessage("Do you want to cancel this booking?")
                    .setPositiveButton("Yes", (dialog, which) -> onCancelClick.onCancel(booking))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBranch, tvDate, tvSlots, tvTotal, tvStatus;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranch = itemView.findViewById(R.id.tvAdminBranch);
            tvDate = itemView.findViewById(R.id.tvAdminDate);
            tvSlots = itemView.findViewById(R.id.tvAdminSlots);
            tvTotal = itemView.findViewById(R.id.tvAdminTotal);
            tvStatus = itemView.findViewById(R.id.tvAdminStatus);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}