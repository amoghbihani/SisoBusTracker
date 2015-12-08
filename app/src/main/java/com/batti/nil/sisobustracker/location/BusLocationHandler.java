package com.batti.nil.sisobustracker.location;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.VolleyError;
import com.batti.nil.sisobustracker.net.JsonRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BusLocationHandler extends JsonRequestHandler {
    public static final String TAG = "BusLocationHandler";

    private final BusLocationHandlerClient mClient;
    private Location mLastKnownBusLocation;
    private String mGetUrl = "";
    private String mPostUrl = "";

    public BusLocationHandler(Context context, BusLocationHandlerClient client) {
        super(context);
        mClient = client;
        mLastKnownBusLocation = new Location("Bus Location");
    }

    public void requestBusLocation() {
        requestJSONObject(mGetUrl);
    }

    public Location getCurrentLocation() {
        return mLastKnownBusLocation;
    }

    public void sendBusLocation(String routeNumber, Location location) {
        Map<String, String> request = new HashMap<String, String>();
        request.put("RouteNumber", routeNumber);
        request.put("Latitude", String.valueOf(location.getLatitude()));
        request.put("Longitude", String.valueOf(location.getLongitude()));
        sendJsonObject(mPostUrl, new JSONObject(request));
    }

    @Override
    public void onResponseReceived(JSONObject response) {
        if (response == null) return;
        try {
            mLastKnownBusLocation.setLatitude(response.getDouble("Latitude"));
            mLastKnownBusLocation.setLongitude(response.getDouble("Longitude"));
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing exception " + e);
            return;
        }
        mClient.onResponseReceived(mLastKnownBusLocation);
    }

    @Override
    public void onErrorReceivingResponse(VolleyError error) {
        mClient.onErrorReceivingResponse(error);
    }

    @Override
    public void onRequestSent() {
        mClient.onRequestSent();
    }

    @Override
    public void onErrorSendingRequest(VolleyError error) {
        mClient.onErrorSendingRequest(error);
    }
}
