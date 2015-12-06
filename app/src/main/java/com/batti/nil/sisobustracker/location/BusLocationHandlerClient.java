package com.batti.nil.sisobustracker.location;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public abstract class BusLocationHandlerClient {
    public void onResponseReceived(JSONObject object) { }

    public void onErrorReceivingResponse(VolleyError error) { }

    public void onRequestSent() { }

    public void onErrorSendingRequest(VolleyError error) { }
}
