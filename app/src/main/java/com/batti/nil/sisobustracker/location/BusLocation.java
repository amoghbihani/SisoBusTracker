package com.batti.nil.sisobustracker.location;

import android.location.Location;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("BusLocation")
public class BusLocation extends ParseObject{
    private static final String TAG = "BusLocation";
    private static final String ROUTE_NUMBER = "routeNumber";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    public void setRouteNumber(String routeNumber) {
        put(ROUTE_NUMBER, routeNumber);
    }

    public void setLocation(Location location) {
        put(LATITUDE, location.getLatitude());
        put(LONGITUDE, location.getLongitude());
    }

    public String getRouteNumber() {
        return getString(ROUTE_NUMBER);
    }

    public Location getLocation() {
        Location location = new Location(TAG);
        location.setLatitude(Double.parseDouble(getString(LATITUDE)));
        location.setLongitude(Double.parseDouble(getString(LONGITUDE)));
        return location;
    }
}
