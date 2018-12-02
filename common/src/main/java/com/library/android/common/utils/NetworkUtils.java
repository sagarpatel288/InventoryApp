package com.library.android.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.library.android.common.listeners.Callbacks;

/*final class because we don't want this class to be extended by any other class*/
public final class NetworkUtils extends BroadcastReceiver {

    private final Callbacks.NetworkConnectionListener mNetworkConnectionListener;

    /*Public constructor because for each activity to register this broadcast*/
    public NetworkUtils(Callbacks.NetworkConnectionListener mNetworkConnectionListener) {
        this.mNetworkConnectionListener = mNetworkConnectionListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mNetworkConnectionListener.onConnectionChanged(isNetworkAvailable(context));
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
