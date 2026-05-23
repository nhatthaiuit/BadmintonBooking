package com.example.badmintonbooking;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView imgBack = findViewById(R.id.imgBack);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        TextInputEditText editTextName = findViewById(R.id.editTextName);
        TextInputEditText editTextPhone = findViewById(R.id.editTextPhoneReg);
        TextInputEditText editTextPass = findViewById(R.id.editTextPassReg);

        imgBack.setOnClickListener(v -> finish());

        buttonRegister.setOnClickListener(v -> {
            String name = editTextName.getText() != null
                    ? editTextName.getText().toString().trim()
                    : "";

            String phone = editTextPhone.getText() != null
                    ? editTextPhone.getText().toString().trim()
                    : "";

            String password = editTextPass.getText() != null
                    ? editTextPass.getText().toString().trim()
                    : "";

            if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection! Please check your network.", Toast.LENGTH_LONG).show();
                return;
            }

            String email = phone + "@badminton.local";

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String userId = auth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("phone", phone);
                        user.put("email", email);
                        user.put("role", "user");

                        db.collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                                    auth.signOut();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Save user failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        String errorMsg = e.getMessage() != null ? e.getMessage() : "";
                        if (errorMsg.contains("The email address is already in use")) {
                            Toast.makeText(this, "This phone number is already registered.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Registration failed. Please check your information.", Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}