package com.example.badmintonbooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();

            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String role = documentSnapshot.getString("role");

                        if ("admin".equals(role)) {
                            startActivity(new Intent(MainActivity.this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        }

                        finish();
                    });
            return;
        }


        setContentView(R.layout.activity_main);

        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegister = findViewById(R.id.textViewRegister);
        TextInputEditText editTextPhone = findViewById(R.id.editTextPhone);
        TextInputEditText editTextPassword = findViewById(R.id.editTextPassword);

        buttonLogin.setOnClickListener(v -> {
            String phone = editTextPhone.getText() != null
                    ? editTextPhone.getText().toString().trim()
                    : "";

            String password = editTextPassword.getText() != null
                    ? editTextPassword.getText().toString().trim()
                    : "";

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter phone and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "No internet connection! Please check your network.", Toast.LENGTH_LONG).show();
                return;
            }

            String email = phone + "@badminton.local";

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                        String userId = auth.getCurrentUser().getUid();

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String role = documentSnapshot.getString("role");

                                    if ("admin".equals(role)) {
                                        startActivity(new Intent(MainActivity.this, AdminActivity.class));
                                    } else {
                                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    }

                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to check role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Incorrect phone number or password. Please try again.", Toast.LENGTH_LONG).show();
                    });
        });

        textViewRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }
}