package com.example.badmintonbooking;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {

    private TableLayout tableLayoutMatrix;
    private TextView tvTotalInfo, tvSelectDate, tvAvailableInfo;
    private Button btnNext;
    private RadioGroup radioGroupBranches;
    private ProgressBar progressBarLoading;

    private int selectedHours = 0;
    private long totalPrice = 0;
    private final long PRICE_PER_HOUR = 100000;

    private FirebaseFirestore db;
    private ListenerRegistration bookingListener;

    private final String[] timeSlots = {
            "05:00\n-\n06:00", "06:00\n-\n07:00", "07:00\n-\n08:00", "08:00\n-\n09:00",
            "09:00\n-\n10:00", "10:00\n-\n11:00", "11:00\n-\n12:00", "12:00\n-\n13:00",
            "13:00\n-\n14:00", "14:00\n-\n15:00", "15:00\n-\n16:00", "16:00\n-\n17:00",
            "17:00\n-\n18:00", "18:00\n-\n19:00", "19:00\n-\n20:00", "20:00\n-\n21:00",
            "21:00\n-\n22:00"
    };

    private final int numCourts = 7;

    private final ArrayList<String> selectedTimeInfoList = new ArrayList<>();
    private final ArrayList<TextView> allCells = new ArrayList<>();
    private final HashMap<TextView, String> cellSlotMap = new HashMap<>();

    private int refreshVersion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();

        tableLayoutMatrix = findViewById(R.id.tableLayoutMatrix);
        tvTotalInfo = findViewById(R.id.tvTotalInfo);
        tvSelectDate = findViewById(R.id.tvSelectDate);
        tvAvailableInfo = findViewById(R.id.availableInfoBar);
        btnNext = findViewById(R.id.btnNext);
        radioGroupBranches = findViewById(R.id.radioGroupBranches);
        progressBarLoading = findViewById(R.id.progressBarLoading);

        findViewById(R.id.rbBranch1).setOnClickListener(v -> showBranchAddressDialog());
        findViewById(R.id.rbBranch2).setOnClickListener(v -> showBranchAddressDialog());
        findViewById(R.id.rbBranch3).setOnClickListener(v -> showBranchAddressDialog());

        ImageView imgLogout = findViewById(R.id.imgLogout);
        Button imgHistory = findViewById(R.id.imgHistory);

        imgLogout.setOnClickListener(v -> {
            // Clean logout
            FirebaseAuth.getInstance().signOut();
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", android.content.Context.MODE_PRIVATE);
            sharedPreferences.edit().clear().apply();
            
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        imgHistory.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });

        tvSelectDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    HomeActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                        tvSelectDate.setText(date);
                        refreshSlotsForSelectedDate();
                    },
                    year,
                    month,
                    day
            );

            Calendar minDate = Calendar.getInstance();
            minDate.set(Calendar.HOUR_OF_DAY, 0);
            minDate.set(Calendar.MINUTE, 0);
            minDate.set(Calendar.SECOND, 0);
            minDate.set(Calendar.MILLISECOND, 0);

            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            datePickerDialog.show();
        });

        radioGroupBranches.setOnCheckedChangeListener((group, checkedId) -> {
            refreshSlotsForSelectedDate();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedHours == 0) {
                Toast.makeText(this, "Please select at least 1 hour!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(HomeActivity.this, CheckoutActivity.class);

            intent.putExtra("BRANCH", getSelectedBranchName());
            intent.putExtra("DATE", tvSelectDate.getText().toString());
            intent.putExtra("TOTAL_PRICE", totalPrice);
            intent.putStringArrayListExtra("SELECTED_TIMES", selectedTimeInfoList);

            startActivity(intent);
        });

        Calendar today = Calendar.getInstance();
        String todayDate = today.get(Calendar.DAY_OF_MONTH) + "/" +
                (today.get(Calendar.MONTH) + 1) + "/" +
                today.get(Calendar.YEAR);

        tvSelectDate.setText(todayDate);

        refreshSlotsForSelectedDate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSlotsForSelectedDate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bookingListener != null) {
            bookingListener.remove();
            bookingListener = null;
        }
    }

    private String getSelectedBranchName() {
        int checkedId = radioGroupBranches.getCheckedRadioButtonId();

        if (checkedId == R.id.rbBranch2) {
            return "Branch 2 (Binh Thanh Dist)";
        } else if (checkedId == R.id.rbBranch3) {
            return "Branch 3 (Dist 1)";
        }

        return "Branch 1 (Go Vap Dist)";
    }

    private String normalizeSlot(String slotTime) {
        return slotTime.replace("\n", "");
    }

    private void refreshSlotsForSelectedDate() {
        refreshVersion++;

        selectedHours = 0;
        selectedTimeInfoList.clear();
        updateTotalUI();

        generateEmptyTimetableMatrix();
        listenBookedSlotsForCurrentDate(refreshVersion);
    }

    private void generateEmptyTimetableMatrix() {
        tableLayoutMatrix.removeAllViews();
        allCells.clear();
        cellSlotMap.clear();

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
            tvTime.setBackgroundResource(R.drawable.bg_time_header);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT
            );

            params.setMargins(6, 6, 6, 6);
            tvTime.setLayoutParams(params);

            headerRow.addView(tvTime);
        }

        tableLayoutMatrix.addView(headerRow);

        for (int i = 1; i <= numCourts; i++) {
            TableRow row = new TableRow(this);

            String courtName = "Court " + i;

            TextView tvCourt = new TextView(this);
            tvCourt.setText(courtName);
            tvCourt.setPadding(32, 24, 32, 24);
            tvCourt.setGravity(Gravity.CENTER);
            tvCourt.setTextColor(Color.WHITE);
            tvCourt.setBackgroundResource(R.drawable.bg_court_header);

            TableRow.LayoutParams courtParams = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT
            );

            courtParams.setMargins(6, 6, 6, 6);
            tvCourt.setLayoutParams(courtParams);

            row.addView(tvCourt);

            for (String slotTime : timeSlots) {
                TextView tvCell = new TextView(this);

                tvCell.setPadding(16, 24, 16, 24);
                tvCell.setBackgroundResource(R.drawable.bg_cell_empty);
                tvCell.setTag("EMPTY");

                String slotClean = normalizeSlot(slotTime);
                String slotInfo = courtName + " (" + slotClean + ")";

                cellSlotMap.put(tvCell, slotInfo);
                allCells.add(tvCell);

                setCellClickListener(tvCell, slotInfo);

                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT
                );

                params.setMargins(6, 6, 6, 6);
                tvCell.setLayoutParams(params);

                row.addView(tvCell);
            }

            tableLayoutMatrix.addView(row);
        }
    }

    private void listenBookedSlotsForCurrentDate(int version) {
        String selectedDate = tvSelectDate.getText().toString();
        String selectedBranch = getSelectedBranchName();

        if (bookingListener != null) {
            bookingListener.remove();
            bookingListener = null;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet connection! Please check your network.", Toast.LENGTH_LONG).show();
            return;
        }

        progressBarLoading.setVisibility(android.view.View.VISIBLE);

        bookingListener = db.collection("bookings")
                .whereEqualTo("branchName", selectedBranch)
                .whereEqualTo("date", selectedDate)
                .whereEqualTo("status", "confirmed")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    progressBarLoading.setVisibility(android.view.View.GONE);
                    
                    if (error != null) {
                        Toast.makeText(this, "Failed to refresh slots: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (queryDocumentSnapshots == null) {
                        return;
                    }

                    if (version != refreshVersion) {
                        return;
                    }

                    ArrayList<String> bookedSlotList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Object timesObj = document.get("selectedTimes");

                        if (timesObj instanceof ArrayList) {
                            ArrayList<?> bookedSlots = (ArrayList<?>) timesObj;

                            for (Object slot : bookedSlots) {
                                if (slot != null) {
                                    bookedSlotList.add(slot.toString());
                                }
                            }
                        }
                    }

                    selectedHours = 0;
                    selectedTimeInfoList.clear();

                    int totalSlots = numCourts * timeSlots.length;
                    int unavailableCount = 0;

                    for (TextView cell : allCells) {
                        String slotInfo = cellSlotMap.get(cell);

                        if (slotInfo == null) {
                            continue;
                        }

                        if (bookedSlotList.contains(slotInfo)) {
                            unavailableCount++;

                            cell.setBackgroundResource(R.drawable.bg_cell_booked);
                            cell.setTag("BOOKED");
                            cell.setOnClickListener(null);

                        } else if (isPastSlotToday(slotInfo)) {
                            unavailableCount++;

                            cell.setBackgroundColor(Color.LTGRAY);
                            cell.setTag("PAST");
                            cell.setOnClickListener(null);

                        } else {
                            cell.setBackgroundResource(R.drawable.bg_cell_empty);
                            cell.setTag("EMPTY");
                            setCellClickListener(cell, slotInfo);
                        }
                    }

                    int availableCount = totalSlots - unavailableCount;

                    tvAvailableInfo.setText("Available slots: " + availableCount + "/" + totalSlots);

                    updateTotalUI();
                });
    }

    private void setCellClickListener(TextView cell, String slotInfo) {
        cell.setOnClickListener(v -> {
            String currentTag = (String) v.getTag();

            if ("EMPTY".equals(currentTag)) {
                v.setBackgroundResource(R.drawable.bg_cell_selected);
                v.setTag("SELECTED");

                selectedHours++;
                selectedTimeInfoList.add(slotInfo);

            } else if ("SELECTED".equals(currentTag)) {
                v.setBackgroundResource(R.drawable.bg_cell_empty);
                v.setTag("EMPTY");

                selectedHours--;
                selectedTimeInfoList.remove(slotInfo);
            }

            updateTotalUI();
        });
    }

    private boolean isPastSlotToday(String slotInfo) {
        String selectedDate = tvSelectDate.getText().toString();

        Calendar today = Calendar.getInstance();
        String todayDate = today.get(Calendar.DAY_OF_MONTH) + "/" +
                (today.get(Calendar.MONTH) + 1) + "/" +
                today.get(Calendar.YEAR);

        if (!selectedDate.equals(todayDate)) {
            return false;
        }

        try {
            String timePart = slotInfo.substring(slotInfo.indexOf("(") + 1, slotInfo.indexOf("-"));
            String[] parts = timePart.split(":");

            int slotHour = Integer.parseInt(parts[0]);
            int currentHour = today.get(Calendar.HOUR_OF_DAY);

            return slotHour <= currentHour;

        } catch (Exception e) {
            return false;
        }
    }

    private void updateTotalUI() {
        totalPrice = selectedHours * PRICE_PER_HOUR;

        String formattedPrice =
                String.format("%,d", totalPrice).replace(',', '.') + " VND";

        tvTotalInfo.setText(
                "Selected: " + selectedHours + " hours\nTotal: " + formattedPrice
        );
    }



    private String getSelectedBranchAddress() {
        int checkedId = radioGroupBranches.getCheckedRadioButtonId();

        if (checkedId == R.id.rbBranch2) {
            return "123 Dien Bien Phu, Binh Thanh District, Ho Chi Minh City";
        } else if (checkedId == R.id.rbBranch3) {
            return "45 Nguyen Hue, District 1, Ho Chi Minh City";
        }

        return "789 Quang Trung, Go Vap District, Ho Chi Minh City";
    }

    private void showBranchAddressDialog() {
        String branchName = getSelectedBranchName();
        String address = getSelectedBranchAddress();

        new AlertDialog.Builder(this)
                .setTitle(branchName)
                .setMessage("Address:\n\n" + address)
                .setPositiveButton("Open Maps", (dialog, which) -> {
                    Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(mapIntent);
                })
                .setNegativeButton("Close", null)
                .show();
    }
}