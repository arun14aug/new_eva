package com.threepsoft.eva.view.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;

import com.threepsoft.eva.R;
import com.threepsoft.eva.model.Category;
import com.threepsoft.eva.model.ModelManager;
import com.threepsoft.eva.model.SearchContent;
import com.threepsoft.eva.utils.EvaLog;
import com.threepsoft.eva.utils.ServiceApi;
import com.threepsoft.eva.utils.Utils;
import com.threepsoft.eva.view.adapter.BeaconExpandableAdapter;
import com.threepsoft.eva.view.adapter.SearchAdapter;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/*
 * Created by arunks on 26/12/17.
 */

public class CategoryFragment extends Fragment implements SearchAdapter.ContactsAdapterListener {

    private static final String TAG = CategoryFragment.class.getName();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private Activity activity;
    private ArrayList<Category> categoryArrayList;
    private String id = "", section_id = "";
    private Dialog dialog;
    private SearchAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = super.getActivity();
        View rootView = inflater.inflate(R.layout.activity_category, container, false);

        String name = "";
        try {
            if (getArguments() != null) {
                Bundle bundle = getArguments();
                id = bundle.getString("spot_id");
                name = bundle.getString("name");
                section_id = bundle.getString("section_id");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            id = "";
            section_id = "";
            name = "";
        }

        // set header
        Intent intent = new Intent("Header");
        intent.putExtra("message", name);

        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

        ImageView searchView = activity.findViewById(R.id.ic_search);
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView();
            }
        });

        recyclerView = rootView.findViewById(R.id.category_list);
        // Lookup the swipe container view
        swipeContainer = rootView.findViewById(R.id.swipeContainer);

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

        getData();
        return rootView;
    }

    private void getData() {
        Utils.showLoading(activity);
        String url;
        if (!Utils.isEmptyString(section_id))
            url = ServiceApi.GET_CATEGORIES + "Spotid=" + id + "&sectionid=" + section_id;
        else
            url = ServiceApi.GET_CATEGORIES + "Spotid=" + id;
        ModelManager.getInstance().getBeaconsManager().getCategories(activity, true, url);
    }

    private void setAdapter() {
        BeaconExpandableAdapter categoryAdapter = new BeaconExpandableAdapter(activity, categoryArrayList);
        recyclerView.setAdapter(categoryAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        categoryAdapter.notifyDataSetChanged();
        swipeContainer.setRefreshing(false);
    }

    private void searchView() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_search);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.setCancelable(true);
        dialog.show();// I was told to call show first I am not sure if this it to cause layout to happen so that we can override width?
        dialog.getWindow().setAttributes(lWindowParams);

        ArrayList<SearchContent> searchContents = ModelManager.getInstance().getBeaconsManager().getSearchContentArrayList();

        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = dialog.findViewById(R.id.edt_search);
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(activity.getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconifiedByDefault(false);
        searchView.setFocusable(true);
        searchView.requestFocus();

        ImageView ic_close = dialog.findViewById(R.id.ic_close);
        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        final RecyclerView recyclerView = dialog.findViewById(R.id.search_list);

        mAdapter = new SearchAdapter(activity, searchContents, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                if (!Utils.isEmptyString(query)) {
                    mAdapter.getFilter().filter(query);
                    recyclerView.setAdapter(mAdapter);
                } else
                    recyclerView.setAdapter(null);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if (!Utils.isEmptyString(query)) {
                    mAdapter.getFilter().filter(query);
                    recyclerView.setAdapter(mAdapter);
                } else
                    recyclerView.setAdapter(null);
                return false;
            }
        });
    }

    @Override
    public void onContactSelected(SearchContent contact) {
        if (dialog != null)
            if (dialog.isShowing())
                dialog.dismiss();

        Fragment fragment = new SpotsFragment();

        Bundle bundle = new Bundle();
        bundle.putString("category_id", contact.getCategoryID());
        if (!Utils.isEmptyString(section_id))
            bundle.putString("section_id", section_id);
        if (!Utils.isEmptyString(contact.getSubCategoryID()))
            bundle.putString("subCategory_id", contact.getSubCategoryID());
        bundle.putString("spot_id", contact.getSpotId());
        bundle.putString("name", contact.getName());
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = ((FragmentActivity) activity)
                .getSupportFragmentManager();
        String title = fragment.getClass().getSimpleName();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_body, fragment, title);
        fragmentTransaction.addToBackStack(title);
        fragmentTransaction.commit();
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
        if (message.equalsIgnoreCase("GetCategory True")) {
            Utils.dismissLoading();
            categoryArrayList = ModelManager.getInstance().getBeaconsManager().getCategories(activity, false, "");
            if (categoryArrayList != null)
                if (categoryArrayList.size() > 0)
                    setAdapter();
                else
                    Utils.showMessage(activity, "No record found");
            else
                Utils.showMessage(activity, "No record found");
            EvaLog.e(TAG, "GetCategory True");
        } else if (message.equalsIgnoreCase("GetCategory False")) {
            Utils.showMessage(activity, getString(R.string.error_message));
            EvaLog.e(TAG, "GetCategory False");
            Utils.dismissLoading();
        }

    }
}
