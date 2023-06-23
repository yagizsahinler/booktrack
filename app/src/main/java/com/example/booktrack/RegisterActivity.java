package com.example.booktrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.booktrack.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //view binding
    private ActivityRegisterBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Lütfen Bekleyin");
        progressDialog.setCanceledOnTouchOutside(false);

        //tıklanmada geri dönme
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //tıklanmada kayıt olma sayfasına iletme
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String name="", email = "", password = "";

    private void validateData() {
        //veriyi çekme
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPassword = binding.cPasswordEt.getText().toString().trim();

        //veri doğrulaması
        if (TextUtils.isEmpty(name)){
            //kullanıcı adı boş bırakılmış
            Toast.makeText(this, "Kullanıcı adını girmelisiniz...", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //eposta boş bırakılmıs ya da yanlış formatta yazılmış
            Toast.makeText(this, "E-posta doğru formatta değil...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            //parola kısmı boş bırakılmış
            Toast.makeText(this, "Parola girilmeli...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cPassword)){
            //parola onaylama kısmı boş bırakılmış
            Toast.makeText(this, "Parola onaylanmalı...", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(cPassword)){
            //girilen iki parola aynı değil
            Toast.makeText(this, "İki parola uyuşmuyor...", Toast.LENGTH_SHORT).show();
        }
        else {
            //bilgiler kullanılabilir, hesap oluşturulur
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //ilerlemeyi gösterme
        progressDialog.setMessage("Hesap Oluşturuluyor...");
        progressDialog.show();

        //kullanıcıyı firebase auth ile oluşturma
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //hesap oluşturma başarılı, yeni veriler veritabanına gidecek
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //hesap oluşturma başarısız
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Kullanıcı verileri kaydediliyor...");

        long timestamp = System.currentTimeMillis();

        String uid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", "");
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //data başarıyla yazıldı
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Hesap oluşturuldu...", Toast.LENGTH_SHORT).show();
                        //kullanıcı verilerinin gösterge panelinde gösterilmesi için
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //data yazılırken hata oldu
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}