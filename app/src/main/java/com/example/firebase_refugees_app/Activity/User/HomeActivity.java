package com.example.firebase_refugees_app.Activity.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase_refugees_app.Activity.DonationActivity;
import com.example.firebase_refugees_app.Activity.MapActivity;
import com.example.firebase_refugees_app.Activity.Refugees.RefugeesActivity;
import com.example.firebase_refugees_app.R;

public class HomeActivity extends AppCompatActivity {
    private Button btnRefugees,btnMap,btnDonation,btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btnRefugees = findViewById(R.id.btnRefugees);
        btnMap = findViewById(R.id.btnMap);
        btnDonation = findViewById(R.id.btnDonation);
        btnProfile = findViewById(R.id.btnProfile);
        btnRefugees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RefugeesActivity.class);
                startActivity(intent);
            }
        });
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        btnDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, DonationActivity.class);
                startActivity(intent);
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,UserProfileActivity.class);
                startActivity(intent);
            }
        });
    }
}
