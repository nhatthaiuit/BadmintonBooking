package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_detail);

        ImageView imageView = findViewById(R.id.imageViewDetail);
        TextView tvName = findViewById(R.id.textViewDetailName);
        TextView tvAddress = findViewById(R.id.textViewDetailAddress);
        TextView tvPrice = findViewById(R.id.textViewDetailPrice);
        Button btnDate = findViewById(R.id.buttonSelectDate);
        Button btnTime = findViewById(R.id.buttonSelectTime);
        Button btnConfirm = findViewById(R.id.buttonConfirmBooking);

        // Get Data from Intent
        Court court = (Court) getIntent().getSerializableExtra("COURT");
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
                
                // Mock API call to save booking. For now, just show Toast.
                Toast.makeText(CourtDetailActivity.this, "Booking Successful for " + selectedDate + " at " + selectedTime, Toast.LENGTH_LONG).show();
                finish(); // Close activity and return to Home
            }
        });
    }
}
