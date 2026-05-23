package com.example.badmintonbooking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class OrderSuccessActivity extends Activity {

    TextView tvPayment, tvTotal;
    Button btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        tvPayment = findViewById(R.id.tvPayment);
        tvTotal = findViewById(R.id.tvTotalSuccess);
        btnBackHome = findViewById(R.id.btnBackHome);

        String paymentMethod = getIntent().getStringExtra("payment_method");
        double total = getIntent().getDoubleExtra("total", 0);

        tvPayment.setText("Payment Method: " + paymentMethod);
        tvTotal.setText("Total Paid: $" + total);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}