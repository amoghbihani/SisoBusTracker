package com.batti.nil.sisobustracker.location;

import android.location.Location;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

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
        if (location == null) return;
        ParseQuery<BusLocation> query = ParseQuery.getQuery(BusLocation.class);
        query.whereEqualTo("routeNumber", mRouteNumber);
        query.findInBackground(new FindCallback<BusLocation>() {
            @Override
            public void done(List<BusLocation> list, ParseException e) {
                if (e != null) {
                    onErrorSendingRequest(e.getMessage());
                    return;
                }

                BusLocation busLocation;
                if (list.size() == 1) {
                    busLocation = list.get(0);
                } else if (list.size() == 0) {
                    Log.d(TAG, "No item found, creating one");
                    busLocation = new BusLocation();
                    busLocation.setRouteNumber(mRouteNumber);
                } else {
                    Log.d(TAG, "Multiple items found, deleting all and creating one");
                    for (int i = 0; i < list.size(); ++i) {
                        list.get(i).deleteInBackground();
                    }
                    busLocation = new BusLocation();
                    busLocation.setRouteNumber(mRouteNumber);
                }
                busLocation.setLocation(location);
                busLocation.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            onErrorSendingRequest(e.getMessage());
                        }
                    }
                });
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
