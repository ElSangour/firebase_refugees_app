package com.example.firebase_refugees_app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.firebase_refugees_app.Activity.Refugees.AddRefugee;
import com.example.firebase_refugees_app.R;
import com.example.firebase_refugees_app.Utils.LocationEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;
    private IMapController mapController;
    private ItemizedOverlayWithFocus<OverlayItem> markerOverlay;

    private FloatingActionButton fabAddLocation;
    private boolean isAddingLocation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setTitle("Camping Map");
        // Initialize osmdroid library
        Configuration.getInstance().load(getApplicationContext(),
                getSharedPreferences("osmdroid", MODE_PRIVATE));

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        // Set the initial map center and zoom level
        mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(40.7128, -74.0060); // New York City
        mapController.setCenter(startPoint);

        // Fetch location data from Firebase Realtime Database (Replace this with your implementation)
        ArrayList<GeoPoint> locations = fetchLocationsFromFirebase();

        // Add markers to the map
        addMarkers(locations);
        fabAddLocation = findViewById(R.id.fab_add_location);
        fabAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event of the floating action button
                toggleAddingLocation();
            }
        });


    }
    private void toggleAddingLocation() {
        isAddingLocation = !isAddingLocation;
        if (isAddingLocation) {
            // Prompt the user to select a location on the map
            Toast.makeText(this, "Tap on the map to add a new location", Toast.LENGTH_SHORT).show();
            mapView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Get the latitude and longitude of the tapped point
                        GeoPoint newLocation = (GeoPoint) mapView.getProjection().fromPixels(
                                (int) event.getX(), (int) event.getY());

                        addNewLocationMarker(newLocation);

                        mapView.setOnTouchListener(null);
                        isAddingLocation = false;
                        return true;
                    }
                    return false;
                }
            });
        } else {
            // Stop adding location and reset the map touch listener
            mapView.setOnTouchListener(null);
        }
    }

    private void addNewLocationMarker(GeoPoint location) {
        // Create a new marker and add it to the map
        OverlayItem newMarker = new OverlayItem("Camps", "", location);
        markerOverlay.addItem(newMarker);
        mapView.invalidate(); // Refresh the map

        // You can also save the new location to Firebase Realtime Database here
        // Replace this with your implementation to store the new location in Firebase
        saveLocationToFirebase(location);
    }

    private void saveLocationToFirebase(GeoPoint location) {
        // Get a reference to the "locations" node in your Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("locations");

        // Generate a unique key for the new location entry
        String newLocationKey = databaseRef.push().getKey();

        // Create a new location entry with latitude and longitude
        LocationEntry newLocation = new LocationEntry(location.getLatitude(), location.getLongitude());

        // Save the new location entry to Firebase Realtime Database
        databaseRef.child(newLocationKey).setValue(newLocation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Location saved successfully
                        Toast.makeText(MapActivity.this, "Location saved to Firebase", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save location
                        Toast.makeText(MapActivity.this, "Failed to save location to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addMarkers(ArrayList<GeoPoint> locations) {
        ArrayList<OverlayItem> items = new ArrayList<>();
        for (GeoPoint location : locations) {
            items.add(new OverlayItem("Marker", "Location", location));
        }

        markerOverlay = new ItemizedOverlayWithFocus<>(this, items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        // Handle marker click event here
                        Toast.makeText(MapActivity.this, "Marker Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        // Handle marker long press event here
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        builder.setTitle("Remove Marker");
                        builder.setMessage("Do you want to remove this marker?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Remove the marker from the markerOverlay
                                markerOverlay.removeItem(index);
                                mapView.invalidate(); // Refresh the map after removing the marker
                                // Retrieve the location entry from Firebase using the index (or any unique identifier)
                                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("locations");
                                databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // Find the corresponding location entry and delete it
                                        for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                                            LocationEntry locationEntry = locationSnapshot.getValue(LocationEntry.class);
                                            if (locationEntry != null) {
                                                double latitude = locationEntry.getLatitude();
                                                double longitude = locationEntry.getLongitude();
                                                GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                                                if (item.getPoint().equals(geoPoint)) {
                                                    // Remove the location entry from Firebase
                                                    locationSnapshot.getRef().removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    // Location entry removed successfully from Firebase
                                                                    Toast.makeText(MapActivity.this, "Location removed from Firebase", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Failed to remove location entry from Firebase
                                                                    Toast.makeText(MapActivity.this, "Failed to remove location from Firebase", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(MapActivity.this, "Failed to fetch locations from Firebase", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        builder.setNegativeButton("No", null);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        return true;
                    }

                });

        markerOverlay.setFocusItemsOnTap(true);
        mapView.getOverlays().add(markerOverlay);
        mapView.invalidate();
    }

    private ArrayList<GeoPoint> fetchLocationsFromFirebase() {
        ArrayList<GeoPoint> locations = new ArrayList<>();
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("locations");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    LocationEntry locationEntry = locationSnapshot.getValue(LocationEntry.class);
                    if (locationEntry != null) {
                        double latitude = locationEntry.getLatitude();
                        double longitude = locationEntry.getLongitude();
                        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                        locations.add(geoPoint);
                    }
                }
                // Once all locations are fetched, add markers to the map
                addMarkers(locations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapActivity.this, "Failed to fetch locations from Firebase", Toast.LENGTH_SHORT).show();
            }
        });
        return locations;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
