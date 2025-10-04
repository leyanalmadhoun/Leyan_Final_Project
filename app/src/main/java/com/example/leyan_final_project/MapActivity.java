package com.example.leyan_final_project;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocation;
    private ActivityResultLauncher<String[]> reqPerms;
    private double centerLat = 31.5184;
    private double centerLng = 34.4669;
    private String  centerTitle = "شارع النصر - غزة";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);


        if (getIntent() != null) {
            centerLat   = getIntent().getDoubleExtra("lat", centerLat);
            centerLng   = getIntent().getDoubleExtra("lng", centerLng);
            centerTitle = getIntent().getStringExtra("title") != null
                    ? getIntent().getStringExtra("title") : centerTitle;
        }

        fusedLocation = LocationServices.getFusedLocationProviderClient(this);

        reqPerms = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fine = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                    boolean coarse = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                    if (fine || coarse) enableMyLocationAndCenter();
                });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        LatLng target = new LatLng(centerLat, centerLng);
        mMap.addMarker(new MarkerOptions().position(target).title(centerTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16f));

        enableMyLocationAndCenter();
    }

    private void enableMyLocationAndCenter() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) mMap.setMyLocationEnabled(true);

            fusedLocation.getLastLocation().addOnSuccessListener(this, (Location location) -> {
                if (location != null && mMap != null) {

                }
            });
        } else {
            reqPerms.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }
}
