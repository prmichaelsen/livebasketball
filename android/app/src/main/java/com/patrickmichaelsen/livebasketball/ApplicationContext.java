package com.patrickmichaelsen.livebasketball;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Patrick on 8/21/2017.
 */


public class ApplicationContext {
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    private static ApplicationContext mInstance;

    private ApplicationContext(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized ApplicationContext getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ApplicationContext(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
