package com.batti.nil.sisobustracker.location;

import android.location.Location;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class BusLocationHandler {
    public static final String TAG = "BusLocationHandler";

    private final BusLocationHandlerClient mClient;
    private final String mRouteNumber;
    private Location mLastKnownBusLocation;
    private Firebase mFirebase;
    private BusLocation mBusLocation;

    private ValueEventListener mBusValueAddListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onDataChanged");
            if (dataSnapshot.getValue() != null) {
                // TODO: directly get object from snapshot.
                mBusLocation.setData(dataSnapshot.getValue().toString());
                onResponseReceived(mBusLocation.getLocation());
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            onErrorReceivingResponse(firebaseError.getMessage());
        }
    };

    public BusLocationHandler(String routeNumber, BusLocationHandlerClient client) {
        mClient = client;
        mRouteNumber = routeNumber;
        mBusLocation = new BusLocation();
        mLastKnownBusLocation = new Location("Bus Location");

        try {
            mFirebase = new Firebase(
                    "https://sisobustracker.firebaseio.com/RouteNumber/" + mRouteNumber);
            mFirebase.addValueEventListener(mBusValueAddListener);
        } catch (IllegalAccessError ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

    public Location getCurrentLocation() {
        return mLastKnownBusLocation;
    }

    public void sendBusLocation(final Location location) {
        mBusLocation.setLocation(location);
        if (mFirebase != null) {
            mFirebase.setValue(mBusLocation.getFirebaseData());
        }
    }

    public void onResponseReceived(Location location) {
        mLastKnownBusLocation = location;
        mClient.onResponseReceived(mLastKnownBusLocation);
    }

    public void onErrorReceivingResponse(String msg) {
        Log.d(TAG, "onErrorReceivingResponse " + msg);
        mClient.onErrorReceivingResponse();
    }
}
