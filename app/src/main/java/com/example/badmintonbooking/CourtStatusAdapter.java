package com.example.badmintonbooking;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class CourtStatusAdapter extends RecyclerView.Adapter<CourtStatusAdapter.ViewHolder> {

    private List<CourtStatus> courts;

    public CourtStatusAdapter(List<CourtStatus> courts) {
        this.courts = courts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_court_status, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourtStatus court = courts.get(position);
        holder.tvNumber.setText(String.valueOf(court.getNumber()));

        int colorResId;
        switch (court.getStatus()) {
            case CourtStatus.STATUS_PENDING:
                colorResId = R.color.status_pending;
                break;
            case CourtStatus.STATUS_BOOKED:
                colorResId = R.color.status_booked;
                break;
            case CourtStatus.STATUS_EMPTY:
            default:
                colorResId = R.color.status_empty;
                break;
        }
        
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), colorResId));
        
        if (court.getStatus() == CourtStatus.STATUS_EMPTY) {
            holder.tvNumber.setTextColor(Color.parseColor("#333333"));
        } else {
            holder.tvNumber.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return courts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumber;
        MaterialCardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.textViewCourtNumber);
            cardView = itemView.findViewById(R.id.cardViewCourtStatus);
        }
    }
}
