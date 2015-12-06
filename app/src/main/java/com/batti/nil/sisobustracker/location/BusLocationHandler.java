package com.batti.nil.sisobustracker.location;

import android.content.Context;
import android.location.Location;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.batti.nil.sisobustracker.net.JsonRequestHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BusLocationHandler extends JsonRequestHandler {
    public static final String TAG = "BusLocationHandler";

    private final BusLocationHandlerClient mClient;
    private String mGetUrl = "";
    private String mPostUrl = "";

    public BusLocationHandler(Context context, BusLocationHandlerClient client) {
        super(context);
        mClient = client;
    }

    public void requestBusLocation() {
        requestJSONObject(mGetUrl);
    }

    public void sendBusLocation(int routeNumber, Location location) {
        Map<String, String> request = new HashMap<String, String>();
        request.put("RouteNumber", String.valueOf(routeNumber));
        request.put("Latitude", String.valueOf(location.getLatitude()));
        request.put("Longitude", String.valueOf(location.getLongitude()));
        sendJsonObject(mPostUrl, new JSONObject(request));
    }

    @Override
    public void onResponseReceived(JSONObject response) {
        mClient.onResponseReceived(response);
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
