package com.batti.nil.sisobustracker.location;

import android.location.Location;

public abstract class BusLocationHandlerClient {
    public void onResponseReceived(Location location) { }

    public void onErrorReceivingResponse() { }

    public void onErrorSendingRequest() { }
}
