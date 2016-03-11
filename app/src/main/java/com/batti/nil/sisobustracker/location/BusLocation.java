package com.batti.nil.sisobustracker.location;

import android.location.Location;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

@ParseClassName("BusLocationTest")
public class BusLocation extends ParseObject{
    private static final String TAG = "BusLocation";
    private static final String ROUTE_NUMBER = "routeNumber";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String UPDATED_AT = "updatedAt";

    public void setRouteNumber(String routeNumber) {
        put(ROUTE_NUMBER, routeNumber);
    }

    public String getRouteNumber() {
        return getString(ROUTE_NUMBER);
    }

    public void setLocation(Location location) {
        put(LATITUDE, String.valueOf(location.getLatitude()));
        put(LONGITUDE, String.valueOf(location.getLongitude()));
    }

    public Location getLocation() {
        Location location = new Location(TAG);
        location.setLatitude(Double.parseDouble(getString(LATITUDE)));
        location.setLongitude(Double.parseDouble(getString(LONGITUDE)));
        return location;
    }

    public Date getLastUpdatedDate() {
        return getDate(UPDATED_AT);
    }
}
