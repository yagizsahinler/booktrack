package com.example.booktrack.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.booktrack.databinding.ActivityCategoryAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoryAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Lütfen Bekleyin");
        progressDialog.setCanceledOnTouchOutside(false);

        //tıklandığında geri döner
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //tıklandığında kategoriyi eklemeye başlar
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String category = "";
    private void validateData() {
        //veri doğrulamasından önce veriyi çekme
        category = binding.categoryEt.getText().toString().trim();
        //doğrulama
        if (TextUtils.isEmpty(category)){
            Toast.makeText(this, "Lütfen bir kategori girin...", Toast.LENGTH_SHORT).show();

        }
        else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        //ilerlemeyi gösterme
        progressDialog.setMessage("Kategori ekleniyor");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();

        //firebase veritabanına eklenecek kurulum bilgileri
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        //firebase veritabanına ekleme..... Database root > kategoriler > categoryid > category info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Kategoriler");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //kategori ekleme başarılı
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "Kategori başarıyla eklendi...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //kategori ekleme başarısız
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}