package com.zerobin.www.beacon_client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Byun YB on 2017-04-24.
 */

public class CuponActivity extends Activity{

    final int MAX_SIZE = 100;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    String userUid;
    List<Object> couponList;
    String[] couponArray;
    Bitmap[] bitmapCouponArray = new Bitmap[MAX_SIZE];
    ArrayList<Bitmap> mBitmapCouponArray = new ArrayList<Bitmap>();
    ListView couponListView;
    Button getCoupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cupon_activity);

        couponListView = (ListView) findViewById(R.id.coupon_listview);


        //저장된 userUid 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        userUid = sharedPreferences.getString("userUid","UserUid를 가져오지 못했습니다.");

        DatabaseReference myRef = databaseReference.child("User").child(userUid).child("coupon");

        //Firebase Database에 User가 가지고있는 쿠폰의 목록을 가져온다. Storage에 접근하여 쿠폰을 다운받기 위하여!
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                couponList = (List<Object>) dataSnapshot.getValue();
                couponList.removeAll(Collections.singleton(null));
                couponArray =  couponList.toArray(new String[couponList.size()]); //List를 Array로 바꾸는 부분. 이렇게 하면 배열에 null값도 들어가서 안씀
                // Log.i("couponList", "" + couponArray);

                for(int i=0; i<couponArray.length; i++) {
                    Log.i("couponList", " db에서 쿠폰 title 가져온 것 :  " + couponArray[i]);
                }

                //아래에는 쿠폰키값으로 Storage에서 이미지 가져와서 bitmap[]에 저장하는 함수가 와야한다.
                bringCouponImage();



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //arrayList =   파이어베이스 서버에서 쿠폰 다운받아서 저장할 것
        //WeatherAdapter adapter = new WeatherAdapter(this, data, R.layout.line); //어뎁터 설정할 것

//        ListView listView = (ListView) findViewById(R.id.list_view); //리스트뷰 찾아서 어댑터 설정정
//        listViewsetAdapter(adapter);


    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    //User가 가진 쿠폰의 array의 크기많큼 반복을 하며 Storage에서 쿠폰을 다운받아서 비트맵 배열에 저장
    void bringCouponImage() {
        int i;
        final long ONE_MEGABYTE = 1024 * 1024;

        for(i=0; i<couponArray.length; i++) {
            StorageReference islandRef = storageReference.child("User/" + userUid +"/"+ couponArray[i]);
            Log.i("couponList", "저장소가 열리는가 ? :" + islandRef);
            final int j = i;
            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) { //다운로드 성공시 이미지를 byte[]로 리턴
                    // Data for "images/island.jpg" is returns, use this as needed
                    bitmapCouponArray[j] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); //비트맵으로 바꿀 배열, 시작번호, 총길이
                    mBitmapCouponArray.add(bitmapCouponArray[j]);
//                   Log.i("couponList", "bringCouponImage : " + mBitmapCouponArray.get(j).toString());
                    Log.i("couponList", "bringCouponImage : " + bitmapCouponArray[j].toString());

                    //coupon배열과 counponList의 크기가 같으면 쿠폰을 전부 가져왔다는 의미이기 때문에 List를 Adapter에 넘겨서 리스트뷰에  저장한다.
                   if(mBitmapCouponArray.size() == couponArray.length){
                        CouponAdapter couponAdapter = new CouponAdapter(CuponActivity.this, mBitmapCouponArray, R.layout.coupon_line);
                        couponListView.setAdapter(couponAdapter);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Toast.makeText(getApplicationContext(), "쿠폰 정보를 불러오는것을 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            });

        }
      //  mBitmapCouponArray.add(BitmapFactory.decodeResource(getResources(), R.drawable.coupon1));
        //ListView에 Adapter를 만들어서 달아주는 부분


    }
}
