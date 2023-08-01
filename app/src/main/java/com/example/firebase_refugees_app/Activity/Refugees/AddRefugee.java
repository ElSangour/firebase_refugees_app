package com.example.firebase_refugees_app.Activity.Refugees;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firebase_refugees_app.R;
import com.example.firebase_refugees_app.Utils.ReadWriteRefugeeDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.UUID;

public class AddRefugee extends AppCompatActivity {
    private EditText editTextRegisterName, editTextRegisterDoB, editTextCountry;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;
    private ImageView imageViewPicture;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri uriImage;

    private UUID uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_refugee);
        getSupportActionBar().setTitle("Add Refugees");
        Toast.makeText(AddRefugee.this, "You can Add a Refugee Now", Toast.LENGTH_LONG).show();
        progressBar = findViewById(R.id.progressBar);
        editTextRegisterName = findViewById(R.id.editText_register_full_name);
        editTextRegisterDoB = findViewById(R.id.editText_register_dob);
        editTextCountry = findViewById(R.id.editText_register_Country);
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();
        imageViewPicture = findViewById(R.id.imageView_refugee);
        uuid = UUID.randomUUID();
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                picker = new DatePickerDialog(AddRefugee.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });
        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);
                String textFullName = editTextRegisterName.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textCountry = editTextCountry.getText().toString();
                String textGender;
                if (TextUtils.isEmpty(textFullName)) {
                    Toast.makeText(AddRefugee.this, "Please Enter the full name", Toast.LENGTH_LONG).show();
                    editTextRegisterName.setError("Full Name is Required");
                    editTextRegisterName.requestFocus();
                } else if (TextUtils.isEmpty(textDoB)) {
                    Toast.makeText(AddRefugee.this, "Please Enter the date of birth", Toast.LENGTH_LONG).show();
                    editTextRegisterDoB.setError("Date of Birth is Required");
                    editTextRegisterDoB.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(AddRefugee.this, "Please Select your gender", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Gender is Required");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    addRefugee(textFullName, textDoB, textGender, textCountry);
                    uploadPic();
                }
            }
        });

        imageViewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            System.out.println(uriImage);
            imageViewPicture.setImageURI(uriImage);
            Toast.makeText(AddRefugee.this, "Load is OK", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddRefugee.this, "Load is failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPic() {
        if (uriImage != null) {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar while uploading
            storageReference = FirebaseStorage.getInstance().getReference("RefugeesPics");
            StorageReference fileReference = storageReference.child(uuid.toString());
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE); // Hide progress bar after successful upload
                    Toast.makeText(AddRefugee.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressBar.setVisibility(View.GONE); // Hide progress bar after upload failure
                    Toast.makeText(AddRefugee.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(AddRefugee.this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void addRefugee(String textName, String textDoB, String textGender, String textCountry) {
        ReadWriteRefugeeDetails writeRefugeeDetails = new ReadWriteRefugeeDetails(uuid.toString(),textName, textDoB, textGender, textCountry);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Refugees");
        reference.child(uuid.toString()).setValue(writeRefugeeDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddRefugee.this, "Refugee Registered successfully", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(AddRefugee.this, RefugeesActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddRefugee.this, "Failed to register refugee", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
