package com.batti.nil.sisobustracker.location;

import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class BusLocation {
    private static final String TAG = "BusLocation";

    private FirebaseData mFirebaseData = new FirebaseData();

    public class FirebaseData {
        private double latitude;
        private double longitude;

        public FirebaseData() {
            // Needed by firebase.
        }

        public FirebaseData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    public void setData(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            mFirebaseData.setLatitude(jsonObject.getDouble("latitude"));
            mFirebaseData.setLongitude(jsonObject.getDouble("longitude"));
        } catch (JSONException ex) {
            Log.d(TAG, "JSON exception: " + ex.getMessage());
        }
    }

    public FirebaseData getFirebaseData() {
        return mFirebaseData;
    }

    public void setLocation(Location location) {
        mFirebaseData.setLatitude(location.getLatitude());
        mFirebaseData.setLongitude(location.getLongitude());
    }

    public Location getLocation() {
        Location location = new Location(TAG);
        location.setLatitude(mFirebaseData.getLatitude());
        location.setLongitude(mFirebaseData.getLongitude());
        return location;
    }
}
