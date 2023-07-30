package com.example.firebase_refugees_app.Activity.Refugees;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.firebase_refugees_app.Activity.Auth.RegisterActivity;
import com.example.firebase_refugees_app.Activity.User.DeleteProfileActivity;
import com.example.firebase_refugees_app.Activity.User.UserProfileActivity;
import com.example.firebase_refugees_app.R;
import com.example.firebase_refugees_app.Utils.ReadWriteRefugeeDetails;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RefugeesActivity extends AppCompatActivity implements RefugeeAdapter.OnDeleteClickListener {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listViewRefugees;
    private RefugeeAdapter adapter;
    private ArrayList<ReadWriteRefugeeDetails> refugeeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refugees);
        getSupportActionBar().setTitle("Refugees");
        Button add = findViewById(R.id.button_add);
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
                // Handle the case when a refugee is removed from the database
                ReadWriteRefugeeDetails removedRefugee = dataSnapshot.getValue(ReadWriteRefugeeDetails.class);
                if (removedRefugee != null) {
                    refugeeList.remove(removedRefugee);
                    adapter.notifyDataSetChanged();

                    // Delete the refugee data from the Firebase Realtime Database
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


    // Implement the onDeleteClick method of the OnDeleteClickListener interface
    @Override
    public void onDeleteClick(ReadWriteRefugeeDetails refugee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RefugeesActivity.this);
        builder.setTitle("Delete User and Related Data");
        builder.setMessage("Do Your Really want to delete your profile and related data?");
        builder.setPositiveButton("Continue",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refugeeList.remove(refugee);
                adapter.notifyDataSetChanged();
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Refugees");
                databaseRef.child(refugee.id).removeValue();
                Toast.makeText(RefugeesActivity.this, "Refugee deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
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
}