package com.batti.nil.sisobustracker.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ResponseGetter {
    public static final String TAG = "ResponseGetter";

    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    private Context mContext;

    public ResponseGetter(Context context) {
       mContext = context;
    }

    public RequestQueue getRequestQueue() {
        if (mContext == null) {
            Log.d(TAG, "Context is null");
            return null;
        }

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(getRequestQueue(), new LruBitmapCache());
        }
        return mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequests() {
        if (mRequestQueue == null) return;
        mRequestQueue.cancelAll(TAG);
    }
}
