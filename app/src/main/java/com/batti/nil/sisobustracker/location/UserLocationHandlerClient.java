package com.batti.nil.sisobustracker.location;

import android.location.Location;
import android.os.Bundle;

public abstract class UserLocationHandlerClient {
    public void onLocationChanged(Location location) { }

    public void onProviderDisabled(String provider) { }

    public void onProviderEnabled(String provider) { }

    public void onStatusChanged(String provider, int status, Bundle extras) { }

    public void exitApplication() { }
}
