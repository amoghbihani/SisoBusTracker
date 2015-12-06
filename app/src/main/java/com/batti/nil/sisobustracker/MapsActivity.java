package com.batti.nil.sisobustracker;

import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.VolleyError;
import com.batti.nil.sisobustracker.location.BusLocationHandler;
import com.batti.nil.sisobustracker.location.BusLocationHandlerClient;
import com.batti.nil.sisobustracker.location.UserLocationHandler;
import com.batti.nil.sisobustracker.location.UserLocationHandlerClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

public class MapsActivity extends FragmentActivity {
    private static final String TAG = "MapsActivity";

    private static final LatLng OFFICE = new LatLng(12.980113, 77.696481);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private UserLocationHandler mUserLocationHandler;
    private BusLocationHandler mBusLocationHandler;

    private Marker mUserMarker;
    private Marker mOfficeMarker;
    private Marker mBusMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mUserLocationHandler = new UserLocationHandler(this, new UserLocationHandlerClientImpl());
        mBusLocationHandler = new BusLocationHandler(this, new BusLocationHandlerClientImpl());
        setUpMapIfNeeded();
        addUIElements();
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
                    .position(OFFICE)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.office_building)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(OFFICE, 15));
            updateCurrentLocation(mUserLocationHandler.getCurrentLocation());
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
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.user)));
            Location officeLocation = new Location("office");
            officeLocation.setLatitude(OFFICE.latitude);
            officeLocation.setLongitude(OFFICE.longitude);
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    getMapBounds(officeLocation, location),
                    size.x - 50, size.y - 100, 10));
        } else {
            mUserMarker.setPosition(latLng);
        }
    }

    private LatLngBounds getMapBounds(Location loc1, Location loc2) {
        double north, east, west, south;
        north = Math.max(loc1.getLatitude(), loc2.getLatitude());
        south = Math.min(loc1.getLatitude(), loc2.getLatitude());
        east = Math.max(loc1.getLongitude(), loc2.getLongitude());
        west = Math.min(loc1.getLongitude(), loc2.getLongitude());
        return new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
    }

    private void addUIElements() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color));
        } else {
            Log.d(TAG, "status bar color change not supported");
        }
    }

    private class UserLocationHandlerClientImpl extends UserLocationHandlerClient {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged " + location.getLatitude()
                    + " " + location.getLongitude());
            updateCurrentLocation(location);
        }
    }

    private class BusLocationHandlerClientImpl extends BusLocationHandlerClient {
        @Override
        public void onResponseReceived(JSONObject object) {
            Log.d(TAG, "onResponseReceived");
        }

        @Override
        public void onErrorReceivingResponse(VolleyError error) {
            Log.d(TAG, "onErrorReceivingResponse");
        }

        @Override
        public void onErrorSendingRequest(VolleyError error) {
            Log.d(TAG, "onErrorSendingRequest");
        }
    }
}
