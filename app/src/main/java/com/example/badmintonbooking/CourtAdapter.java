package com.example.badmintonbooking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private List<Court> courtList;

    public CourtAdapter(List<Court> courtList) {
        this.courtList = courtList;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        // Bind data to the views
        Court court = courtList.get(position);
        holder.textViewName.setText(court.getName());
        holder.textViewAddress.setText(court.getAddress());
        holder.textViewPrice.setText("$" + court.getPricePerHour() + " / hour");

        // Load image from URL using Glide
        Glide.with(holder.itemView.getContext())
                .load(court.getImageUrl())
                .placeholder(R.mipmap.ic_launcher) // Show default icon while loading
                .into(holder.imageViewCourt);
                
        // Handle click event on the whole item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to CourtDetailActivity and pass the Court object
                android.content.Intent intent = new android.content.Intent(v.getContext(), CourtDetailActivity.class);
                intent.putExtra("COURT", court);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return courtList != null ? courtList.size() : 0;
    }

    // ViewHolder class to hold view references
    public static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCourt;
        TextView textViewName, textViewAddress, textViewPrice;

        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCourt = itemView.findViewById(R.id.imageViewCourt);
            textViewName = itemView.findViewById(R.id.textViewCourtName);
            textViewAddress = itemView.findViewById(R.id.textViewCourtAddress);
            textViewPrice = itemView.findViewById(R.id.textViewCourtPrice);
        }
    }
}
