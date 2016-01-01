package com.batti.nil.sisobustracker;

import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.batti.nil.sisobustracker.common.MathUtils;
import com.batti.nil.sisobustracker.location.BusLocation;
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
import com.parse.Parse;
import com.parse.ParseObject;

public class MapsActivity extends FragmentActivity {
    private static final String TAG = "MapsActivity";
    private static final String APPLICATION_ID="dqRUCRTKgKIqhgMKOE096W85NmPxj9kfRXAFYMrH";
    private static final String CLIENT_ID="SUi9RPni3ihmaUThh9lx9NMuUERKDw08miLjtxG6";

    private static final LatLng OFFICE = new LatLng(12.980113, 77.696481);
    private static final int REQUEST_BUS_LOCATION = 0;
    private static final int REQUEST_BUS_LOCATION_DELAY = 2000;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private UserLocationHandler mUserLocationHandler;
    private BusLocationHandler mBusLocationHandler;
    private String mRouteNumber = "9"; // TODO: add logic to take user input.
    private boolean mIsWaiting = true;

    private Marker mUserMarker;
    private Marker mOfficeMarker;
    private Marker mBusMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mUserLocationHandler = new UserLocationHandler(this, new UserLocationHandlerClientImpl());
        mBusLocationHandler = new BusLocationHandler(mRouteNumber,
                new BusLocationHandlerClientImpl());

        try {
            ParseObject.registerSubclass(BusLocation.class);
            Parse.initialize(this, APPLICATION_ID, CLIENT_ID);
        } catch (IllegalStateException e) {
            Log.d(TAG, "Parse already initialized");
        }

        addUIElements();
        setUpMapIfNeeded();
        requestBusLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        requestBusLocation();
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
            updateUserLocation(mUserLocationHandler.getCurrentLocation());
        }
    }

    private void updateUserLocation(Location location) {
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
            LatLngBounds bounds;
            if (mBusMarker != null) {
                bounds = getMapBounds(
                        officeLocation, location, mBusLocationHandler.getCurrentLocation());
            } else {
                Log.d(TAG, "Bus marker not available yet");
                bounds = getMapBounds(officeLocation, location);
            }
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    bounds, size.x, size.y - 200, 50));
        } else {
            mUserMarker.setPosition(latLng);
        }

        if (!mIsWaiting) {
            mBusLocationHandler.sendBusLocation(location);
            if (mBusMarker != null) {
                mBusMarker.setPosition(latLng);
            }
        }
    }

    private void updateBusLocation(Location location) {
        if (location == null) {
            Log.d(TAG, "Location not available yet");
            return;
        }
        if (mMap == null) {
            Log.d(TAG, "Map not available yet");
            return;
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mBusMarker == null) {
            mBusMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
            Location officeLocation = new Location("office");
            officeLocation.setLatitude(OFFICE.latitude);
            officeLocation.setLongitude(OFFICE.longitude);
            LatLngBounds bounds;
            if (mUserMarker != null) {
                bounds = getMapBounds(
                        officeLocation, location, mUserLocationHandler.getCurrentLocation());
            } else {
                Log.d(TAG, "User marker not available yet");
                bounds = getMapBounds(officeLocation, location);
            }
            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    bounds, size.x - 50, size.y - 100, 10));
        } else {
            mBusMarker.setPosition(latLng);
        }
    }

    private void addUIElements() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color));
        } else {
            Log.d(TAG, "status bar color change not supported");
        }

        TextView routeNumber = (TextView) findViewById(R.id.route_number_text);
        routeNumber.setText("R:" + mRouteNumber);
    }

    private void requestBusLocation() {
        if (!mIsWaiting) return;
        mBusLocationHandler.requestBusLocation();
        mHandler.sendEmptyMessageDelayed(REQUEST_BUS_LOCATION, REQUEST_BUS_LOCATION_DELAY);
    }

    private void stopBusLocationRequest() {
        mHandler.removeMessages(REQUEST_BUS_LOCATION);
    }

    public void onRadioButtonClicked(View view) {
        boolean isChecked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.waiting_radio_button:
                if (isChecked) {
                    Log.d(TAG, "waiting button checked");
                    mIsWaiting = true;
                    requestBusLocation();
                }
                break;
            case R.id.inside_radio_button:
                if (isChecked) {
                    Log.d(TAG, "inside button checked");
                    mIsWaiting = false;
                    stopBusLocationRequest();
                    mBusLocationHandler.sendBusLocation(mUserLocationHandler.getCurrentLocation());
                }
                break;
            default:
                Log.d(TAG, "Something went wrong.");
                break;
        }
    }

    private class UserLocationHandlerClientImpl extends UserLocationHandlerClient {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged " + location.getLatitude()
                    + " " + location.getLongitude());
            updateUserLocation(location);
        }
    }

    private class BusLocationHandlerClientImpl extends BusLocationHandlerClient {
        @Override
        public void onResponseReceived(Location location) {
            updateBusLocation(location);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_BUS_LOCATION:
                    MapsActivity.this.requestBusLocation();
                    break;
                default:
                    break;
            }
        }
    };


    private LatLngBounds getMapBounds(Location loc1, Location loc2) {
        double north, east, west, south;
        north = MathUtils.max(loc1.getLatitude(), loc2.getLatitude());
        south = MathUtils.min(loc1.getLatitude(), loc2.getLatitude());
        east = MathUtils.max(loc1.getLongitude(), loc2.getLongitude());
        west = MathUtils.min(loc1.getLongitude(), loc2.getLongitude());
        return new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
    }

    private LatLngBounds getMapBounds(Location loc1, Location loc2, Location loc3) {
        double north, east, west, south;
        north = MathUtils.max(loc1.getLatitude(), loc2.getLatitude(), loc3.getLatitude());
        south = MathUtils.min(loc1.getLatitude(), loc2.getLatitude(), loc3.getLatitude());
        east = MathUtils.max(loc1.getLongitude(), loc2.getLongitude(), loc3.getLongitude());
        west = MathUtils.min(loc1.getLongitude(), loc2.getLongitude(), loc3.getLongitude());
        return new LatLngBounds(new LatLng(south, west), new LatLng(north, east));
    }
}
