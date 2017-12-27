package com.threepsoft.eva.view.activity;

/*
 * Created by Ravi on 29/07/15.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.threepsoft.eva.R;
import com.threepsoft.eva.model.NavDrawerItem;
import com.threepsoft.eva.utils.CircularNetworkImageView;
import com.threepsoft.eva.utils.CustomVolleyRequestQueue;
import com.threepsoft.eva.utils.EvaLog;
import com.threepsoft.eva.utils.Preferences;
import com.threepsoft.eva.utils.ServiceApi;
import com.threepsoft.eva.utils.Utils;
import com.threepsoft.eva.view.adapter.NavigationDrawerAdapter;

import java.util.ArrayList;
import java.util.List;


public class FragmentDrawer extends Fragment {

    private static String TAG = FragmentDrawer.class.getSimpleName();

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private View containerView;
    private static String[] titles = null;
    private FragmentDrawerListener drawerListener;
    private TextView txt_party;

    public FragmentDrawer() {

    }

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    public static List<NavDrawerItem> getData() {
        List<NavDrawerItem> data = new ArrayList<>();


        // preparing navigation drawer items
        for (String title : titles) {
            NavDrawerItem navItem = new NavDrawerItem();
            navItem.setTitle(title);
            data.add(navItem);
        }
        return data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // drawer labels
        titles = getActivity().getResources().getStringArray(R.array.nav_drawer_labels);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating view layout
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mHeaderReceiver, new IntentFilter("UserName"));
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        RecyclerView recyclerView = layout.findViewById(R.id.drawerList);

        RelativeLayout nav_header_container = (RelativeLayout) layout.findViewById(R.id.nav_header_container);

        TextView txt_mobile = layout.findViewById(R.id.txt_mobile);
        txt_party = layout.findViewById(R.id.txt_party);
        txt_mobile.setText(Preferences.readString(getActivity(), Preferences.MOBILE_NUMBER, ""));

        if (Preferences.readString(getActivity(), Preferences.USER_NAME, "").length() > 0)
            txt_party.setText(Preferences.readString(getActivity(), Preferences.USER_NAME, ""));

        CircularNetworkImageView imageView = layout.findViewById(R.id.icon_user);

        String image = Preferences.readString(getActivity(), Preferences.USER_IMAGE, "");
        if (!Utils.isEmptyString(image)) {
            Log.e(" URL : ", "" + image);
            ImageLoader mImageLoader;
            mImageLoader = new CustomVolleyRequestQueue(getActivity())
                    .getImageLoader();
////        if (!imageUrl.equalsIgnoreCase("null"))
            mImageLoader.get(image, ImageLoader.getImageListener(imageView,
                /*R.drawable.logo*/ 0, /*R.drawable.logo*/ 0));
            imageView.setImageUrl(image, mImageLoader);
            imageView.setTag(image);

        } else
            imageView.setImageResource(R.mipmap.avatar);


        nav_header_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                getActivity().startActivity(intent);
            }
        });

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(getActivity(), getData());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                drawerListener.onDrawerItemSelected(view, position);
                mDrawerLayout.closeDrawer(containerView);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        return layout;
    }


    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (Preferences.readString(getActivity(), Preferences.USER_NAME, "").length() > 0)
                    txt_party.setText(Preferences.readString(getActivity(), Preferences.USER_NAME, ""));
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (Preferences.readString(getActivity(), Preferences.USER_NAME, "").length() > 0)
                    txt_party.setText(Preferences.readString(getActivity(), Preferences.USER_NAME, ""));
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

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

    /**
     * Header heading update method
     **/
    private final BroadcastReceiver mHeaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            txt_party.setText(message);
            EvaLog.d("receiver", "Got message: " + message);
        }
    };


    interface FragmentDrawerListener {
        void onDrawerItemSelected(View view, int position);
    }
}
