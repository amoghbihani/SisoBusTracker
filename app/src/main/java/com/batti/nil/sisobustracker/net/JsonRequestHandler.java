package com.batti.nil.sisobustracker.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class JsonRequestHandler extends RequestHandler {
    public static final String TAG = "JsonRequestHandler";

    public JsonRequestHandler(Context context) {
        super(context);
    }

    public void requestJSONObject(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onResponseReceived(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onErrorReceivingResponse(error);
                    }
                });
        addToRequestQueue(request);
    }

    public void sendJsonObject(String url, JSONObject object) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        onRequestSent();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onErrorSendingRequest(error);
                    }
                });
        addToRequestQueue(request);
    }

    public void onResponseReceived(JSONObject object) {
        // To be implemented by derived classes.
    }

    public void onErrorReceivingResponse(VolleyError error) {
        Log.d(TAG, error.getMessage());
        // To be implemented by derived classes.
    }

    public void onRequestSent() {
        // To be implemented by derived classes.
    }

    public void onErrorSendingRequest(VolleyError error) {
        Log.d(TAG, error.getMessage());
        // To be implemented by derived classes.
    }
}
