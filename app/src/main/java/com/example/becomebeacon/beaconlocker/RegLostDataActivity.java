package com.example.becomebeacon.beaconlocker;

/**
 * Created by heeseung on 2017-05-23.
 */

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.beaconpop.pyeongchang.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;

public class RegLostDataActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseRef;
    private LostDevInfo devInfo;
    LatLng lostPos;
    CameraPosition cp;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_reg_lost_data);

            Intent notiIntent=getIntent();
            int noti=notiIntent.getIntExtra("NOTI",-1);


            mDatabase = FirebaseDatabase.getInstance();
            mDatabaseRef = mDatabase.getReference();


            Intent intent=getIntent();
            String mac=intent.getStringExtra("MAC");
            BleDeviceInfo bleDeviceInfo = BeaconList.mItemMap.get(mac);

            devInfo = new LostDevInfo();
            devInfo.setDevAddr(bleDeviceInfo.getDevAddress());
            devInfo.setLatitude(bleDeviceInfo.latitude);
            devInfo.setLongitude(bleDeviceInfo.longitude);
            devInfo.setNickNameOfThing(bleDeviceInfo.nickname);
            if(bleDeviceInfo.pictureLink!=null)
                devInfo.setPictureLink(bleDeviceInfo.pictureLink);
            devInfo.setPictureUri(bleDeviceInfo.pictureUri);
//        devInfo.setDevAddr("EE:EE:EE:EE:EE:EE");
//        devInfo.setLatitude(35.885661);
//        devInfo.setLongetude(128.609486);

        if(noti!=-1) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(noti);
            Notifications.notifications.remove(devInfo.getDevAddr()+Values.NOTI_FAR);
        }

            SimpleDateFormat CurDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if(bleDeviceInfo.lastDate!=null) {

            devInfo.setLostDate(bleDeviceInfo.lastDate);
        }
        else
        {
            devInfo.setLostDate("NO-DATE");
        }

        BeaconList.mItemMap.get(devInfo.getDevAddr()).isLost=true;

            mDatabase
                    .getReference("beacon/" + devInfo.getDevAddr() + "/")
                    .child("isLost")
                    .setValue(true); // isLost 속성값 변경

            mDatabase
                    .getReference("beacon/" + devInfo.getDevAddr() + "/")
                    .child("isFar")
                    .setValue(true); // isFar 속성값 변경


            mDatabase
                    .getReference("lost_items/" + devInfo.getDevAddr() + "/")
                    .setValue(devInfo);

            lostPos = new LatLng(devInfo.getLatitude(),devInfo.getLongitude());
            cp = new CameraPosition.Builder().target((lostPos)).zoom(17).build();

            MapFragment mMapFragment = MapFragment.newInstance(new GoogleMapOptions().camera(cp));
            android.app.FragmentTransaction fragmentTransaction =
                    getFragmentManager().beginTransaction();

            fragmentTransaction.replace(R.id.miniMap, mMapFragment);
            fragmentTransaction.commit();
            mMapFragment.getMapAsync(this);

            exit_button_init();
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10800", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    //!!
    private void exit_button_init() {
        try {
            findViewById(R.id.rld_ExitBtn).setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();


            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10801", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        try {
            MarkerOptions markerOptions = new MarkerOptions();
            GpsInfo lostPosContents= new GpsInfo(GetMainActivity.getMainActity(),GetMainActivity.getMainActity());
            lostPosContents.getLocation();

            markerOptions.position(new LatLng(lostPosContents.lat,lostPosContents.lon));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
            map.addMarker(markerOptions);
            map.getUiSettings().setScrollGesturesEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setZoomGesturesEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 관리자에게 문의하세요\n오류코드 : 10802", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}