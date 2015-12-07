package com.batti.nil.sisobustracker.location;

import android.location.Location;

import com.android.volley.VolleyError;

public abstract class BusLocationHandlerClient {
    public void onResponseReceived(Location location) { }

    public void onErrorReceivingResponse(VolleyError error) { }

    public void onRequestSent() { }

    public void onErrorSendingRequest(VolleyError error) { }
}
