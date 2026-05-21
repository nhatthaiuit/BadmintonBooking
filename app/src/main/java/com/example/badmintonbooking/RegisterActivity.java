package com.example.badmintonbooking;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageView imgBack = findViewById(R.id.imgBack);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        TextInputEditText editTextName = findViewById(R.id.editTextName);
        TextInputEditText editTextPhone = findViewById(R.id.editTextPhoneReg);
        TextInputEditText editTextPass = findViewById(R.id.editTextPassReg);

        imgBack.setOnClickListener(v -> finish());

        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText().toString();
            String phone = editTextPhone.getText().toString();
            String pass = editTextPass.getText().toString();

            if (name.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
            finish(); // return to login
        });
    }
}
