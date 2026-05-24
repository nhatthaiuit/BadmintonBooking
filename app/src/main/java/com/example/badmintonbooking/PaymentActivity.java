package com.example.badmintonbooking;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    RadioGroup paymentGroup;
    Button btnPlaceOrder;
    TextView tvTotal;

    String branch, date;
    long totalPrice;
    ArrayList<String> slots;

    boolean isSaving = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        paymentGroup = findViewById(R.id.paymentGroup);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        tvTotal = findViewById(R.id.tvTotal);

        branch = getIntent().getStringExtra("BRANCH");
        date = getIntent().getStringExtra("DATE");
        totalPrice = getIntent().getLongExtra("TOTAL_PRICE", 0);
        slots = getIntent().getStringArrayListExtra("SELECTED_TIMES");

        String formattedPrice = String.format("%,d", totalPrice).replace(',', '.') + " VND";
        tvTotal.setText("Total: " + formattedPrice);

        btnPlaceOrder.setOnClickListener(v -> {
            if (isSaving) return;

            int selectedId = paymentGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            if (branch == null || date == null || slots == null || slots.isEmpty() || totalPrice <= 0) {
                Toast.makeText(this, "Missing booking data", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Missing data: " + branch + " | " + date + " | " + totalPrice + " | " + slots);
                return;
            }

            RadioButton selectedPayment = findViewById(selectedId);
            String paymentMethod = selectedPayment.getText().toString();

            saveBookingToFirestore(paymentMethod);
        });
    }

    private void saveBookingToFirestore(String paymentMethod) {
        isSaving = true;
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Processing...");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String bookingId = db.collection("bookings").document().getId();

        String userId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        String bookingCode = "BK-" + System.currentTimeMillis();

        Map<String, Object> booking = new HashMap<>();
        booking.put("bookingCode", bookingCode);
        booking.put("bookingId", bookingId);
        booking.put("userId", userId);
        booking.put("branchName", branch);
        booking.put("date", date);
        booking.put("selectedTimes", slots);
        booking.put("totalPrice", totalPrice);
        booking.put("paymentMethod", paymentMethod);
        booking.put("status", "confirmed");
        booking.put("createdAt", FieldValue.serverTimestamp());

        db.collection("bookings")
                .document(bookingId)
                .set(booking)
                .addOnCompleteListener(task -> {
                    isSaving = false;
                    btnPlaceOrder.setEnabled(true);
                    btnPlaceOrder.setText("Place Order");

                    if (task.isSuccessful()) {
                        Log.d(TAG, "SUCCESS: Booking saved to Firestore");
                        Toast.makeText(this, "Booking successful! Code: " + bookingCode, Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
                        intent.putExtra("payment_method", paymentMethod);
                        intent.putExtra("total", totalPrice);
                        startActivity(intent);
                        finish();
                    } else {
                        String error = task.getException() != null
                                ? task.getException().toString()
                                : "Unknown error";

                        Log.e(TAG, "FAILED: " + error);
                        Toast.makeText(this, "Booking failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}