package co.smartreceipts.android.ocr.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;

public class OcrConfigurationFragment extends Fragment {

    @Inject
    OcrConfigurationInteractor interactor;

    private OcrConfigurationPresenter presenter;

    public static OcrConfigurationFragment newInstance() {
        return new OcrConfigurationFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.debug(this, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        interactor.routeToProperLocation(savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.simple_recycler_view, container, false);
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(android.R.id.list);

        final View headerView = inflater.inflate(R.layout.ocr_configuration_fragment, null);
        this.presenter = new OcrConfigurationPresenter(interactor, headerView, recyclerView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return interactor.navigateBack();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        Logger.debug(this, "onResume");
        super.onResume();
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.ocr_configuration_title);
            actionBar.setSubtitle("");
        }
        presenter.onResume();
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");
        presenter.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        presenter.onDestroyView();
        super.onDestroyView();
    }
}
