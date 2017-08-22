package com.zerobin.www.beacon_client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beaconpop.pyeongchang.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    FirebaseUser user;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private TextView mDetailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Views
        mStatusTextView = (TextView) findViewById(R.id.status);
        mDetailTextView = (TextView) findViewById(R.id.detail);

        // Button listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]



        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                //로그인된 User정보가 있을 경우
                if (user != null) {
                    // User is signed in
                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

//                    //아래 SharedPreference코드는 정보를 db에 저장하여 다른 화면에서도 해당하는 키 값으로 불러올 수 있게 하기위해서 사용될 수 있다.
//                    //다른 화면에서 Firebase Database의 user객체에 접근하기 위하여 SharedPreferences에 userUid를 저장한다.
//                    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("userUid", user.getUid());
//                    editor.apply();
//
////                    //User정보를 파이어베이스 서버에 저장하고 메인 화면으로 넘기는 부분
////                    //로그인에 성공한다면! -----------------------------------여기부터 내가 추가한 코드 *************************************************
//                    //로그아웃을 눌러야지 디비에 정보가 저장되는 오류가 있음 (5_22_7시45분)
//                    User userInfo = new User(user.getEmail(), user.getUid());
////                    userInfo.coupon.add("coupon1");
////                    userInfo.coupon.add("coupon2");
////                    userInfo.coupon.add("coupon3");
//                    userInfo.coupon.add(1);
//                    new FirebaseUtils().DatabaseInsert("User", userInfo.getUid(), userInfo);
                    //
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                    //

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                updateUI(user);
                // [END_EXCLUDE]
            }
        };
        // [END auth_state_listener]
    }

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

                //User정보를 파이어베이스 서버에 저장하고 메인 화면으로 넘기는 부분
                //로그인에 성공한다면! -----------------------------------여기부터 내가 추가한 코드 *************************************************




//                User userInfo = new User(user.getEmail(), user.getUid());
//                new FirebaseUtils().DatabaseInsert("User", userInfo.getUid(), userInfo);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                //finish();
                //



            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //아래 SharedPreference코드는 정보를 db에 저장하여 다른 화면에서도 해당하는 키 값으로 불러올 수 있게 하기위해서 사용될 수 있다.
        //다른 화면에서 Firebase Database의 user객체에 접근하기 위하여 SharedPreferences에 userUid를 저장한다.
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userUid", acct.getId());
        editor.putString("userEmail", acct.getEmail());
        editor.apply();


//        //로그아웃을 눌러야지 디비에 정보가 저장되는 오류가 있음 (5_22_7시45분)
//        User userInfo = new User(acct.getEmail(), acct.getId());
////                    userInfo.coupon.add("coupon1");
////                    userInfo.coupon.add("coupon2");
////                    userInfo.coupon.add("coupon3");
//        userInfo.coupon.add(1);
//        new FirebaseUtils().DatabaseInsert("User", userInfo.getUid(), userInfo);
        // [START_EXCLUDE silent]


        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {


        if (user != null) {
            mStatusTextView.setText( user.getEmail());
            mDetailTextView.setText( user.getUid());

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.sign_in_button) {
            signIn();
        } else if (i == R.id.sign_out_button) {
            signOut();
        } else if (i == R.id.disconnect_button) {
            revokeAccess();
        }
    }
}

//package com.zerobin.www.beacon_client;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.gms.auth.api.Auth;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.auth.api.signin.GoogleSignInResult;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//
///**
// * Demonstrate Firebase Authentication using a Google ID Token.
// */
//public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
//
//    private static final String TAG = "GoogleActivity";
//    private static final int RC_SIGN_IN = 9001;
//
//    // [START declare_auth]
//    private FirebaseAuth mAuth;
//    FirebaseUser user;
//    // [END declare_auth]
//
//    // [START declare_auth_listener]
//    private FirebaseAuth.AuthStateListener mAuthListener;
//    // [END declare_auth_listener]
//
//    private GoogleApiClient mGoogleApiClient;
//    private TextView mStatusTextView;
//    private TextView mDetailTextView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.login_activity);
//
//        // Views
//        mStatusTextView = (TextView) findViewById(R.id.status);
//        mDetailTextView = (TextView) findViewById(R.id.detail);
//
//        // Button listeners
//        findViewById(R.id.sign_in_button).setOnClickListener(this);
//        findViewById(R.id.sign_out_button).setOnClickListener(this);
//        findViewById(R.id.disconnect_button).setOnClickListener(this);
//
//        // [START config_signin]
//        // Configure Google Sign In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        // [END config_signin]
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
//
//        // [START initialize_auth]
//        mAuth = FirebaseAuth.getInstance();
//        // [END initialize_auth]
//
//
//
//        // [START auth_state_listener]
//        mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                user = firebaseAuth.getCurrentUser();
//
//                //로그인된 User정보가 있을 경우
//                if (user != null) {
//                    // User is signed in
//                    //Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//
//                    //아래 SharedPreference코드는 정보를 db에 저장하여 다른 화면에서도 해당하는 키 값으로 불러올 수 있게 하기위해서 사용될 수 있다.
//                    //다른 화면에서 Firebase Database의 user객체에 접근하기 위하여 SharedPreferences에 userUid를 저장한다.
//                    SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("userUid", user.getUid());
//                    editor.apply();
//
////                    //User정보를 파이어베이스 서버에 저장하고 메인 화면으로 넘기는 부분
////                    //로그인에 성공한다면! -----------------------------------여기부터 내가 추가한 코드 *************************************************
//                      //로그아웃을 눌러야지 디비에 정보가 저장되는 오류가 있음 (5_22_7시45분)
//                    User userInfo = new User(user.getEmail(), user.getUid());
////                    userInfo.coupon.add("coupon1");
////                    userInfo.coupon.add("coupon2");
////                    userInfo.coupon.add("coupon3");
//                    userInfo.coupon.add(1);
//                    new FirebaseUtils().DatabaseInsert("User", userInfo.getUid(), userInfo);
//                    //
////                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
////                    startActivity(intent);
////                    finish();
////                    //
//
//                } else {
//                    // User is signed out
//                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                }
//                // [START_EXCLUDE]
//                updateUI(user);
//                // [END_EXCLUDE]
//            }
//        };
//        // [END auth_state_listener]
//    }
//
//    // [START on_start_add_listener]
//    @Override
//    public void onStart() {
//        super.onStart();
//        mAuth.addAuthStateListener(mAuthListener);
//    }
//    // [END on_start_add_listener]
//
//    // [START on_stop_remove_listener]
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (mAuthListener != null) {
//            mAuth.removeAuthStateListener(mAuthListener);
//        }
//    }
//    // [END on_stop_remove_listener]
//
//    // [START onactivityresult]
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            if (result.isSuccess()) {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = result.getSignInAccount();
//                firebaseAuthWithGoogle(account);
//
//                //User정보를 파이어베이스 서버에 저장하고 메인 화면으로 넘기는 부분
//                //로그인에 성공한다면! -----------------------------------여기부터 내가 추가한 코드 *************************************************
//
//
//
//
////                User userInfo = new User(user.getEmail(), user.getUid());
////                new FirebaseUtils().DatabaseInsert("User", userInfo.getUid(), userInfo);
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                //finish();
//                //
//
//
//
//            } else {
//                // Google Sign In failed, update UI appropriately
//                // [START_EXCLUDE]
//                updateUI(null);
//                // [END_EXCLUDE]
//            }
//        }
//    }
//    // [END onactivityresult]
//
//    // [START auth_with_google]
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
//        // [START_EXCLUDE silent]
//
//
//        // [END_EXCLUDE]
//
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
//
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "signInWithCredential", task.getException());
//                            Toast.makeText(LoginActivity.this, "Authentication failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        // [START_EXCLUDE]
//
//                        // [END_EXCLUDE]
//                    }
//                });
//    }
//    // [END auth_with_google]
//
//    // [START signin]
//    private void signIn() {
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//    // [END signin]
//
//    private void signOut() {
//        // Firebase sign out
//        mAuth.signOut();
//
//        // Google sign out
//        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
//                new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//                        updateUI(null);
//                    }
//                });
//    }
//
//     private void revokeAccess() {
//        // Firebase sign out
//        mAuth.signOut();
//
//        // Google revoke access
//        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
//                new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//                        updateUI(null);
//                    }
//                });
//    }
//
//    private void updateUI(FirebaseUser user) {
//
//
//        if (user != null) {
//            mStatusTextView.setText( user.getEmail());
//            mDetailTextView.setText( user.getUid());
//
//            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
//        } else {
//            mStatusTextView.setText(R.string.signed_out);
//            mDetailTextView.setText(null);
//
//            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
//        }
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
//        // be available.
//        Log.d(TAG, "onConnectionFailed:" + connectionResult);
//        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onClick(View v) {
//        int i = v.getId();
//        if (i == R.id.sign_in_button) {
//            signIn();
//        } else if (i == R.id.sign_out_button) {
//            signOut();
//        } else if (i == R.id.disconnect_button) {
//            revokeAccess();
//        }
//    }
//}