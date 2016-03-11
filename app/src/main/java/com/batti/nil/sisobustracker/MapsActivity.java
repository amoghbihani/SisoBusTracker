package com.batti.nil.sisobustracker;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.batti.nil.sisobustracker.common.MathUtils;
import com.batti.nil.sisobustracker.location.BusLocation;
import com.batti.nil.sisobustracker.location.LocationService;
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
    private static final String APPLICATION_ID = "dqRUCRTKgKIqhgMKOE096W85NmPxj9kfRXAFYMrH";
    private static final String CLIENT_ID = "SUi9RPni3ihmaUThh9lx9NMuUERKDw08miLjtxG6";
    private static final LatLng OFFICE = new LatLng(12.980113, 77.696481);

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String mRouteNumber = "9"; // TODO: add logic to take user input.
    private boolean mIsWaiting = true;
    private LocationService.LocationServiceBinder mBinder;

    private Marker mUserMarker;
    private Marker mOfficeMarker;
    private Marker mBusMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        try {
            ParseObject.registerSubclass(BusLocation.class);
            Parse.initialize(this, APPLICATION_ID, CLIENT_ID);
        } catch (IllegalStateException e) {
            Log.d(TAG, "Parse already initialized");
        }

        startUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(mConnection);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void startUp() {
        addUIElements();
        mIsWaiting = ((RadioButton) findViewById(R.id.waiting_radio_button)).isChecked();

        if (mBinder == null) {
            Intent locationServiceIntent = new Intent(this, LocationService.class);
            bindService(locationServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            mBinder.requestLocationUpdates();
        }

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
            Location officeLocation = getOfficeLocation();
            LatLngBounds bounds;
            if (mBusMarker != null) {
                bounds = getMapBounds(officeLocation, location, getBusLocation());
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

        if (!mIsWaiting && mBusMarker != null) {
            mBusMarker.setPosition(latLng);
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
            Location officeLocation = getOfficeLocation();
            LatLngBounds bounds;
            if (mUserMarker != null) {
                bounds = getMapBounds(officeLocation, location, getUserLocation());
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

    public void onRadioButtonClicked(View view) {
        boolean isChecked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.waiting_radio_button:
                if (isChecked) {
                    Log.d(TAG, "waiting button checked");
                    mIsWaiting = true;
                    onWaitingModeChanged();
                }
                break;
            case R.id.inside_radio_button:
                if (isChecked) {
                    createInsideBusConfirmationAlert();
                }
                break;
            default:
                Log.d(TAG, "Something went wrong.");
                break;
        }
    }

    private void createInsideBusConfirmationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.inside_confirmation)
                .setCancelable(false)
                .setPositiveButton(R.string.yes_im_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "inside button checked");
                        mIsWaiting = false;
                        onWaitingModeChanged();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioButton button = (RadioButton) findViewById(R.id.waiting_radio_button);
                        button.setChecked(true);
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private Location getOfficeLocation() {
        Location officeLocation = new Location("office");
        officeLocation.setLatitude(OFFICE.latitude);
        officeLocation.setLongitude(OFFICE.longitude);
        return officeLocation;
    }

    private Location getUserLocation() {
        if (mBinder == null) return getOfficeLocation();
        return mBinder.getUserLocation();
    }

    private Location getBusLocation() {
        if (mBinder == null) return getOfficeLocation();
        return mBinder.getBusLocation();
    }

    private void onWaitingModeChanged() {
        if (mBinder == null) return;
        mBinder.onWaitingModeChanged(mIsWaiting);
    }

    public class MapsActivityClient {
        public void updateUserLocation(Location location) {
            MapsActivity.this.updateUserLocation(location);
        }

        public void updateBusLocation(Location location) {
            MapsActivity.this.updateBusLocation(location);
        }

        public void exitApplication() {
            finish();
            System.exit(1);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (LocationService.LocationServiceBinder) service;
            mBinder.initialize(new MapsActivityClient(), mRouteNumber, mIsWaiting);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
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
