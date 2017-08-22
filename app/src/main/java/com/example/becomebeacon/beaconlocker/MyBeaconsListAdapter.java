package com.example.becomebeacon.beaconlocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.zerobin.www.beacon_client.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 함상혁입니다 on 2017-04-27.
 */

public class MyBeaconsListAdapter extends BaseAdapter {
    private Context mContext;
    LayoutInflater mInflater;
    int mLayout;
    private boolean isScanning = false;
    private ArrayList<BleDeviceInfo> mBleDeviceInfoArrayList;
    private Bitmap mBitmap;
    public FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mUserAddressRef;
    private FirebaseUser mUser;
    private ImageView mImage;
    Bitmap bitmapImage;
    // 검색된 BLE 장치가 중복 추가되는 부분을 방지하기 위해 HashMap을 사용
    // String: Device Address(key값)
    private HashMap<String, BleDeviceInfo> mHashBleMap = new HashMap<String, BleDeviceInfo>();


    public MyBeaconsListAdapter(Context context, int layout, ArrayList<BleDeviceInfo> arBleList,
                                HashMap<String, BleDeviceInfo> hashBleMap)
    {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mBleDeviceInfoArrayList = arBleList;
        mLayout = layout;
        mHashBleMap = hashBleMap;
    }

    public synchronized void addOrUpdateItem(BleDeviceInfo info)
    {
        if(mHashBleMap.containsKey(info.getDevAddress()))
        {
            mHashBleMap.get(info.getDevAddress()).setRssi(info.getRssi());
        }
        else
        {
            mBleDeviceInfoArrayList.add(info);
            mHashBleMap.put(info.getDevAddress(), info);
        }

        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mBleDeviceInfoArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mBleDeviceInfoArrayList.get(position);
    }

    public void addBleDeviceItem(BleDeviceInfo item)
    {
        if(!mBleDeviceInfoArrayList.contains(item))
            mBleDeviceInfoArrayList.add(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if(convertView == null)
        {
            convertView = mInflater.inflate(mLayout, parent, false);
        }



        //TextView txtUuid = (TextView)convertView.findViewById(R.id.text_uuid);
        //txtUuid.setText("UUID: " + mBleDeviceInfoArrayList.get(position).proximityUuid);
        if(PictureList.pictures.containsKey(mBleDeviceInfoArrayList.get(position).devAddress)) {
            ImageView image = (ImageView) convertView.findViewById(R.id.device_image);
            image.setImageBitmap(PictureList.pictures.get(mBleDeviceInfoArrayList.get(position).devAddress));

        }
        else
        {
            ImageView image = (ImageView) convertView.findViewById(R.id.device_image);
            image.setImageBitmap(PictureList.pictures.get(R.mipmap.ic_launcher_round));

        }

        TextView txtBdName = (TextView)convertView.findViewById(R.id.text_bd_name);
        txtBdName.setText(mBleDeviceInfoArrayList.get(position).nickname);

        TextView txtBdAddress = (TextView)convertView.findViewById(R.id.text_bd_address);
        //txtBdAddress.setText("MAC ID: " + mBleDeviceInfoArrayList.get(position).devAddress);
        txtBdAddress.setVisibility(View.GONE);

        /*TextView txtMajor = (TextView)convertView.findViewById(R.id.text_major);
        txtMajor.setText("Major: " + String.valueOf(mBleDeviceInfoArrayList.get(position).major));

        TextView txtMinor = (TextView)convertView.findViewById(R.id.text_minor);
        txtMinor.setText("Minor: " + String.valueOf(mBleDeviceInfoArrayList.get(position).minor));

        TextView txtRssi = (TextView)convertView.findViewById(R.id.text_rssi);
        txtRssi.setText("RSSI: " + String.valueOf(mBleDeviceInfoArrayList.get(position).rssi) + " dbm");

        TextView txtTxPower = (TextView)convertView.findViewById(R.id.text_txpower);
        //txtTxPower.setText("Tx Power: " + String.valueOf(mBleDeviceInfoArrayList.get(position).measuredPower) + " dbm");
        txtTxPower.setText("Tx Power: " + String.valueOf(mBleDeviceInfoArrayList.get(position).txPower) + " dbm");      // changsu
        */
        TextView txtDistance = (TextView)convertView.findViewById(R.id.text_distance);
        txtDistance.setText("떨어진 거리: "// + String.valueOf(mBleDeviceInfoArrayList.get(position).distance) + " m ("
                + String.format("%.2f", mBleDeviceInfoArrayList.get(position).distance2) + "m");

        //TextView txtTimeout = (TextView)convertView.findViewById(R.id.text_timeout);
        //txtTimeout.setText("Timeout: " + String.valueOf(mBleDeviceInfoArrayList.get(position).timeout));


        //편집중
        Button btnConnect = (Button)convertView.findViewById(R.id.button_connect);
        btnConnect.setVisibility(View.GONE);
        LinearLayout layoutConnect = (LinearLayout)convertView.findViewById(R.id.linearLayout);
        layoutConnect.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v)
            {


                Activity mActi=GetMainActivity.getMainActity();
                Intent intent = new Intent(mActi, BeaconDetailsActivity.class);
                intent.putExtra("MAC",mBleDeviceInfoArrayList.get(pos).devAddress);
                mActi.startActivity(intent);



            }
        });
//
//
        return convertView;
    }



}