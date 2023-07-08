package com.example.booktrack.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.booktrack.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //view binding
    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Lütfen Bekleyin");
        progressDialog.setCanceledOnTouchOutside(false);


        //tıklanmada kayıt olma sayfasına iletme
        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //tıklandığında giriş yapmayı başlatır
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String email = "", password = "";
    private void validateData() {
        //veriyi çekme
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //veri doğrulaması
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //eposta boş bırakılmıs ya da yanlış formatta yazılmış
            Toast.makeText(this, "E-posta doğru formatta değil...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            //parola kısmı boş bırakılmış
            Toast.makeText(this, "Parola girilmeli...", Toast.LENGTH_SHORT).show();
        }
        else {
            //veri doğrulandı, giriş yapılacak
            loginUser();
        }

    }

    private void loginUser() {
        progressDialog.setMessage("Giriş Yapılıyor...");
        progressDialog.show();

        // Login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Giriş başarılı, kullanıcının veritabanı kontrolünü yap
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Giriş başarısız
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Giriş başarısız: " + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setTitle("Kullanıcı kontrol ediliyor...");

        // Kullanıcının oturum açtığından emin ol
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Veritabanında kullanıcıyı kontrol et
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            progressDialog.dismiss();
                            // Kullanıcı tipini alma
                            String userType = snapshot.child("userType").getValue(String.class);

                            if (userType != null) {
                                if (userType.equals("user")) {
                                    startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                                    finish();
                                } else if (userType.equals("admin")) {
                                    startActivity(new Intent(LoginActivity.this, DashboardAdminActivity.class));
                                    finish();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Kullanıcı tipi bulunamadı.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Veritabanı hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(LoginActivity.this, "Oturum açan kullanıcı bulunamadı.", Toast.LENGTH_SHORT).show();
        }
    }

}