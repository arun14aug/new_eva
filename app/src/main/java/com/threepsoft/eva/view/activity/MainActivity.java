package com.threepsoft.eva.view.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.threepsoft.eva.R;
import com.threepsoft.eva.background_task.BeaconScanningService;
import com.threepsoft.eva.utils.EvaLog;
import com.threepsoft.eva.utils.ServiceApi;
import com.threepsoft.eva.view.fragments.BeaconListFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener {

    private Activity activity;

    private BeaconManager beaconManager;
    private BeaconRegion region;
    Intent mServiceIntent;
    private FragmentManager fragmentManager;
    private boolean backer = false;
    private TextView tvTitle;
    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = MainActivity.this;

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                mHeaderReceiver, new IntentFilter("Header"));

        fragmentManager = getSupportFragmentManager();
        mToolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.header_text);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {

            }
        });

        region = new BeaconRegion("ranged region", null, Integer.parseInt(ServiceApi.MAJOR), Integer.parseInt(ServiceApi.MINOR));

        BeaconScanningService mSensorService = new BeaconScanningService(MainActivity.this);
        mServiceIntent = new Intent(MainActivity.this, mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }

        displayViews(0);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    /**
     * Header heading update method
     **/
    private final BroadcastReceiver mHeaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            tvTitle.setText(message);
            EvaLog.d("receiver", "Got message: " + message);
        }
    };

    private void displayViews(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new BeaconListFragment();
                title = getString(R.string.title_beacons);
                break;
//            case 1:
//                fragment = new ScheduleFragment();
//                title = getString(R.string.title_schedules);
//                break;
//            case 2:
//                fragment = new RecurringFragment();
//                title = getString(R.string.title_recurring);
//                break;
//            case 3:
//                fragment = new TransactionsFragment();
//                title = getString(R.string.title_transactions);
//                break;
            default:
                break;
        }

        backer = false;
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment, title);
            fragmentTransaction.addToBackStack(title);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }


    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        if (position == 4) {
            startActivity(new Intent(activity, MapsMarkerActivity.class));
        } else
            displayViews(position);
    }

    @Override
    public void onBackPressed() {
        Fragment f = fragmentManager.findFragmentById(R.id.container_body);
        try {
            if (f instanceof BeaconListFragment) {
                if (backer)
                    finish();
                else {
                    backer = true;
                    Toast.makeText(activity, "Press again to exit the app.", Toast.LENGTH_SHORT).show();
                }
            } else {
                super.onBackPressed();
                backer = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}