package com.example.badmintonbooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "booking_channel_home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        ImageView imgBack = findViewById(R.id.imgBack);
        TextView tvCheckoutBranch = findViewById(R.id.tvCheckoutBranch);
        TextView tvCheckoutDate = findViewById(R.id.tvCheckoutDate);
        TextView tvCheckoutSlots = findViewById(R.id.tvCheckoutSlots);
        TextView tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        Button btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        imgBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        String branch = intent.getStringExtra("BRANCH");
        String date = intent.getStringExtra("DATE");
        long totalPrice = intent.getLongExtra("TOTAL_PRICE", 0);
        ArrayList<String> slots = intent.getStringArrayListExtra("SELECTED_TIMES");

        tvCheckoutBranch.setText("Branch: " + branch);
        tvCheckoutDate.setText("Date: " + date);
        
        StringBuilder slotsStr = new StringBuilder();
        if (slots != null) {
            for (String s : slots) {
                slotsStr.append("- ").append(s).append("\n");
            }
        }
        tvCheckoutSlots.setText(slotsStr.toString().trim());
        
        String formattedPrice = String.format("%,d", totalPrice).replace(',', '.') + " VND";
        tvCheckoutTotal.setText(formattedPrice);

        btnConfirmPayment.setOnClickListener(v -> {
            sendBookingNotification(slots != null ? slots.size() : 0, formattedPrice);
            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void sendBookingNotification(int hours, String price) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Booking Confirmed!")
                .setContentText("You successfully booked " + hours + " slots. Total: " + price)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
