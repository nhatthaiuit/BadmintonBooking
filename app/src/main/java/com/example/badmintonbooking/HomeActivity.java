package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    private TableLayout tableLayoutMatrix;
    private TextView tvTotalInfo, tvSelectDate;
    private Button btnNext;
    private RadioGroup radioGroupBranches;
    
    private int selectedHours = 0;
    private long totalPrice = 0;
    private final long PRICE_PER_HOUR = 100000;
    
    private String[] timeSlots = {
            "05:00\n-\n06:00", "06:00\n-\n07:00", "07:00\n-\n08:00", "08:00\n-\n09:00", 
            "09:00\n-\n10:00", "10:00\n-\n11:00", "11:00\n-\n12:00", "12:00\n-\n13:00", 
            "13:00\n-\n14:00", "14:00\n-\n15:00", "15:00\n-\n16:00", "16:00\n-\n17:00",
            "17:00\n-\n18:00", "18:00\n-\n19:00", "19:00\n-\n20:00", "20:00\n-\n21:00", "21:00\n-\n22:00"
    };
    private int numCourts = 7;
    private ArrayList<String> selectedTimeInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tableLayoutMatrix = findViewById(R.id.tableLayoutMatrix);
        tvTotalInfo = findViewById(R.id.tvTotalInfo);
        tvSelectDate = findViewById(R.id.tvSelectDate);
        btnNext = findViewById(R.id.btnNext);
        radioGroupBranches = findViewById(R.id.radioGroupBranches);
        ImageView imgLogout = findViewById(R.id.imgLogout);
        ImageView imgHistory = findViewById(R.id.imgHistory);

        imgLogout.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);
            editor.apply();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        });

        imgHistory.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });

        // Date Picker logic
        tvSelectDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(HomeActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        tvSelectDate.setText(date);
                        generateTimetableMatrix(); // Reload matrix for new date
                    }, year, month, day);
            datePickerDialog.show();
        });

        radioGroupBranches.setOnCheckedChangeListener((group, checkedId) -> {
            generateTimetableMatrix();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedHours == 0) {
                Toast.makeText(this, "Please select at least 1 hour!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Go to Checkout Activity
            Intent intent = new Intent(HomeActivity.this, CheckoutActivity.class);
            
            // Get branch name
            String branchName = "Branch 1 (Go Vap Dist)";
            int checkedId = radioGroupBranches.getCheckedRadioButtonId();
            if (checkedId == R.id.rbBranch2) branchName = "Branch 2 (Binh Thanh Dist)";
            else if (checkedId == R.id.rbBranch3) branchName = "Branch 3 (Dist 1)";

            intent.putExtra("BRANCH", branchName);
            intent.putExtra("DATE", tvSelectDate.getText().toString());
            intent.putExtra("TOTAL_PRICE", totalPrice);
            intent.putStringArrayListExtra("SELECTED_TIMES", selectedTimeInfoList);
            startActivity(intent);
        });

        generateTimetableMatrix();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If returning from a successful checkout, we might want to clear selection.
        // For now, we leave it as is or clear it if needed.
    }

    private void generateTimetableMatrix() {
        tableLayoutMatrix.removeAllViews();
        selectedHours = 0;
        selectedTimeInfoList.clear();
        updateTotalUI();

        // 1. Header Row
        TableRow headerRow = new TableRow(this);
        
        TextView tvEmpty = new TextView(this);
        tvEmpty.setPadding(16, 16, 16, 16);
        headerRow.addView(tvEmpty);

        for (String slot : timeSlots) {
            TextView tvTime = new TextView(this);
            tvTime.setText(slot);
            tvTime.setPadding(24, 16, 24, 16);
            tvTime.setGravity(Gravity.CENTER);
            tvTime.setTextColor(Color.parseColor("#0288D1"));
            tvTime.setTextSize(12f);
            tvTime.setBackgroundResource(R.drawable.bg_time_header); // Rounded light blue
            
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
            params.setMargins(6, 6, 6, 6); // Add gap to prevent spreadsheet look
            tvTime.setLayoutParams(params);
            
            headerRow.addView(tvTime);
        }
        tableLayoutMatrix.addView(headerRow);

        // 2. Court Rows
        Random rnd = new Random();
        for (int i = 1; i <= numCourts; i++) {
            TableRow row = new TableRow(this);
            String courtName = "Court " + i;
            
            TextView tvCourt = new TextView(this);
            tvCourt.setText(courtName);
            tvCourt.setPadding(32, 24, 32, 24);
            tvCourt.setGravity(Gravity.CENTER);
            tvCourt.setTextColor(Color.WHITE);
            tvCourt.setBackgroundResource(R.drawable.bg_court_header); // Rounded accent blue
            
            TableRow.LayoutParams courtParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
            courtParams.setMargins(6, 6, 6, 6);
            tvCourt.setLayoutParams(courtParams);
            row.addView(tvCourt);

            for (int j = 0; j < timeSlots.length; j++) {
                String slotTime = timeSlots[j].replace("\n", ""); // "05:00-06:00"
                TextView tvCell = new TextView(this);
                tvCell.setPadding(16, 24, 16, 24);
                
                int status = rnd.nextInt(5); 
                
                if (status == 1) { 
                    tvCell.setBackgroundResource(R.drawable.bg_cell_booked);
                    tvCell.setTag("BOOKED");
                } else { 
                    tvCell.setBackgroundResource(R.drawable.bg_cell_empty);
                    tvCell.setTag("EMPTY");
                    setCellClickListener(tvCell, courtName, slotTime);
                }
                
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
                params.setMargins(6, 6, 6, 6); // 6dp gap for all cells
                tvCell.setLayoutParams(params);
                
                row.addView(tvCell);
            }
            
            tableLayoutMatrix.addView(row);
        }
    }
    
    private void setCellClickListener(TextView cell, String courtName, String slotTime) {
        cell.setOnClickListener(v -> {
            String currentTag = (String) v.getTag();
            String info = courtName + " (" + slotTime + ")";

            if (currentTag.equals("EMPTY")) {
                v.setBackgroundResource(R.drawable.bg_cell_selected);
                v.setTag("SELECTED");
                selectedHours++;
                selectedTimeInfoList.add(info);
            } else if (currentTag.equals("SELECTED")) {
                v.setBackgroundResource(R.drawable.bg_cell_empty); 
                v.setTag("EMPTY");
                selectedHours--;
                selectedTimeInfoList.remove(info);
            }
            updateTotalUI();
        });
    }

    private void updateTotalUI() {
        totalPrice = selectedHours * PRICE_PER_HOUR;
        String formattedPrice = String.format("%,d", totalPrice).replace(',', '.') + " VND";
        tvTotalInfo.setText("Selected: " + selectedHours + " hours\nTotal: " + formattedPrice);
    }
}
