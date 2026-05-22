package com.example.badmintonbooking;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerCourts;
    CourtAdapter courtAdapter;
    ArrayList<Court> courtList;
    FirebaseFirestore db;
//ư
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerCourts = findViewById(R.id.recyclerCourts);
        recyclerCourts.setLayoutManager(new LinearLayoutManager(this));

        courtList = new ArrayList<>();
        courtAdapter = new CourtAdapter(courtList);
        recyclerCourts.setAdapter(courtAdapter);

        db = FirebaseFirestore.getInstance();

        loadCourts();
    }

    private void loadCourts() {
        db.collection("courts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courtList.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        Court court = document.toObject(Court.class);
                        courtList.add(court);
                    });

                    courtAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải danh sách sân", Toast.LENGTH_SHORT).show();
                });
    }
}