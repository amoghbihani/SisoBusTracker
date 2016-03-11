package com.batti.nil.sisobustracker.location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.batti.nil.sisobustracker.MapsActivity.MapsActivityClient;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private static final int REQUEST_BUS_LOCATION = 0;
    private static final int REQUEST_BUS_LOCATION_DELAY = 2000;

    private String mRouteNumber;
    private boolean mIsWaiting = true;

    private MapsActivityClient mClient;
    private UserLocationHandler mUserLocationHandler;
    private BusLocationHandler mBusLocationHandler;

    public class LocationServiceBinder extends Binder {
        public void initialize(MapsActivityClient client, String routeNumber, boolean isWaiting) {
            LocationService.this.initialize(client, routeNumber, isWaiting);
        }

        public void onWaitingModeChanged(boolean isWaiting) {
            mIsWaiting = isWaiting;
            if (mIsWaiting) {
                requestBusLocation();
            } else {
                stopBusLocationRequest();
                if (mUserLocationHandler != null) {
                    sendBusLocation(mUserLocationHandler.getCurrentLocation());
                }
            }
        }

        public void requestLocationUpdates() {
            LocationService.this.requestLocationUpdates();
        }

        public Location getUserLocation() {
            return LocationService.this.getUserLocation();
        }

        public Location getBusLocation() {
            return LocationService.this.getBusLocation();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new LocationServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        return Service.START_STICKY;
    }

    private void initialize(MapsActivityClient client, String routeNumber, boolean isWaiting) {
        mClient = client;
        mRouteNumber = routeNumber;
        mIsWaiting = isWaiting;
        mUserLocationHandler = new UserLocationHandler(this, new UserLocationHandlerClientImpl());
        mBusLocationHandler = new BusLocationHandler(mRouteNumber,
                new BusLocationHandlerClientImpl());
        requestBusLocation();
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        mUserLocationHandler.requestLocationUpdates();
    }

    private Location getUserLocation() {
        return mUserLocationHandler.getCurrentLocation();
    }

    private void requestBusLocation() {
        if (!mIsWaiting) return;
        mBusLocationHandler.requestBusLocation();
        mHandler.sendEmptyMessageDelayed(REQUEST_BUS_LOCATION, REQUEST_BUS_LOCATION_DELAY);
    }

    private void stopBusLocationRequest() {
        mHandler.removeMessages(REQUEST_BUS_LOCATION);
    }

    private Location getBusLocation() {
        return mBusLocationHandler.getCurrentLocation();
    }

    private void sendBusLocation(Location location) {
        Log.d(TAG, "sendBusLocation " + location.getLatitude()
                + " " + location.getLatitude());
        mBusLocationHandler.sendBusLocation(location);
    }

    private class UserLocationHandlerClientImpl extends UserLocationHandlerClient {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged " + location.getLatitude()
                    + " " + location.getLongitude());
            mClient.updateUserLocation(location);
            if (!mIsWaiting) {
                sendBusLocation(location);
            }
        }

        @Override
        public void exitApplication() {
            mClient.exitApplication();
        }
    }

    private class BusLocationHandlerClientImpl extends BusLocationHandlerClient {
        @Override
        public void onResponseReceived(Location location) {
            Log.d(TAG, "onResponseReceived bus location " + location.getLatitude()
                    + " " + location.getLongitude());
            mClient.updateBusLocation(location);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_BUS_LOCATION:
                    requestBusLocation();
                    break;
                default:
                    break;
            }
        }
    };
}
