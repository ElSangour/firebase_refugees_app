package com.example.firebase_refugees_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {
    private EditText editTextUpdateName,editTextUpdateDob,editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName,textGender,textDob,textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        getSupportActionBar().setTitle("Update Profile Activity");
        progressBar = findViewById(R.id.progressBar);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDob = findViewById(R.id.editText_update_profile_dob);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);
        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);
        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        showProfile(firebaseUser);
        editTextUpdateDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                String textSADoB[] = textDob.split("/");
                int day = Integer.parseInt(textSADoB[0]);
                int month = Integer.parseInt(textSADoB[1]) - 1 ;
                int year = Integer.parseInt(textSADoB[2]);
                DatePickerDialog picker = new DatePickerDialog(UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        editTextUpdateDob.setText(dayOfMonth + "/" + (month+1)+ "/" + year);
                    }
                },year,month,day);
                picker.show();
            }
        });
        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });
    }
    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);
        textGender = radioButtonUpdateGenderSelected.getText().toString();
        textFullName = editTextUpdateName.getText().toString();
        textDob = editTextUpdateDob.getText().toString();
        textMobile = editTextUpdateMobile.getText().toString();
        String mobileRegex = "\"^(9|5|2)\\\\d{7}$\";";
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        Matcher mobileMatcher = mobilePattern.matcher(textMobile);
        if (TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfileActivity.this,"Please Enter your full name", Toast.LENGTH_LONG);
            editTextUpdateName.setError("Full Name is Required");
            editTextUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(textDob)){
            Toast.makeText(UpdateProfileActivity.this,"Please Enter your date of birth", Toast.LENGTH_LONG);
            editTextUpdateDob.setError("Date of Birth is Required");
            editTextUpdateDob.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonUpdateGenderSelected.getText())){
            Toast.makeText(UpdateProfileActivity.this,"Please select gender", Toast.LENGTH_LONG);
            radioButtonUpdateGenderSelected.setError("Gender is Required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)){
            Toast.makeText(UpdateProfileActivity.this,"Please Enter your mobile number", Toast.LENGTH_LONG);
            editTextUpdateMobile.setError("Mobile Number is Required");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 8){
            Toast.makeText(UpdateProfileActivity.this,"Please re-Enter your mobile number", Toast.LENGTH_LONG);
            editTextUpdateMobile.setError("Valid Mobile Number is Required");
            editTextUpdateMobile.requestFocus();
        } else {
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textDob,textGender,textMobile);
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
            String userID = firebaseUser.getUid();
            progressBar.setVisibility(View.VISIBLE);
            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);
                        Toast.makeText(UpdateProfileActivity.this,"Profile Updated",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdateProfileActivity.this,UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try{
                            throw task.getException();
                        } catch ( Exception e){
                            Toast.makeText(UpdateProfileActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
    private void showProfile(FirebaseUser firebaseUser){
        String userIDofRegistered = firebaseUser.getUid();
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
        progressBar.setVisibility(View.VISIBLE);
        referenceProfile.child(userIDofRegistered).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ReadWriteUserDetails readUserDetails = dataSnapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails!=null){
                    textFullName = firebaseUser.getDisplayName();
                    textDob = readUserDetails.doB;
                    textGender = readUserDetails.gender;
                    textMobile = readUserDetails.mobile;
                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDob.setText(textDob);
                    editTextUpdateMobile.setText(textMobile);
                    if (textGender.equals("Male")){
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    } else {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfileActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateProfileActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }

        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_refresh){
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UpdateProfileActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UpdateProfileActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.menu_settings){
            Toast.makeText(UserProfileActivity.this,"menu_settings",Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UserProfileActivity.this,ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(UserProfileActivity.this,DeleteProfileActivity.class);
            startActivity(intent);
        }*/ else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this,"Logout",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UpdateProfileActivity.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateProfileActivity.this,"Something went Wrong",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}