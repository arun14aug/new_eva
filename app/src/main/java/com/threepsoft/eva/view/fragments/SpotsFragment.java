package com.threepsoft.eva.view.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.threepsoft.eva.R;
import com.threepsoft.eva.model.ModelManager;
import com.threepsoft.eva.model.Spots;
import com.threepsoft.eva.utils.EvaLog;
import com.threepsoft.eva.utils.ServiceApi;
import com.threepsoft.eva.utils.Utils;
import com.threepsoft.eva.view.adapter.SpotsAdapter;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static android.Manifest.permission.CALL_PHONE;

/**
 * Created by arunks on 26/12/17.
 */

public class SpotsFragment extends Fragment {

    private static final String TAG = SpotsFragment.class.getName();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private Activity activity;
    private ArrayList<Spots> spotsArrayList;
    private String spot_id = "", category_id = "", subCategory_id = "";
    private final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 0x01;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = super.getActivity();
        String name = "";
        try {
            if (getArguments() != null) {
                Bundle bundle = getArguments();
                spot_id = bundle.getString("spot_id");
                category_id = bundle.getString("category_id");
                subCategory_id = bundle.getString("subCategory_id");
                name = bundle.getString("name");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // set header
        Intent intent = new Intent("Header");
        intent.putExtra("message", name);

        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        View rootView = inflater.inflate(R.layout.fragment_beacon_list, container, false);
        recyclerView = rootView.findViewById(R.id.beacon_list);
        // Lookup the swipe container view
        swipeContainer = rootView.findViewById(R.id.swipeContainer);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(activity, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                getData();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (!checkPermission())
            requestPermission();
        else
            getData();

        return rootView;
    }
    private void getData() {
        Utils.showLoading(activity);
        String url;
        if (Utils.isEmptyString(subCategory_id))
            url = ServiceApi.GET_SHOPS + "spotid=" + spot_id + "&categoryid=" + category_id;
        else
            url = ServiceApi.GET_SHOPS + "spotid=" + spot_id + "&categoryid=" + category_id + "&subcategoryid=" + subCategory_id;
        ModelManager.getInstance().getBeaconsManager().getSpots(activity, true, url);
    }

    private void setAdapter() {
        SpotsAdapter categoryAdapter = new SpotsAdapter(activity, spotsArrayList);
        recyclerView.setAdapter(categoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        categoryAdapter.notifyDataSetChanged();
        swipeContainer.setRefreshing(false);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(activity, CALL_PHONE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        String[] permissions = new String[]{CALL_PHONE};
        ActivityCompat.requestPermissions(activity, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    final String[] perm = new String[]{CALL_PHONE};
                    boolean phoneState = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!(phoneState)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CALL_PHONE)) {
                                showMessageOKCancel(getString(R.string.permission_alert),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(perm,
                                                            ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            } else {
                                getData();
                            }
                        }
                    } else {
                        getData();
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    private class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void onEventMainThread(String message) {
        if (message.equalsIgnoreCase("GetSpots True")) {
            Utils.dismissLoading();
            spotsArrayList = ModelManager.getInstance().getBeaconsManager().getSpots(activity, false, "");
            if (spotsArrayList != null)
                if (spotsArrayList.size() > 0)
                    setAdapter();
                else
                    Utils.showMessage(activity, "No record found");
            else
                Utils.showMessage(activity, "No record found");
            EvaLog.e(TAG, "GetSpots True");
        } else if (message.equalsIgnoreCase("GetShops False")) {
            Utils.showMessage(activity, getString(R.string.error_message));
            EvaLog.e(TAG, "GetSpots False");
            Utils.dismissLoading();
        }

    }
}
