package com.threepsoft.eva.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;

import com.threepsoft.eva.R;


public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/*
         * This must be called before any other AppRedeemSDK calls are made.
		 */

		/*
         * Passing "0" to AppRedeemSDK.registerEngagement(int) designates that
		 * the "install" engagement has occurred. This call can be made every
		 * time the app is opened, however, it will only register an install the
		 * first time it is ever called.
		 */

// Add code to print out the key hash
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.threepsoft.eva",
//                    PackageManager.GET_SIGNATURES);
//            for (android.content.pm.Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

        // Key Hash   ZvsFcwOq5kFYVDzJ0+buOWrutFM=

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        /* Making this activity, full screen */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* Sets a layout for this activity */
        setContentView(R.layout.activity_splash);


        /*
         * Creates a count down timer, which will be expired after 3000
         * milliseconds
         */
        new CountDownTimer(3000, 1000) {

            /** This method will be invoked on finishing or expiring the timer */
            @Override
            public void onFinish() {

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                /* Close this activity screen */
                finish();


            }

            /**
             * This method will be invoked in every 1000 milli seconds until
             * this timer is expired.Because we specified 1000 as tick time
             * while creating this CountDownTimer
             */
            @Override
            public void onTick(long millisUntilFinished) {

            }
        }.start();

    }

}