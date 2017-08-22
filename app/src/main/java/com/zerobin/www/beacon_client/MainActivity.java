package com.zerobin.www.beacon_client;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.beaconpop.pyeongchang.R;
import com.example.becomebeacon.beaconlocker.*;
import com.example.becomebeacon.beaconlocker.LoginActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    BeaconScanService beaconScanService; //Service 객체
    boolean isService = false; //Service중인지 확인용
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1001;
    String userUid, userEmail;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    GoogleApiClient mGoogleApiClient;


    protected void onStart() {
        try {
            super.onStart();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 40505", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = LoginActivity.getAuth();
        mUser = LoginActivity.getUser();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initBluetoothAdapter();

        //로그인시 db에 user정보가 등록되어 있다면 등록을 하지 않고,
        //등록되어 있지 않다면 user정보를 등록한다.

        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        userUid = sharedPreferences.getString("userUid", "UserUid를 가져오지 못했습니다.");
        userEmail = sharedPreferences.getString("userEmail", "UserEmail를 가져오지 못했습니다.");

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("User").child(userUid);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(getApplicationContext(),  dataSnapshot.getValue()+"",Toast.LENGTH_SHORT).show();
                if(dataSnapshot.getValue() == null){
                                       // 로그아웃을 눌러야지 디비에 정보가 저장되는 오류가 있음 (5_22_7시45분)
                    //계정의 acct.getId()를 key값으로 사용함
                    User userInfo = new User(userEmail, userUid);
            //      userInfo.coupon.add("coupon1");
                    userInfo.coupon.add(1);
                    new FirebaseUtils().DatabaseInsert("User", userInfo.getUid(), userInfo);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "저장되어 있지 않다",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //블루투스가 꺼져 있을 때 키도록 요청하는 코드
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent  = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    //Service와 통신하기 위하여 필요한 Connection!
    ServiceConnection connection = new ServiceConnection() {
        //Service와 연결되었을 때 호출되는 메서드
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BeaconScanService.MyBinder myBinder = (BeaconScanService.MyBinder) service;
            beaconScanService = myBinder.getService();

            isService = true; //Service 실행 중
        }

        //서비스 연결이 끊어졌을 때 호출
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isService = false;
        }
    };
    /*****************************************************************************************
     *  Function: initBluetoothAdapter
     *
     *  Description
     *      - Bluetoth Adapter 설정
     *
     *****************************************************************************************/
    public void initBluetoothAdapter()
    {
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            //finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(mBluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            //finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    //네비게이션 항목에서 선택된 항목에 대한 이벤트 처리
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //Beacon Scan Service Intent
        Intent  Service = new Intent(this, BeaconScanService.class);

        if (id == R.id.nav_map) {
            Intent intent = new Intent(this, GoogleMap.class);
            startActivity(intent);
        } else if (id == R.id.nav_stamp) {
            Intent intent = new Intent(this, StampActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_board) {
            Intent intent = new Intent(this, ComunicationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_coupon) {
                Intent intent = new Intent(this, CuponActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_prevention) {
                Intent intent = new Intent(this, com.example.becomebeacon.beaconlocker.MainActivity.class);
                startActivity(intent);

            } else if(id == R.id.background_on){  //백그라운드 기능 활성화 시 비컨 스캔 Service 시작
//            startService(Service);
//            bindService(Service, //Service Intent 객체
//                    connection, //서비스와 연결에 대한 정의
//                    Context.BIND_AUTO_CREATE); //flag
                startService(Service);
                Log.i("main액티비티", "서비스 시작");
        }else if(id == R.id.background_off){ //백그라운드 기능 비 활성화 시 비컨 스캔 Service 시작
           // stopService(Service);
            //unbindService(connection); //Service 종료
            stopService(Service);
        }else if(id == R.id.logout){
//            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//            firebaseAuth.signOut();

            signOut();
            Intent intent = new Intent(this, com.example.becomebeacon.beaconlocker.LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        try {
            mAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            // ...
                            Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10509", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //안드로이드 버전 6.0 이상부터 어떤 권한들은 앱 안에서 사용 승인을 받아야 한다.
    //이 때 필요한 코드이다.
   private void checkPermission(){
        // Here, thisActivity is the current activity

       //밑에는 체크할 권한 넣고 체크하면 됨.
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}