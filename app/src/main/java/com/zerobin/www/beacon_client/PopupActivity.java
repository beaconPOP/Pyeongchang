package com.zerobin.www.beacon_client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.beaconpop.pyeongchang.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by Byun YB on 2017-05-09.
 */

//화면 접근시 해당하는 비콘 Address를 통하여 서버에서 저장된 정보를 가져와서 보여줘야 한다.
public class PopupActivity extends Activity implements View.OnClickListener {
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference();
    ImageView contentImageView;
    String textBdAddress, beaconContenTitle, couponKey, userUid;
    Bitmap bitmapImage;
    byte[] byteImage;
    Button image_save_btn;
    boolean return_value;
    List<Object> couponList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //상단 상태바 없에는 코드

        //잠자는 화면을 깨우는 설정을 한다.
        PushWakeLock.acquireCpuWakeLock(this);

        setContentView(R.layout.popupactivity);
        contentImageView = (ImageView) findViewById(R.id.contentImageView);
        image_save_btn = (Button) findViewById(R.id.image_save_btn);
        image_save_btn.setOnClickListener(this);
        //textView = (TextView) findViewById(R.id.deviceAddress);

        //선택된 비컨 주소 받아오기
        Intent intent = getIntent();
        textBdAddress = intent.getExtras().getString("textBdAddress");
        byteImage = intent.getExtras().getByteArray("image");

        bitmapImage = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
        contentImageView.setImageBitmap(bitmapImage);

        try {
            DatabaseReference myRef = databaseReference.child("Beacon").child(textBdAddress);
            Log.i("myRef", myRef + "");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        beaconContenTitle = (dataSnapshot.child("Title").getValue().toString());
                        Log.i("myRef", beaconContenTitle + "");
                        //비컨address와 해당 비컨에 저장된 컨텐츠의 title을 더한 것을 Key값으로 Storage에 쿠폰을 저장한다.
                        couponKey = textBdAddress + beaconContenTitle;
                        Log.i("myRef", couponKey);
                    } catch (Exception e) { //받은 비컨의 Address가 파이어베이스 서버에 저장되어 있지 않은경우 예외가 발생할 수 있다.
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("getBeaconDistanceMeter", "getBeaconDistanceMeter Error");
                }
            });
        } catch (Exception e) {
            Log.d("getBeaconDistanceMeter", "예외 발생----------------------------------");
        }

        //화면을 깨우는 설정을 해제한다.
        PushWakeLock.releaseCpuLock();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_save_btn:
                uploadImage();
                break;
            default:
        }
    }

    //Firebase Storage저장소에 userUid를 key값으로 받은 쿠폰을 저장
    public void uploadImage() {
        //비트맵 이미지를 byte[]로 바꿔서 저장한다.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //아래 SharedPreference코드는 정보를 db에 저장하여 다른 화면에서도 해당하는 키 값으로 불러올 수 있게 하기위해서 사용될 수 있다.
        //다른 화면에서 Firebase Database의 user객체에 접근하기 위하여 SharedPreferences에 userUid를 저장한다.
        //여기서는 userUid값을 가져온다.
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        userUid = sharedPreferences.getString("userUid", "UserUid를 가져오지 못했습니다.");

        Log.i("coupon", couponKey + " ");

        ////////////////////////////////
        //** 임시로 아직 couponkey를 db에서 받아오지 못했을 경우 잠시후에 다시 눌러달라는 요청으로 처리하였음
        // 쿠폰타이틀값을 비컨스켄서비스에서 미리 저장해서 넘겨주는 방향으로 수정해야 할 듯
        ////////////////////////////////
        if(couponKey == null){
            Toast.makeText(this, "잠시 후에 다시 눌러주세요", Toast.LENGTH_SHORT).show();
        }else{
            new FirebaseUtils().couponSave("User", userUid, data, couponKey); //root name, userUid, 이미지 고유값
            // new FirebaseUtils().insertCoupon(new String[]{"User", userUid, "coupon"}, couponList); //user 정보에 쿠폰 등록하기

            try {
                Log.i("myRef", "userUid는?????: " + userUid);
                final DatabaseReference myRef = databaseReference.child("User").child(userUid).child("coupon");

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            couponList = (List<Object>) dataSnapshot.getValue();
                            Log.i("myRef", couponList + "");
                            //비컨address와 해당 비컨에 저장된 컨텐츠의 title을 더한 것을 user의 couponList에 저장한다.
                            couponKey = textBdAddress + beaconContenTitle;
                            Log.i("myRef", couponList + " -------------------------");
                            if (couponList.contains(couponKey)) { //리스트에 동일한 쿠폰이 있다면 추가하지 않는다.
                                Log.i("myRef", "datachange 함수 안***********************************************************"); //리스트에 동일한 쿠폰의 url값이 있을 때 여기로 들어와지는지 확인 필요
                            } else {
                                //가지고 있는 쿠폰이 아니라면 list에 쿠폰을 등록한다.
                                couponList.add(couponKey);
                                myRef.setValue(couponList);
                            }

                        } catch (Exception e) { //받은 비컨의 Address가 파이어베이스 서버에 저장되어 있지 않은경우 예외가 발생할 수 있다.
                            e.printStackTrace();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("getBeaconDistanceMeter", "getBeaconDistanceMeter Error");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(this, "쿠폰 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}