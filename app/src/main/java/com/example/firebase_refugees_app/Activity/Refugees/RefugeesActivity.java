package com.example.firebase_refugees_app.Activity.Refugees;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.firebase_refugees_app.R;
import com.example.firebase_refugees_app.Utils.ReadWriteRefugeeDetails;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class RefugeesActivity extends AppCompatActivity implements RefugeeAdapter.OnDeleteClickListener, RefugeeAdapter.OnEditClickListener, RefugeeAdapter.OnViewClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listViewRefugees;
    private RefugeeAdapter adapter;
    private ArrayList<ReadWriteRefugeeDetails> refugeeList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refugees);
        getSupportActionBar().setTitle("Refugees");
        Button scan = findViewById(R.id.button_scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the method to start QR code scanning
                startQrCodeScanner();
            }
        });
        Button add = findViewById(R.id.button_add);
        progressBar = findViewById(R.id.progressBar);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RefugeesActivity.this, AddRefugee.class);
                startActivity(intent);
            }
        });
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        listViewRefugees = findViewById(R.id.listView_refugees);

        refugeeList = new ArrayList<>();
        adapter = new RefugeeAdapter(this, refugeeList);
        adapter.setOnDeleteClickListener(this); // Set the delete button click listener
        adapter.setOnViewClickListener(this); // Set the view button click listener
        listViewRefugees.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refugeeList.clear();
                fetchRefugeesFromDatabase();
            }
        });
        fetchRefugeesFromDatabase();

    }

    private void fetchRefugeesFromDatabase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Refugees");
        databaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @NonNull String previousChildName) {
                ReadWriteRefugeeDetails refugee = dataSnapshot.getValue(ReadWriteRefugeeDetails.class);
                if (refugee != null) {
                    refugeeList.add(refugee);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @NonNull String previousChildName) {
                // Handle the case when a refugee's data is changed in the database
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                ReadWriteRefugeeDetails removedRefugee = dataSnapshot.getValue(ReadWriteRefugeeDetails.class);
                if (removedRefugee != null) {
                    refugeeList.remove(removedRefugee);
                    adapter.notifyDataSetChanged();
                    DatabaseReference refToDelete = dataSnapshot.getRef();
                    refToDelete.removeValue();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @NonNull String previousChildName) {
                // Handle the case when a refugee's position in the database is changed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        // After fetching data, stop the refreshing animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void startQrCodeScanner() {
        // Create an IntentIntegrator instance for QR code scanning
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0); // Use the back camera by default
        integrator.setBeepEnabled(true); // Play beep sound when a QR code is scanned
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result comes from the QR code scanner
        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the scanned result
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                // Handle the scanned QR code data, e.g., pass it to the RefugeeDetailActivity
                String qrCodeData = result.getContents();
                checkIfIdExists(qrCodeData);
            }
        }
    }
    private void checkIfIdExists(String id) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Refugees");
        databaseRef.child(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    // ID exists, proceed to the RefugeeDetailActivity
                    Intent intent = new Intent(RefugeesActivity.this, RefugeeDetailActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                } else {
                    // ID does not exist in the database, show an error message or perform other action
                    Toast.makeText(RefugeesActivity.this, "Invalid ID", Toast.LENGTH_LONG).show();
                }
            } else {
                // Error occurred while fetching data from the database
                Toast.makeText(RefugeesActivity.this, "Error occurred", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onDeleteClick(ReadWriteRefugeeDetails refugee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RefugeesActivity.this);
        builder.setTitle("Delete User and Related Data");
        builder.setMessage("Do You Really want to delete your profile and related data?");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refugeeList.remove(refugee);
                adapter.notifyDataSetChanged();
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Refugees");
                databaseRef.child(refugee.id).removeValue();
                Toast.makeText(RefugeesActivity.this, "Refugee deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new AlertDialog.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.red));
            }
        });
        alertDialog.show();
    }

    @Override
    public void onEditClick(ReadWriteRefugeeDetails refugee) {
        // Handle the edit button click here (if required)
    }

    @Override
    public void onViewClick(ReadWriteRefugeeDetails refugee) {
        Intent intent = new Intent(RefugeesActivity.this, RefugeeDetailActivity.class);
        intent.putExtra("id", refugee.id);
        startActivity(intent);
    }
}
