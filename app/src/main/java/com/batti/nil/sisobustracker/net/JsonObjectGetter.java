package com.batti.nil.sisobustracker.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class JsonObjectGetter extends ResponseGetter {
    public static final String TAG = "JsonObjectGetter";

    public JsonObjectGetter(Context context) {
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
                        Log.d(TAG, error.getMessage());
                    }
        });
        addToRequestQueue(request);
    }

    public void onResponseReceived(JSONObject object) {
        // To be implemented by derived classes.
    }
}
