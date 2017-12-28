package com.threepsoft.eva.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.threepsoft.eva.R;
import com.threepsoft.eva.model.ModelManager;
import com.threepsoft.eva.model.User;
import com.threepsoft.eva.utils.EvaLog;
import com.threepsoft.eva.utils.Preferences;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

/***
 * Created by arunks on 27/12/17.
 */

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";
    public User user;
    private EditText email;
    private EditText password;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgressDialog;
    //Add YOUR Firebase Reference URL instead of the following URL
    Firebase mRef = new Firebase("https://eeva-fabc2.firebaseio.com/users/");

    //FaceBook callbackManager
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private Button facebookSignIn;
    private TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure Twitter SDK
        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();

        Twitter.initialize(twitterConfig);

        // Inflate layout (must be done after Twitter is configured)
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

//        FirebaseUser mUser = mAuth.getCurrentUser();
//        if (mUser != null) {
//            // User is signed in
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            String uid = mAuth.getCurrentUser().getUid();
//            String image = mAuth.getCurrentUser().getPhotoUrl().toString();
//            intent.putExtra("user_id", uid);
//            if (!TextUtils.isEmpty(image)) {
//                intent.putExtra("profile_picture", image);
//            }
//            startActivity(intent);
//            finish();
//            EvaLog.d(TAG, "onAuthStateChanged:signed_in:" + mUser.getUid());
//        }

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    // User is signed in
                    EvaLog.d(TAG, "onAuthStateChanged:signed_in:" + mUser.getUid());
                } else {
                    // User is signed out
                    EvaLog.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        //FaceBook
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                EvaLog.d(TAG, "facebook:onSuccess:" + loginResult);
                signInWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                EvaLog.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                EvaLog.d(TAG, "facebook:onError" + error);
            }
        });
        //

        twitterLoginButton = findViewById(R.id.button_twitter_login);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.e(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e(TAG, "twitterLogin:failure", exception);
                updateUI(null);
            }
        });
        facebookSignIn = findViewById(R.id.button_facebook_sign_in);
        setData();
    }

    private void setData() {
        Profile profile = Profile.getCurrentProfile().getCurrentProfile();
        if (profile != null) {
            // user has logged in
            facebookSignIn.setText(getString(R.string.facebook_logout));
        } else {
            // user has not logged in
            facebookSignIn.setText(getString(R.string.facebook_login));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        email = findViewById(R.id.edit_text_email_id);
        password = findViewById(R.id.edit_text_password);
        mAuth.addAuthStateListener(mAuthListener);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    //FaceBook
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the Twitter login button.
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        setData();
    }

    //
    private void handleTwitterSession(TwitterSession session) {
        Log.e(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            loginSuccessfull(task);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {

            findViewById(R.id.button_twitter_login).setVisibility(View.GONE);
            findViewById(R.id.button_twitter_signout).setVisibility(View.VISIBLE);
        } else {

            findViewById(R.id.button_twitter_login).setVisibility(View.VISIBLE);
            findViewById(R.id.button_twitter_signout).setVisibility(View.GONE);
        }
    }


    protected void setUpUser() {
        user = new User();
        user.setEmail(email.getText().toString());
        user.setPassword(password.getText().toString());
    }

    public void onSignUpClicked(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void onLoginClicked(View view) {
        setUpUser();
        signIn(email.getText().toString(), password.getText().toString());
    }

    private void signIn(String email, String password) {
        EvaLog.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        EvaLog.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            EvaLog.e(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            String uid = mAuth.getCurrentUser().getUid();
                            intent.putExtra("user_id", uid);
                            startActivity(intent);
                            finish();
                        }

                        hideProgressDialog();
                    }
                });
        //
    }

    private boolean validateForm() {
        boolean valid = true;

        String userEmail = email.getText().toString();
        if (TextUtils.isEmpty(userEmail)) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        String userPassword = password.getText().toString();
        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }


    private void signInWithFacebook(AccessToken token) {
        EvaLog.d(TAG, "signInWithFacebook:" + token);

        showProgressDialog();


        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        EvaLog.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            EvaLog.e(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            loginSuccessfull(task);
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void loginSuccessfull(Task<AuthResult> task) {
        String uid = task.getResult().getUser().getUid();
        String name = task.getResult().getUser().getDisplayName();
        String email = task.getResult().getUser().getEmail();
        String image = task.getResult().getUser().getPhotoUrl().toString();

        EvaLog.e("Details : ", "UID : " + uid + "\nName: " + name + "\nEmail : " + email + "\nimage : " + image);

        //Create a new User and Save it in Firebase database
        User user = new User(uid, name, null, email, null, image);

        ModelManager.getInstance().getAuthenticationManager().setUser(user);
        Preferences.writeString(LoginActivity.this, Preferences.USER_NAME, name);
        Preferences.writeString(LoginActivity.this, Preferences.EMAIL, email);
        Preferences.writeString(LoginActivity.this, Preferences.USER_IMAGE, image);
        Preferences.writeString(LoginActivity.this, Preferences.UID, uid);

        mRef.child(uid).setValue(user);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user_id", uid);
        intent.putExtra("profile_picture", image);
        startActivity(intent);
        finish();

    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.please_wait));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    public void onFacebookLogInClicked(View view) {
        loginButton.performClick();
    }

    public void onTwitterLogoutClicked(View view) {
        signOut();
    }

    private void signOut() {
        mAuth.signOut();
        TwitterCore.getInstance().getSessionManager().clearActiveSession();

        updateUI(null);
    }
}
