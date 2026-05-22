package com.example.badmintonbooking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword;
    Button btnRegister;
    TextView txtGoLogin;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        btnRegister = findViewById(R.id.btnRegister);
        txtGoLogin = findViewById(R.id.txtGoLogin);

        btnRegister.setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        String userId = auth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("role", "user");

                        db.collection("users")
                                .document(userId)
                                .set(user);

                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        txtGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}