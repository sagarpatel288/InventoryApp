package com.example.android.inventoryapp.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;

import com.library.android.common.R2;
import com.library.android.common.listeners.Callbacks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment implements Callbacks.OnFragmentLoad {

    @BindView(R2.id.progressbar)
    ProgressBar progressbar;
    @BindView(R2.id.view_stub)
    ViewStub viewStub;
    private Unbinder unbinder;
    private boolean hasViewStubInflated;
    private Bundle mSavedInstanceState;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && viewStub != null && !hasViewStubInflated) {
            inflateViewStub();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.library.android.common.R.layout.base_view_stub_layout, container, false);
        unbinder = ButterKnife.bind(this, view);
        viewStub.setLayoutResource(getLayoutId());
        mSavedInstanceState = savedInstanceState;

        if (getUserVisibleHint() && !hasViewStubInflated) {
            inflateViewStub();
        }
        return view;
    }

    abstract int getLayoutId();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void inflateViewStub() {
        if (viewStub != null && !hasViewStubInflated) {
            View inflatedView = viewStub.inflate();
            onViewStubInflated(inflatedView, mSavedInstanceState);
            onViewStubInflated();
        }
        initControllers();
        handleViews();
        setListeners();
        restoreValues(mSavedInstanceState);
    }

    //Bind the inflatedView for data binding
    abstract void onViewStubInflated(View inflatedView, Bundle savedInstanceState);

    private void onViewStubInflated() {
        hasViewStubInflated = true;
        hideProgressbar();
    }

    abstract void initControllers();

    abstract void handleViews();

    abstract void setListeners();

    abstract void restoreValues(Bundle savedInstanceState);

    private void hideProgressbar() {
        if (progressbar != null) {
            progressbar.setVisibility(View.GONE);
        }
    }
}
