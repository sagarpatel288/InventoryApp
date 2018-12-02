package com.library.android.common.listeners;


public abstract class Callbacks {

    public interface OnFragmentLoad {
        void onFragmentVisible();

        void onFragmentHide();
    }

    public interface NetworkConnectionListener {
        void onConnectionChanged(boolean isConnected);
    }
}
