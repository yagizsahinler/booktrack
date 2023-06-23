package com.example.booktrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.booktrack.databinding.ActivityDashboardAdminBinding;
import com.example.booktrack.databinding.ActivityDashboardUserBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardUserActivity extends AppCompatActivity {

    //view binding
    private ActivityDashboardUserBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth  = FirebaseAuth.getInstance();
        checkUser();

        //butona basıldığında çıkış yapar
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser==null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else {
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);

        }
    }
}