package com.batti.nil.sisobustracker.location;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.batti.nil.sisobustracker.MapsActivity;
import com.batti.nil.sisobustracker.MapsActivity.MapsActivityClient;
import com.batti.nil.sisobustracker.R;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private static final String BROADCAST_CANCEL_SHARING = "Siso Bus Tracker: Cancel sharing";
    private static final int NOTIFICATION_ID = 10192;
    private static final int NOTIFICATION_REQUEST_CODE = 13187;

    private String mRouteNumber;
    private boolean mIsWaiting = true;

    private MapsActivityClient mClient;
    private UserLocationHandler mUserLocationHandler;
    private BusLocationHandler mBusLocationHandler;
    private Notification mNotification;

    public class LocationServiceBinder extends Binder {
        public void initialize(MapsActivityClient client, String routeNumber, boolean isWaiting) {
            LocationService.this.initialize(client, routeNumber, isWaiting);
        }

        public void onWaitingModeChanged(boolean isWaiting) {
            mIsWaiting = isWaiting;
            if (mIsWaiting) {
                showNotification(false);
            } else {
                sendBusLocation(getUserLocation());
                showNotification(true);
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
        return new LocationServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();

        IntentFilter filter = new IntentFilter(BROADCAST_CANCEL_SHARING);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showNotification(false);
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    private void initialize(MapsActivityClient client, String routeNumber, boolean isWaiting) {
        mClient = client;
        mRouteNumber = routeNumber;
        mIsWaiting = isWaiting;
        mUserLocationHandler = new UserLocationHandler(this, new UserLocationHandlerClientImpl());
        mBusLocationHandler = new BusLocationHandler(mRouteNumber,
                new BusLocationHandlerClientImpl());
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        mUserLocationHandler.requestLocationUpdates();
    }

    private Location getUserLocation() {
        return mUserLocationHandler.getCurrentLocation();
    }

    private Location getBusLocation() {
        return mBusLocationHandler.getCurrentLocation();
    }

    private void sendBusLocation(Location location) {
        Log.d(TAG, "sendBusLocation " + location.getLatitude()
                + " " + location.getLongitude());
        mBusLocationHandler.sendBusLocation(location);
    }

    private void createNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_REQUEST_CODE,
                new Intent(this, MapsActivity.class),
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.awesome))
                .setContentText(getString(R.string.sharing_on))
                .setSmallIcon(R.drawable.notification_badge)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT < 16) {
            mNotification = builder.getNotification();
        } else {
            if (Build.VERSION.SDK_INT > 19) {
                PendingIntent cancelSharePendingIntent = PendingIntent.getBroadcast(
                        this, 0,
                        new Intent(BROADCAST_CANCEL_SHARING),
                        PendingIntent.FLAG_CANCEL_CURRENT);
                builder.addAction(
                        R.drawable.cancel_location,
                        getString(R.string.cancel_sharing),
                        cancelSharePendingIntent);
            }
            if (Build.VERSION.SDK_INT > 20) {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
                builder.setColor(Color.argb(100, 0, 171, 238));
            }
            builder.setPriority(Notification.PRIORITY_HIGH);
            mNotification = builder.build();
        }
    }

    private void showNotification(boolean enable) {
        if (enable) {
            startForeground(NOTIFICATION_ID, mNotification);
        } else {
            stopForeground(true);
        }
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
        public void createGPSOffAlert() {
            mClient.createGPSOffAlert();
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

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BROADCAST_CANCEL_SHARING)) {
                if (mClient != null) {
                    mClient.exitApplication();
                }
            }
        }
    };
}
