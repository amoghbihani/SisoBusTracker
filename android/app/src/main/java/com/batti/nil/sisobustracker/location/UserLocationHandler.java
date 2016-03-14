package com.batti.nil.sisobustracker.location;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;

import com.batti.nil.sisobustracker.R;

public class UserLocationHandler {
    private static final String TAG = "UserLocationHandler";

    private Context mContext;
    private LocationManager mLocationManager;
    private UserLocationHandlerClient mClient;

    public UserLocationHandler(Context context, UserLocationHandlerClient client) {
        mContext = context;
        mClient = client;

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (!isLocationEnabled()) {
            mClient.createGPSOffAlert();
        }
        requestLocationUpdates();
    }

    public void requestLocationUpdates() {
        mLocationManager.requestLocationUpdates(
                getBestLocationProvider(), 2 * 1000, 10, new UserLocationListener(mClient));
    }

    public Location getCurrentLocation() {
        return mLocationManager.getLastKnownLocation(getBestLocationProvider());
    }

    private boolean isLocationEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private String getBestLocationProvider() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedRequired(true);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return mLocationManager.getBestProvider(criteria, true);
    }
}
