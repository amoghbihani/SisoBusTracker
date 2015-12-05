package com.batti.nil.sisobustracker;

import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.batti.nil.sisobustracker.location.LocationHandler;
import com.batti.nil.sisobustracker.location.LocationHandlerClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity {
    private static final String TAG = "MapsActivity";

    private static final LatLng OFFICE = new LatLng(12.980113, 77.696481);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationHandler mLocationHandler;

    private Marker mUserMarker;
    private Marker mOfficeMarker;
    private Marker mBusMarker;

    private int delay_1_sec = 1000;
    private final int UPDATE_CORDINATES_FAKE = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CORDINATES_FAKE:
                    Location newLoc = new Location(location.getLa)
                    updateCurrentLocation(location.);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mLocationHandler = new LocationHandler(this, new LocationHandlerClientImpl());
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap == null) return;

            mOfficeMarker = mMap.addMarker(new MarkerOptions()
                                    .position(OFFICE));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(OFFICE, 15));
            updateCurrentLocation(mLocationHandler.getCurrentLocation());
        }
    }

    private void updateCurrentLocation(Location location) {
        if (location == null) {
            Log.d(TAG, "Location not available yet");
            return;
        }
        if (mMap == null) {
            Log.d(TAG, "Map not available yet");
            return;
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mUserMarker == null) {
            mUserMarker = mMap.addMarker(new MarkerOptions()
                                  .position(latLng));
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    getMapBounds(location), size.x - 50, size.y - 100, 10));
        } else {
            mUserMarker.setPosition(latLng);
        }
    }

    private LatLngBounds getMapBounds(Location user) {
        double north, east, west, south;
        north = Math.max(OFFICE.latitude, user.getLatitude());
        south = Math.min(OFFICE.latitude, user.getLatitude());
        east = Math.max(OFFICE.longitude, user.getLongitude());
        west = Math.min(OFFICE.longitude, user.getLongitude());
        return new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
    }

    private class LocationHandlerClientImpl extends LocationHandlerClient {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged " + location.getLatitude()
                    + " " + location.getLongitude());
            updateCurrentLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged " + status);
        }
    }
}
