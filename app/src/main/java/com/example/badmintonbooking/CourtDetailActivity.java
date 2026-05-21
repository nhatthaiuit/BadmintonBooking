package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CourtDetailActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "booking_channel";

    private RecyclerView recyclerTimeSlots;
    private RecyclerView recyclerCourtsStatus;
    private TimeSlotAdapter timeSlotAdapter;
    private CourtStatusAdapter courtStatusAdapter;
    
    private List<TimeSlot> timeSlotsList;
    private List<CourtStatus> courtsStatusList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_detail);

        // Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        createNotificationChannel();

        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        recyclerTimeSlots = findViewById(R.id.recyclerViewTimeSlots);
        recyclerCourtsStatus = findViewById(R.id.recyclerViewCourtsStatus);
        Button btnConfirm = findViewById(R.id.buttonConfirmBooking);
        TextView tvListLabel = findViewById(R.id.textViewListLabel);

        imageViewBack.setOnClickListener(v -> finish());

        // Khởi tạo danh sách giờ (05:00 - 22:00)
        timeSlotsList = new ArrayList<>();
        String[] hours = {"05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", 
                          "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"};
        for (String h : hours) {
            timeSlotsList.add(new TimeSlot(h, false));
        }
        
        // Setup Grid Giờ (5 cột giống trong ảnh)
        recyclerTimeSlots.setLayoutManager(new GridLayoutManager(this, 5));
        timeSlotAdapter = new TimeSlotAdapter(timeSlotsList, position -> {
            // Bỏ chọn tất cả
            for (TimeSlot slot : timeSlotsList) slot.setSelected(false);
            // Chọn cái được click
            timeSlotsList.get(position).setSelected(true);
            timeSlotAdapter.notifyDataSetChanged();
            
            // Random trạng thái sân để minh họa khi đổi giờ
            randomizeCourtStatus();
        });
        recyclerTimeSlots.setAdapter(timeSlotAdapter);

        // Khởi tạo danh sách sân (Sân 1 - 10)
        courtsStatusList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            courtsStatusList.add(new CourtStatus(i, CourtStatus.STATUS_EMPTY));
        }
        
        // Setup Grid Sân (5 cột giống ảnh)
        recyclerCourtsStatus.setLayoutManager(new GridLayoutManager(this, 5));
        courtStatusAdapter = new CourtStatusAdapter(courtsStatusList);
        recyclerCourtsStatus.setAdapter(courtStatusAdapter);

        // Nút Xác nhận đặt sân
        btnConfirm.setOnClickListener(v -> {
            boolean hasSelectedTime = false;
            String selectedT = "";
            for (TimeSlot t : timeSlotsList) {
                if (t.isSelected()) {
                    hasSelectedTime = true;
                    selectedT = t.getTime();
                    break;
                }
            }

            if (!hasSelectedTime) {
                Toast.makeText(this, "Vui lòng chọn giờ!", Toast.LENGTH_SHORT).show();
                return;
            }

            sendBookingNotification(selectedT);
            tvListLabel.setText("Danh sách đặt lịch (1)\nBạn vừa đặt sân vào lúc " + selectedT);
            Toast.makeText(this, "Xác nhận đặt sân thành công!", Toast.LENGTH_SHORT).show();
        });
    }

    private void randomizeCourtStatus() {
        Random rnd = new Random();
        for (CourtStatus c : courtsStatusList) {
            // Tỉ lệ trống cao hơn để dễ nhìn
            int status = rnd.nextInt(4); // 0, 1, 2, 3
            if (status == 3) status = 0;
            c.setStatus(status);
        }
        courtStatusAdapter.notifyDataSetChanged();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Booking Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void sendBookingNotification(String time) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Booking Confirmed!")
                .setContentText("Bạn đã đặt sân thành công vào lúc " + time)
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
