package com.zerobin.www.beacon_client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.beaconpop.pyeongchang.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by Byun YB on 2017-04-24.
 */

public class StampActivity extends Activity{
    ImageView stamp1, stamp2, stamp3, stamp4, stamp5, stamp6, stamp7, stamp8, stamp9, stamp10, stamp11, stamp12, stamp13;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    String userUid;
    Button getCoupon;
    List<Object> couponList;
    Bitmap bitmap;
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = mStorage.getReference();
    final String STAMPCOUPON = "STAMPCOUPON";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.stamp2);

        stamp1 = (ImageView) findViewById(R.id.stamp1);
        stamp2 = (ImageView) findViewById(R.id.stamp2);
        stamp3 = (ImageView) findViewById(R.id.stamp3);
        stamp4 = (ImageView) findViewById(R.id.stamp4);
        stamp5 = (ImageView) findViewById(R.id.stamp5);
        stamp6 = (ImageView) findViewById(R.id.stamp6);
        stamp7 = (ImageView) findViewById(R.id.stamp7);
        stamp8 = (ImageView) findViewById(R.id.stamp8);
        stamp9 = (ImageView) findViewById(R.id.stamp9);
        stamp10 = (ImageView) findViewById(R.id.stamp10);
        stamp11 = (ImageView) findViewById(R.id.stamp11);
        stamp12 = (ImageView) findViewById(R.id.stamp12);
        stamp13 = (ImageView) findViewById(R.id.stamp13);


        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        userUid = sharedPreferences.getString("userUid", "UserUid를 가져오지 못했습니다.");

//
       bringStampList();
    }
    //FIREBASE에서 사용자의 stamplist를 가져온다.
    //stamplist가 존재하지 않을 경우 리스트에 stampNum을 바로 저장한다.
    private void bringStampList(){
        try {
            final DatabaseReference myRef = databaseReference.child("User").child(userUid).child("stamplist");
            Log.i("stampList", "************** "+userUid+ "bringStampList" );
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        Stamp userHaveStamp = dataSnapshot.getValue(Stamp.class);
                        showStamp(userHaveStamp);

//                        //스탬프를 전부 모은경우 스탬프리스트를 초기화 한 후 스탬프 보상 쿠폰 추가!
//                        if(stampList == 222222){
//                            //스탬프리스트 초기화
//                            int init = 111111;
//                            myRef.setValue(init);
//                            //유저 쿠폰 리스트에 쿠폰 추가하기
//                            myRef.getParent().child("coupon").addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    //사용자 디비정보에서 쿠폰 이름 추가
//                                    couponList = (List<Object>) dataSnapshot.getValue();
//                                    couponList.add(STAMPCOUPON);
//                                    myRef.getParent().child("coupon").setValue(couponList);
//
//                                    //사용자 저장소에 쿠폰 이미지 저장
//                                    generateStampCoupon();
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
//                        }

                    }catch (Exception e){ //받은 비컨의 Address가 파이어베이스 서버에 저장되어 있지 않은경우 예외가 발생할 수 있다.
                        e.printStackTrace();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            //stamplist가 저장 안되어 있는 경우. 즉 처음 저장된 스탬프인 경우
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void showStamp(Stamp userHvaeStamp){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize=2;
        Bitmap bitmap;

        int[] stampArray = {0, userHvaeStamp.getStamp1(), userHvaeStamp.getStamp2(), userHvaeStamp.getStamp3(),
                userHvaeStamp.getStamp4(), userHvaeStamp.getStamp5(), userHvaeStamp.getStamp6(),
                userHvaeStamp.getStamp7(), userHvaeStamp.getStamp8(), userHvaeStamp.getStamp9(),
                userHvaeStamp.getStamp10(), userHvaeStamp.getStamp11(), userHvaeStamp.getStamp12(),  userHvaeStamp.getStamp13() };

        Log.i("stampArray", stampArray[4] + " "+ stampArray[11]+"");


        for(int i=0; 0<stampArray.length; i++){
            switch (stampArray[i]){
                case 1:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_1_check, options);
                    stamp1.setImageBitmap(bitmap);
                    //stamp1.setImageResource(R.drawable.stamp_1_check);
                    break;
                case 2:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_2_check, options);
                    stamp2.setImageBitmap(bitmap);
                    //stamp2.setImageResource(R.drawable.stamp_2_check);
                    break;
                case 3:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_3_check, options);
                    stamp3.setImageBitmap(bitmap);
                   // stamp3.setImageResource(R.drawable.stamp_3_check);
                    break;
                case 4:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_4_check, options);
                    stamp4.setImageBitmap(bitmap);
                    //stamp4.setImageResource(R.drawable.stamp_4_check);
                    break;
                case 5:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_5_check, options);
                    stamp5.setImageBitmap(bitmap);
                    //stamp5.setImageResource(R.drawable.stamp_5_check);
                    break;
                case 6:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_6_check, options);
                    stamp6.setImageBitmap(bitmap);
                   // stamp6.setImageResource(R.drawable.stamp_6_check);
                    break;
                case 7:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_7_check, options);
                    stamp7.setImageBitmap(bitmap);
                   // stamp7.setImageResource(R.drawable.stamp_7_check);
                    break;
                case 8:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_8_check, options);
                    stamp8.setImageBitmap(bitmap);
                    //stamp8.setImageResource(R.drawable.stamp_8_check);
                    break;
                case 9:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_9_check, options);
                    stamp9.setImageBitmap(bitmap);
                    //stamp9.setImageResource(R.drawable.stamp_9_check);
                    break;
                case 10:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_10_check, options);
                    stamp10.setImageBitmap(bitmap);
                    //stamp10.setImageResource(R.drawable.stamp_10_check);
                    break;
                case 11:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_11_check, options);
                    stamp11.setImageBitmap(bitmap);
                    //stamp11.setImageResource(R.drawable.stamp_11_check);
                    break;
                case 12:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_12_check, options);
                    stamp12.setImageBitmap(bitmap);
                   // stamp12.setImageResource(R.drawable.stamp_12_check);
                    break;
                case 13:
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.stamp_13_check, options);
                    stamp13.setImageBitmap(bitmap);
                   //stamp13.setImageResource(R.drawable.stamp_13_check);
                    break;
                default:
                    break;
            }
        }
    }

    //Firebase Storage저장소에 이미지를 저장
    public void generateStampCoupon(){

        StorageReference mountainsRef = mStorageRef.child("User").child(userUid).child(STAMPCOUPON);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.coupon3);
        //비트맵 이미지를 byte[]로 바꿔서 저장한다.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainsRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { //이미지 업로드가 성공했을 때 실행
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
               // downloadUrl = taskSnapshot.getDownloadUrl();
                AlertDialog.Builder builder = new AlertDialog.Builder(StampActivity.this);
                builder.setTitle("스탬프 보상 쿠폰 발급");
                builder.setMessage("스탬프 보상 쿠폰을 발급 하시겠습니까?");
                builder.setIcon(R.drawable.coupon1);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "스탬프 보상 쿠폰이 발급되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
//                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Toast toast = new Toast(MainActivity.this);
//                        TextView textView = new TextView(MainActivity.this);
//                        textView.setText("난 어디에");
//                        toast.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
//                        toast.setView(textView);
//
//                        toast.setDuration(Toast.LENGTH_SHORT);
//                        toast.show();
//                    }
//                });
                builder.show();
            }
        });
    }
}