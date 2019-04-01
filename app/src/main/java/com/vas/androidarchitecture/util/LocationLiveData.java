package com.vas.androidarchitecture.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import androidx.annotation.RequiresPermission;
import androidx.lifecycle.LiveData;

@SuppressWarnings("unused")
public class LocationLiveData extends LiveData<Location> {
    private static LocationLiveData instance;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;

    @RequiresPermission(anyOf = {
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
    })
    public static LocationLiveData getInstance(Context appContext) {
        if (instance == null) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            instance = new LocationLiveData(appContext, locationRequest);
        }
        return instance;
    }

    @RequiresPermission(anyOf = {
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
    })
    public static LocationLiveData getInstance(Context appContext, LocationRequest locationRequest) {
        if (instance == null) {
            instance = new LocationLiveData(appContext, locationRequest);
        }
        return instance;
    }

    @RequiresPermission(anyOf = {
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
    })
    private LocationLiveData(Context appContext, LocationRequest locationRequest) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null)
                setValue(location);
        });
        this.locationRequest = locationRequest;
    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onActive() {
        super.onActive();
        fusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (location != null)
                    setValue(location);
            }
        }
    };

    @Override
    protected void onInactive() {
        super.onInactive();
        if (mLocationCallback != null)
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}