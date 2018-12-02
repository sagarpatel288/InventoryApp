package com.example.android.inventoryapp.base;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;

import com.example.android.inventoryapp.R;
import com.library.android.common.databinding.BaseViewStubLayoutBinding;
import com.library.android.common.listeners.Callbacks;
import com.library.android.common.utils.NetworkUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.util.Log.d;
import static com.library.android.common.appconstants.AppConstants.TAG;

/**
 * 1. Even if app module is accessing butterKnife from library module through api keyword,
 * it is required for app module to have butter knife annotation processor dependency
 * (in app level gradle), otherwise we get null views.
 * 2. Library may be not capable enough to access resources from app module at runtime to set
 * layout for viewstub. Hence, using base package here in app module instead of in library module.
 */
public abstract class BaseActivity extends AppCompatActivity implements Callbacks.NetworkConnectionListener {

    BaseViewStubLayoutBinding binding; //Uncomment to use native binding instead of butter knife
    @BindView(com.library.android.common.R.id.progressbar)
    ProgressBar progressbar;
    @BindView(com.library.android.common.R.id.view_stub)
    ViewStub viewStub;
    private boolean hasStubInflated;
    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;
    private NetworkUtils networkUtils;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Normal method to inflate the layout
        setContentView(com.library.android.common.R.layout.base_view_stub_layout);
        ButterKnife.bind(this);
        //Data binding to inflate the layout and binding views at the same time
        binding = BaseViewStubLayoutBinding.inflate(getLayoutInflater());
        if (binding == null) {
            binding = DataBindingUtil.setContentView(this, R.layout.base_view_stub_layout);
        }
        if (binding != null) {
            d(TAG, "BaseActivity: onCreate: binding is not null");

            if (binding.viewStub != null) {
                d(TAG, "BaseActivity: onCreate: binding got views");
            }
        }
        /*For fragments, listview or recyclerview adapter, we can use below method:
        binding = DataBindingUtil.setContentView(this, R.layout.base_activity_layout);*/

        // FIXME: 11/24/2018 sagar: I couldn't understand why I could not use data binding here!
        viewStub.setLayoutResource(getLayoutId());
        viewStub.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {
                //Abstract method
                onViewStubInflated(inflated, savedInstanceState);
                ButterKnife.bind(this, inflated);
                initNetworkManager();
                initControllers();
                handleViews();
                setListeners();
                // Note: 11/4/2018 by sagar  Manually check first time
                onGetConnectionState(NetworkUtils.isNetworkAvailable(getApplicationContext()));
                //Normal method to hide progress bar
                onViewStubInflated();
            }
        });

        if (!hasStubInflated) {
            viewStub.inflate();
        }
    }

    protected abstract int getLayoutId();

    //Bind the inflatedView for data binding
    protected abstract void onViewStubInflated(View inflatedView, Bundle savedInstancSate);

    private void initNetworkManager() {
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerNetworkReceiver(mConnectivityManager);
        } else {
            networkUtils = new NetworkUtils(this);
            // Note: 11/24/2018 by sagar  https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
            // Note: 11/24/2018 by sagar  To support api lower than N (24)
            registerReceiver(networkUtils, new IntentFilter(CONNECTIVITY_ACTION));
        }
    }

    protected abstract void initControllers();

    protected abstract void handleViews();

    protected abstract void setListeners();

    protected abstract void onGetConnectionState(boolean isConnected);

    private void onViewStubInflated() {
        hasStubInflated = true;
        hideProgressbar();
    }

    private void registerNetworkReceiver(ConnectivityManager mConnectivityManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mNetworkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    // Note: 11/4/2018 by sagar  Notify that yes, we are connected to internet
                    onGetConnectionState(true);
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    // Note: 11/4/2018 by sagar  Notify that we have lost our internet connection
                    onGetConnectionState(false);
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                }

                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                }

                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    super.onLinkPropertiesChanged(network, linkProperties);
                }
            };
        }

        if (mConnectivityManager != null) {
            // Note: 11/4/2018 by sagar  To support connectivity service for API >= N
            // Note: 11/4/2018 by sagar  https://developer.android.com/training/monitoring-device-state/connectivity-monitoring
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
            }
            // Note: 11/4/2018 by sagar  No else part because for API < N, we are doing connectivity service by NetworkUtils class
        }
    }

    private void hideProgressbar() {
        if (progressbar != null) {
            progressbar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hasStubInflated = false;
        if (networkUtils != null) {
            unregisterReceiver(networkUtils);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
        }
    }

    @Override
    public void onConnectionChanged(boolean isConnected) {
        onGetConnectionState(isConnected);
    }
}
