package com.batti.nil.sisobustracker.location;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

public class BusLocationHandler {
    public static final String TAG = "BusLocationHandler";

    private final BusLocationHandlerClient mClient;
    private final String mRouteNumber;
    private Location mLastKnownBusLocation;

    public BusLocationHandler(String routeNumber, BusLocationHandlerClient client) {
        mClient = client;
        mRouteNumber = routeNumber;
        mLastKnownBusLocation = new Location("Bus Location");
    }

    public void requestBusLocation() {
        ParseQuery<BusLocation> query = ParseQuery.getQuery(BusLocation.class);
        query.whereEqualTo("routeNumber", mRouteNumber);
        query.findInBackground(new FindCallback<BusLocation>() {
            @Override
            public void done(List<BusLocation> list, ParseException e) {
                if (e != null) {
                    onErrorReceivingResponse(e.getMessage());
                    return;
                }
                if (list.size() == 0) {
                    onErrorReceivingResponse("No location present");
                    return;
                } else if (list.size() > 1) {
                    onErrorReceivingResponse("Multiple locations present");
                    return;
                }
                onResponseReceived(list.get(0).getLocation());
            }
        });
    }

    public Location getCurrentLocation() {
        return mLastKnownBusLocation;
    }

    public void sendBusLocation(final Location location) {
        ParseQuery<BusLocation> query = ParseQuery.getQuery(BusLocation.class);
        query.whereEqualTo("routeNumber", mRouteNumber);
        query.findInBackground(new FindCallback<BusLocation>() {
            @Override
            public void done(List<BusLocation> list, ParseException e) {
                if (e != null) {
                    onErrorSendingRequest(e.getMessage());
                    return;
                }
                if (list.size() == 0) {
                    onErrorSendingRequest("No location present");
                    return;
                } else if (list.size() > 1) {
                    onErrorSendingRequest("Multiple locations present");
                    return;
                }
                BusLocation busLocation = list.get(0);
                Log.d(TAG, busLocation.getObjectId());
                busLocation.setLocation(location);
                busLocation.saveInBackground();
            }
        });
    }

    public void onResponseReceived(Location location) {
        mLastKnownBusLocation = location;
        mClient.onResponseReceived(mLastKnownBusLocation);
    }

    public void onErrorReceivingResponse(String msg) {
        Log.d(TAG, "onErrorReceivingResponse " + msg);
        mClient.onErrorReceivingResponse();
    }

    public void onErrorSendingRequest(String msg) {
        Log.d(TAG, "onErrorSendingResponse " + msg);
        mClient.onErrorSendingRequest();
    }
}
