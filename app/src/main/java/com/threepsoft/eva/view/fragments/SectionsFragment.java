package com.threepsoft.eva.view.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.threepsoft.eva.R;
import com.threepsoft.eva.model.BeaconValues;
import com.threepsoft.eva.model.ModelManager;
import com.threepsoft.eva.model.Sections;
import com.threepsoft.eva.view.adapter.SectionAdapter;

import java.util.ArrayList;

/*
 * Created by arunks on 26/12/17.
 */

public class SectionsFragment extends Fragment {

    private Activity activity;
    private String id = "";
    private RecyclerView recyclerView;
    private ArrayList<Sections> sectionsArrayList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = super.getActivity();
        String name = "";
        try {
            if (getArguments() != null) {
                Bundle bundle = getArguments();
                id = bundle.getString("spot_id");
                name = bundle.getString("name");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            id = "";
            name = "";
        }

        // set header
        Intent intent = new Intent("Header");
        intent.putExtra("message", name);

        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        View rootView = inflater.inflate(R.layout.activity_sections, container, false);

//        TextView txt_header = findViewById(R.id.txt_header);
//        txt_header.setText(name);
//        ImageView imageView = findViewById(R.id.ic_back);
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        recyclerView = rootView.findViewById(R.id.section_list);
        // Lookup the swipe container view
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(activity, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
//                Intent intent = new Intent(activity, CategoryActivity.class);
//                intent.putExtra("spot_id", id);
//                intent.putExtra("section_id", sectionsArrayList.get(position).getSectionID());
//                intent.putExtra("name", sectionsArrayList.get(position).getSectionName());
//                startActivity(intent);
                Fragment fragment = new CategoryFragment();
                FragmentManager fragmentManager = ((FragmentActivity) activity)
                        .getSupportFragmentManager();
                String title = "CategoryFragment";
                Bundle bundle = new Bundle();
                bundle.putString("spot_id", id);
                bundle.putString("section_id", sectionsArrayList.get(position).getSectionID());
                bundle.putString("name", sectionsArrayList.get(position).getSectionName());
                fragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_body, fragment, title);
                fragmentTransaction.addToBackStack(title);
                fragmentTransaction.commit();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        getData();
        return rootView;
    }

    private void getData() {
        ArrayList<BeaconValues> beaconValuesArrayList = ModelManager.getInstance().getBeaconsManager().getNames(activity, true, "");
        for (int i = 0; i < beaconValuesArrayList.size(); i++) {
            if (beaconValuesArrayList.get(i).getSpotId().equalsIgnoreCase(id)) {
                sectionsArrayList = beaconValuesArrayList.get(i).getSectionsArrayList();
                setAdapter();
                break;
            }
        }
    }

    private void setAdapter() {
        SectionAdapter sectionAdapter = new SectionAdapter(activity, sectionsArrayList);
        recyclerView.setAdapter(sectionAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        sectionAdapter.notifyDataSetChanged();
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

}
