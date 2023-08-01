package com.example.firebase_refugees_app.Activity.Refugees;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.firebase_refugees_app.R;
import com.example.firebase_refugees_app.Utils.ReadWriteRefugeeDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class RefugeeDetailActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeContainer;
    private TextView textViewWelcome,textViewFullName,textViewDoB,textViewGender,textViewCountry;
    private ProgressBar progressBar;
    private String fullName,doB,gender,country,uuid;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refugee_detail);
        Bundle extras = getIntent().getExtras();
        uuid = extras.getString("id");
        getSupportActionBar().setTitle(uuid);
        swipeToRefresh();
        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewDoB = findViewById(R.id.textView_show_dob);
        textViewGender = findViewById(R.id.textView_show_gender);
        textViewCountry = findViewById(R.id.textView_show_country);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView_profile_dp);
        progressBar.setVisibility(View.VISIBLE);
        showRefugee();
    }

    private void showRefugee() {
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Refugees");
        referenceProfile.child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteRefugeeDetails readURefugeeDetails = snapshot.getValue(ReadWriteRefugeeDetails.class);
                if (readURefugeeDetails != null) {
                    fullName = readURefugeeDetails.name;
                    doB = readURefugeeDetails.doB;
                    gender = readURefugeeDetails.gender;
                    country = readURefugeeDetails.country;
                    textViewWelcome.setText(fullName);
                    textViewFullName.setText(fullName);
                    textViewDoB.setText(doB);
                    textViewGender.setText(gender);
                    textViewCountry.setText(country);
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();
                    StorageReference pictureRef = storageRef.child("RefugeesPics").child(uuid);
                    pictureRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(RefugeeDetailActivity.this).load(uri).into(imageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors that occurred during the download
                            if (exception instanceof StorageException) {
                                StorageException storageException = (StorageException) exception;
                                int errorCode = storageException.getErrorCode();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RefugeeDetailActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RefugeeDetailActivity.this,"Something went wrong!",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void swipeToRefresh() {
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startActivity(getIntent());
                finish();
                overridePendingTransition(0,0);
                swipeContainer.setRefreshing(false);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,android.R.color.holo_green_light,android.R.color.holo_orange_light,android.R.color.holo_red_light);
    }
}