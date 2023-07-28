package com.example.firebase_refugees_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextLoginEmail;
    private EditText editTextPwd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login");
        authProfile = FirebaseAuth.getInstance();
        Button buttonForgotPassword = findViewById(R.id.button_forgot_password);
        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this,"You can reset your password now!",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextPwd =  findViewById(R.id.editText_login_password);
        progressBar =  findViewById(R.id.progressBar);
        ImageView imageViewShowHidePwd = findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    editTextPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                } else {
                    editTextPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = editTextLoginEmail.getText().toString();
                String textPwd = editTextPwd.getText().toString();

                if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Email is required");
                    editTextLoginEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please re-enter your email", Toast.LENGTH_SHORT).show();
                    editTextLoginEmail.setError("Please enter a valid email");
                    editTextLoginEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPwd)) {
                    Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    editTextPwd.setError("Password is required");
                    editTextPwd.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail,textPwd);
//                    showAlertDialog();
                }
            }
            private void loginUser(String textEmail, String textPwd) {
                authProfile.signInWithEmailAndPassword(textEmail, textPwd)
                      .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this,"You are logged in now",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    try{
                                        throw task.getException();
                                    } catch ( FirebaseAuthInvalidUserException e){
                                        editTextLoginEmail.setError("User doesn't exist");
                                        editTextLoginEmail.requestFocus();
                                    } catch(FirebaseAuthInvalidCredentialsException e){
                                        editTextLoginEmail.setError("Invalid credentials");
                                        editTextLoginEmail.requestFocus();
                                    } catch(Exception e){
                                        Log.e(TAG,e.getMessage());
                                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }
}