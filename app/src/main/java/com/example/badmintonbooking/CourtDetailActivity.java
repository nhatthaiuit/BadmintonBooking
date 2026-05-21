package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Calendar;

public class CourtDetailActivity extends AppCompatActivity {

    private String selectedDate = "";
    private String selectedTime = "";
    private Court court;
    private static final String CHANNEL_ID = "booking_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_detail);

        // Ask for Notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        createNotificationChannel();

        ImageView imageView = findViewById(R.id.imageViewDetail);
        TextView tvName = findViewById(R.id.textViewDetailName);
        TextView tvAddress = findViewById(R.id.textViewDetailAddress);
        TextView tvPrice = findViewById(R.id.textViewDetailPrice);
        Button btnDate = findViewById(R.id.buttonSelectDate);
        Button btnTime = findViewById(R.id.buttonSelectTime);
        Button btnConfirm = findViewById(R.id.buttonConfirmBooking);

        // Get Data from Intent
        court = (Court) getIntent().getSerializableExtra("COURT");
        if (court != null) {
            tvName.setText(court.getName());
            tvAddress.setText(court.getAddress());
            tvPrice.setText("$" + court.getPricePerHour() + " / hour");
            Glide.with(this).load(court.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(imageView);
        }

        // Setup Date Picker
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(CourtDetailActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                btnDate.setText("Date: " + selectedDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Setup Time Picker
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(CourtDetailActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                selectedTime = hourOfDay + ":" + String.format("%02d", minute);
                                btnTime.setText("Time: " + selectedTime);
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        // Setup Confirm Booking
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                    Toast.makeText(CourtDetailActivity.this, "Please select Date and Time", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                sendBookingNotification();
                
                Toast.makeText(CourtDetailActivity.this, "Booking Successful!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and return to Home
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Booking Notifications";
            String description = "Channel for badminton booking confirmations";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void sendBookingNotification() {
        String courtName = court != null ? court.getName() : "Court";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round) // Using default icon for now
                .setContentTitle("Booking Confirmed!")
                .setContentText("You booked " + courtName + " on " + selectedDate + " at " + selectedTime)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace(); // Handle missing permission or rejected permission
        }
    }
}
