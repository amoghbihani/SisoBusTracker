package com.batti.nil.sisobustracker.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocationListenerImpl implements LocationListener {
    private static final String TAG = "LocationListenerImpl";

    private LocationHandlerClient mClient;

    public LocationListenerImpl(LocationHandlerClient client) {
        mClient = client;
    }

    @Override
    public void onLocationChanged(Location location) {
        mClient.onLocationChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        mClient.onStatusChanged(provider, status, extras);
    }
}
