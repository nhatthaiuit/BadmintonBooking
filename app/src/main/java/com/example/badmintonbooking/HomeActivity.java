package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    private TableLayout tableLayoutMatrix;
    private TextView tvTotalInfo;
    private Button btnNext;
    private RadioGroup radioGroupBranches;
    
    private int selectedHours = 0;
    private long totalPrice = 0;
    private final long PRICE_PER_HOUR = 100000; // 100k VND
    
    private String[] timeSlots = {
            "05:00\n-\n06:00", "06:00\n-\n07:00", "07:00\n-\n08:00", "08:00\n-\n09:00", 
            "09:00\n-\n10:00", "10:00\n-\n11:00", "11:00\n-\n12:00", "12:00\n-\n13:00", 
            "13:00\n-\n14:00", "14:00\n-\n15:00", "15:00\n-\n16:00", "16:00\n-\n17:00",
            "17:00\n-\n18:00", "18:00\n-\n19:00", "19:00\n-\n20:00", "20:00\n-\n21:00", "21:00\n-\n22:00"
    };
    private int numCourts = 7; // CN có 7 sân như hình mẫu
    private static final String CHANNEL_ID = "booking_channel_home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Permissions for Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        createNotificationChannel();

        tableLayoutMatrix = findViewById(R.id.tableLayoutMatrix);
        tvTotalInfo = findViewById(R.id.tvTotalInfo);
        btnNext = findViewById(R.id.btnNext);
        radioGroupBranches = findViewById(R.id.radioGroupBranches);
        ImageView imgLogout = findViewById(R.id.imgLogout);

        imgLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        });

        // Đổi chi nhánh -> load lại ma trận
        radioGroupBranches.setOnCheckedChangeListener((group, checkedId) -> {
            generateTimetableMatrix();
        });

        // Nút Tiếp theo / Đặt sân
        btnNext.setOnClickListener(v -> {
            if (selectedHours == 0) {
                Toast.makeText(this, "Vui lòng chạm vào các ô Trống để chọn giờ!", Toast.LENGTH_SHORT).show();
                return;
            }
            sendBookingNotification();
            Toast.makeText(this, "Xác nhận đặt sân thành công!", Toast.LENGTH_SHORT).show();
            
            // Reset lại bảng sau khi đặt xong
            generateTimetableMatrix();
        });

        // Generate ma trận lần đầu
        generateTimetableMatrix();
    }

    private void generateTimetableMatrix() {
        tableLayoutMatrix.removeAllViews();
        selectedHours = 0;
        updateTotalUI();

        // 1. Tạo hàng tiêu đề (Header Row - Khung giờ)
        TableRow headerRow = new TableRow(this);
        
        // Ô trống ở góc trên cùng bên trái
        TextView tvEmpty = new TextView(this);
        tvEmpty.setPadding(16, 16, 16, 16);
        headerRow.addView(tvEmpty);

        // Sinh các cột giờ
        for (String slot : timeSlots) {
            TextView tvTime = new TextView(this);
            tvTime.setText(slot);
            tvTime.setPadding(24, 16, 24, 16);
            tvTime.setGravity(Gravity.CENTER);
            tvTime.setTextColor(Color.parseColor("#0288D1"));
            tvTime.setTextSize(12f);
            tvTime.setBackgroundColor(Color.parseColor("#E1F5FE")); // Nền xanh nhạt
            
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
            params.setMargins(2, 2, 2, 2); // Tạo viền lưới 2dp
            tvTime.setLayoutParams(params);
            
            headerRow.addView(tvTime);
        }
        tableLayoutMatrix.addView(headerRow);

        // 2. Tạo các hàng Sân (Row) và các ô trạng thái (Cells)
        Random rnd = new Random();
        for (int i = 1; i <= numCourts; i++) {
            TableRow row = new TableRow(this);
            
            // Cột đầu tiên: Tên sân
            TextView tvCourt = new TextView(this);
            tvCourt.setText("Sân " + i);
            tvCourt.setPadding(32, 24, 32, 24);
            tvCourt.setGravity(Gravity.CENTER);
            tvCourt.setTextColor(Color.WHITE);
            tvCourt.setBackgroundColor(Color.parseColor("#03A9F4")); // Accent Blue
            
            TableRow.LayoutParams courtParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
            courtParams.setMargins(2, 2, 2, 2);
            tvCourt.setLayoutParams(courtParams);
            row.addView(tvCourt);

            // Các ô trạng thái tương ứng với từng khung giờ
            for (int j = 0; j < timeSlots.length; j++) {
                TextView tvCell = new TextView(this);
                tvCell.setPadding(16, 24, 16, 24);
                
                // Random trạng thái: 0=Trống, 1=Đã đặt, 2=Đang chờ
                int status = rnd.nextInt(5); // Tỉ lệ trống cao hơn (3/5)
                
                if (status == 1) { // Đã đặt (Đỏ)
                    tvCell.setBackgroundColor(ContextCompat.getColor(this, R.color.status_booked));
                    tvCell.setTag("BOOKED");
                } else if (status == 2) { // Đang chờ (Vàng) - giả lập người khác đang chờ
                    // Trong ảnh Đang chờ là vàng, Đang chọn cũng là Vàng. 
                    // Ta giả lập random là Đã đặt hoặc Trống cho dễ. (Gộp status 2 thành Trống)
                    tvCell.setBackgroundColor(ContextCompat.getColor(this, R.color.status_empty));
                    tvCell.setTag("EMPTY");
                    setCellClickListener(tvCell);
                } else { // Trống (Trắng)
                    tvCell.setBackgroundColor(ContextCompat.getColor(this, R.color.status_empty));
                    tvCell.setTag("EMPTY");
                    setCellClickListener(tvCell);
                }
                
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                params.setMargins(2, 2, 2, 2);
                tvCell.setLayoutParams(params);
                
                row.addView(tvCell);
            }
            
            tableLayoutMatrix.addView(row);
        }
    }
    
    // Logic Click để chọn ô Trống thành Đang Chọn (Vàng)
    private void setCellClickListener(TextView cell) {
        cell.setOnClickListener(v -> {
            String currentTag = (String) v.getTag();
            if (currentTag.equals("EMPTY")) {
                v.setBackgroundColor(ContextCompat.getColor(this, R.color.status_pending)); // Chuyển sang Vàng
                v.setTag("SELECTED");
                selectedHours++;
            } else if (currentTag.equals("SELECTED")) {
                v.setBackgroundColor(ContextCompat.getColor(this, R.color.status_empty)); // Trở về Trắng
                v.setTag("EMPTY");
                selectedHours--;
            }
            updateTotalUI();
        });
    }

    private void updateTotalUI() {
        totalPrice = selectedHours * PRICE_PER_HOUR;
        String formattedPrice = String.format("%,d", totalPrice).replace(',', '.') + " đ";
        tvTotalInfo.setText("Đang chọn: " + selectedHours + " giờ\nTổng: " + formattedPrice);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Booking Home Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void sendBookingNotification() {
        String formattedPrice = String.format("%,d", totalPrice).replace(',', '.') + " đ";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Đặt sân thành công!")
                .setContentText("Bạn đã đặt " + selectedHours + " tiếng. Tổng tiền: " + formattedPrice)
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
