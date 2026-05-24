package com.example.badmintonbooking;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookingHistoryAdapter extends RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<BookingHistory> historyList;
    private final OnCancelClickListener cancelClickListener;

    public interface OnCancelClickListener {
        void onCancelClick(BookingHistory booking);
    }

    public BookingHistoryAdapter(Context context, List<BookingHistory> historyList, OnCancelClickListener cancelClickListener) {
        this.context = context;
        this.historyList = historyList;
        this.cancelClickListener = cancelClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingHistory booking = historyList.get(position);

        holder.tvBranchName.setText(booking.getBranchName());

        holder.tvBookingCode.setText("Booking Code: " + booking.getBookingCode());

        holder.tvDate.setText(booking.getDate());
        holder.tvSlots.setText(booking.getSlotsText());
        holder.tvPayment.setText(booking.getPaymentMethod());
        holder.tvPrice.setText(booking.getTotalPriceFormatted());

        holder.tvStatus.setText(booking.getStatus().toUpperCase());

        if ("cancelled".equalsIgnoreCase(booking.getStatus())) {
            holder.tvStatus.setTextColor(Color.parseColor("#E53935"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C"));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed);
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        holder.btnCancel.setOnClickListener(v -> {
            if (cancelClickListener != null) {
                cancelClickListener.onCancelClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBranchName, tvBookingCode, tvStatus, tvDate, tvSlots, tvPayment, tvPrice;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvBookingCode = itemView.findViewById(R.id.tvBookingCode);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSlots = itemView.findViewById(R.id.tvSlots);
            tvPayment = itemView.findViewById(R.id.tvPayment);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}