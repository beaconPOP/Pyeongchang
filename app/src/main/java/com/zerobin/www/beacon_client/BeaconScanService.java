package com.zerobin.www.beacon_client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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

import java.util.ArrayList;

/**
 * Created by Byun YB on 2017-05-05.
 */

public class BeaconScanService extends Service implements Runnable{
    private int count = 0;
    private static boolean isService = false;
    private BluetoothAdapter mBluetoothAdapter;
    Thread myThread;
    IBinder mBinder = new MyBinder();

    //비컨에 설정된 거리정보를 읽어오기 위하여 사용 필요
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference = firebaseStorage.getReference();


    //비컨에 설정된 거리(M)정보
    Double beaconDistance;

    //비컨의 타입(정보, 스탬프)
    int beaconType;
    int stampBeaconNumber;
    private String userUid;
    int stampList;

    ///////////
    private static final long SCAN_PERIOD = 1000;       // 10초동안 SCAN 과정을 수행함
    private static final boolean IS_DEBUG = true;
    private static final long TIMEOUT_LIMIT = 20;
    private static final long TIMEOUT_PERIOD = 1000;
    //private static final boolean USING_WINI = true; // TI CC2541 사용: true

    private BleUtils mBleUtils;
    /*
        Member Variables
     */

    private ArrayList<BleDeviceInfo> mArrayListBleDevice;   // scan 후 검색된 pebBle 장비를 저장하는 array list

    //private Handler mHandler;
    boolean mScanning;

    /*
        Widgets
     */
    ////////////////////////////

    class MyBinder extends Binder{
        BeaconScanService getService(){
            return BeaconScanService.this;
        }
    }

    @Override
    //Service 역시 onCreate()메서드에서 부터 실행된다.
    public void onCreate() {
        super.onCreate();
        initBluetoothAdapter();
        mBleUtils = new BleUtils(); //거리 계산을 위한 인스턴스 생성
        mArrayListBleDevice = new ArrayList<BleDeviceInfo>(); //비컨 정보를 담을 클래스 arrayList생성
        isService = true;
        Log.i("service ", " "+ "onCreate " + isService );
        mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
        mTimeOut.sendEmptyMessageDelayed(0, TIMEOUT_PERIOD);

        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        userUid = sharedPreferences.getString("userUid", "UserUid를 가져오지 못했습니다.");

    }

    @Nullable
    @Override
    //lBinder 객체가 '서비스'와 '클라이언트'사이의 인터페이스 역할을 한다
    //startService()로 실행하면 쓸모 없다.
    public IBinder onBind(Intent intent) {

        Log.i("service ", " "+ "onBind " + isService );
        myThread = new Thread(this);
        myThread.start();
        return mBinder;
    }

    //해당 서비스가 실제로 제공하는 코드를 구현
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        myThread = new Thread(this);
        myThread.start();

        Log.i("service ", " "+ "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isService = false; //백그라운드 기능 끌 시 'isService = false;' 로 바꾸어 스레드 무한 실행 방지.
        if(mHandler != null) {
            mHandler.removeMessages(0);
        }
        if(mTimeOut != null){
            mTimeOut.removeMessages(0);
        }
        Log.i("service ", " "+ "onDestroy");

    }


    @Override
    public void run() {
        Log.i("service ", "run "+ isService);



        //백그라운드 기능이 켜져있다면
        while(isService){
            try {
                Thread.sleep(3000);
                //Log.i("service ", " "+ count++);
                if(mArrayListBleDevice.size() != 0) {
                    for (int i = 0; i < mArrayListBleDevice.size(); i++) {
                        Log.i("스캔된 비컨 정보", mArrayListBleDevice.get(i).devAddress + " TimeOut 값 : " + mArrayListBleDevice.get(i).getTimeout()
                                + " Distance 값 : " + mArrayListBleDevice.get(i).getDistance2() + "M");
//                        Log.i("beaconType", "**********************비콘 타입"+beaconType);
//                        Log.i("beaconType", "**********************비콘 스탬프 넘버 :"+stampBeaconNumber);
//                        Log.i("beaconType", "**********************User Stamp List :"+stampList);
                    }
                }
               Log.i("--","----------------------------------------------------------------------------------------------------------");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
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

        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(mBluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();

            return;
        }
    }

    /*****************************************************************************************
     *  Bluetooth LE Device Scan Handler
     *
     *  Description
     *      - SCAN_PERIOD 간격으로 startLeScan()을 호출하여 beacon을 검색함
     *
     *****************************************************************************************/
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if(mScanning)
            {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }

            mScanning = true;
           // Log.i("handleMessage", "handleMessage-----------handleMessage----------handleMessage");
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mHandler.sendEmptyMessageDelayed(0, SCAN_PERIOD);
        }
    };
    /*****************************************************************************************
     *  Bluetooth LE Device Timeout Handler
     *
     *  Description
     *      - TIMEOUT_PERIOD(1000) 간격으로 어뎁터 리스트의 목록들의 timeout을 갱신
     *      - timeout이 0이 되면 목록에서 제거 시킨다.
     *
     *****************************************************************************************/

    private Handler mTimeOut = new Handler(){
        public void handleMessage(Message msg){
           // Log.i("TAG","TIMEOUT UPDATE");

            int maxRssi = 0;
            int maxIndex = -1;

            //timeout counter update
            //리스트에서 스캔된지 오래 된 비컨은 timeout 카운트를 줄이면서 카운트가 20->0이되면 리스트에서 없앤다.
            for (int i= 0 ; i < mArrayListBleDevice.size() ; i++){
                mArrayListBleDevice.get(i).timeout--;
                if(mArrayListBleDevice.get(i).timeout == 0){
//                    mItemMap.remove(mArrayListBleDevice.get(i).devAddress);
                    mArrayListBleDevice.remove(i);
                }
                else{
                    if(mArrayListBleDevice.get(i).rssi > maxRssi || maxRssi == 0)
                    {
                        maxRssi = mArrayListBleDevice.get(i).rssi;
                        maxIndex = i;
                    }
                }
            }

            mTimeOut.sendEmptyMessageDelayed(0,TIMEOUT_PERIOD);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) { //(장치의 식별자, 블루투스 하드웨어에서 기록된 rssi값, 장치에서 제공하는 기록)
            mScanning = true;
            getBleDeviceInfoFromLeScan(device, rssi, scanRecord);
           // Log.i("잡힌 비컨의 리스트 --------", "                             " + device.getAddress());
                    /*
                        Exception 방지를 위해 runOnUiThread()에서 notifyDataSetChanged()를 호출함
                        - Only the original thread that created a view hierarchy can touch its views
                     */
            //runOnUiThread : 현재의 스레드가 UI스레드라면 즉시 화면을 바꾸고, 아니라면 UI스레드의 메시지 메시지 처리 대기 큐에 전달한다.
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    mBleDeviceListAdapter.notifyDataSetChanged();
//                    //mBleDeviceListAdapter.addOrUpdateItem();
//                }
//            });

        }
    };




    /*****************************************************************************************
     *  Function: getBleDeviceInfoFromLeScan
     *
     *  Description
     *      - scanRecord[] 데이터를 파싱하여
     *        proximityUUID, Major, Minor, Measured Power, distance 값을 구함
     *
     *****************************************************************************************/
    private void getBleDeviceInfoFromLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
    {
        String devName;
        String devAddress;
        String scanRecordAsHex;     // 24byte
        String proximityUUID;       // 12 + 5 characters
        int major, minor;
        //int measuredPower;
        int txPower;                // changsu: 혼동을 없애기 위헤 measuredPower를 txPower로 변경함
        int rssiValue = rssi;

        //Callback으로 전달된 device객체에서 device이름과 주소를 가져온다.
        devName = device.getName();
        if(devName == null)
            devName = "Unknown";

        devAddress = device.getAddress();
        if(devAddress == null)
            devAddress = "Unknown";

        if(!IS_DEBUG) {
//            Log.d(TAG, "getBleDeviceInfoFromLeScan() : rssi: " + rssi +
//                    ", addr: " + devName +
//                    ", name: " + devAddress);
        }

        //이 비교부분을 위로 올리자.. 일치 하지않으면 뭐할 연산하나....
        scanRecordAsHex = mBleUtils.ByteArrayToString(scanRecord);

        //24byte
        proximityUUID = String.format("%s-%s-%s-%s-%s",
                scanRecordAsHex.substring(18, 26),
                scanRecordAsHex.substring(26, 30),
                scanRecordAsHex.substring(30, 34),
                scanRecordAsHex.substring(34, 38),
                scanRecordAsHex.substring(38, 50));


        major = mBleUtils.byteToInt(scanRecord[25], scanRecord[26]);
        minor = mBleUtils.byteToInt(scanRecord[27], scanRecord[28]);

        txPower = scanRecord[29];

        //Log.d(TAG, "proximityUUID: " + proximityUUID);

        //원래는 특정 제조사의 비컨 정보만을 리스트에 출력하였으나 if조건을 true로 바꿈으로써 모든 ble장치를 리스트에 출력하게 만들었다.
//        if(proximityUUID.equals(BEACON_UUID) || proximityUUID.equals(BluetoothUuid.WIZTURN_PROXIMITY_UUID.toString()) || proximityUUID.equals(BluetoothUuid.WINI_UUID.toString()) )
        if(true)
        {
            try {
                //Log.d(TAG, "Found Pebble UUID: " + proximityUUID);

                double distance = mBleUtils.getDistance(rssiValue, txPower);
                double distance2 = mBleUtils.getDistance_20150515(rssiValue, txPower);

//                Log.d(TAG, "dev name: " + devName +
//                        ", addr: " + devAddress +
//                        ", major: " + major +
//                        ", minor: " + minor +
//                        ", rssi: " + rssi +
//                        ", txPower: " + txPower +
//                        ", distance: " + distance +
//                        ", distance2: " + distance2);

                BleDeviceInfo item = new BleDeviceInfo(proximityUUID, devName, devAddress, major, minor,
                        txPower, rssiValue, distance, distance2);

                //txMeter가 설정되어있지 않을 때 한번만 실행한다.
                if(item.getTxMeter() == 0.0) {
                    item.setTxMeter(getBeaconDistanceMeter(item.devAddress)); //beaconDeviceInfo에 txMeter에 몇 M에서 신호를 받을지 값을 설정한다.
                    Log.d("item.getTxMeter()", "item.getTxMeter()");
                }
                //거리를 계산한다
                //비컨에 설정된 거리안에 단말이 들어오면 arrayList에 비컨정보 저장 후 팝업을 띄운다.
                //아닐시 저장 & 팝업 안한다.
                boolean upload = calculateBeaconDistance(item.distance2, item.txMeter, devAddress);
                //Log.i("beaconType", " : "+upload);
                if(upload) {

                    updateBleDeviceList(item);
                }


            }catch(Exception ex)
            {
                //Log.e("Error", "Exception: " + ex.getMessage());
                ex.printStackTrace();
            }

        }
    }
    /*****************************************************************************************
     *  Function: calculateBeaconDistance
     *
     *  Description
     *      - Beacon에 설정된 거리와 현재 측정된 거리를 비교한다.
     *
     *****************************************************************************************/
    private boolean calculateBeaconDistance(double distance, double txMeter, String devAddress){
        if(txMeter>distance) {//측정된 거리가 설정된 거리보다 작을 때 true 반환
           // Log.i("거리 비교", devAddress + " : 비컨 설정 전송 거리 : " + txMeter + "M   비컨과 휴대폰 사이의 거리 : " + Double.parseDouble(String.format("%.1f",distance )) +"M");
            return true;
        }
        else
            return false;
    }

    /*****************************************************************************************
     *  Function: getBeaconDistanceMeter
     *
     *  Description
     *      - firebase서버에서 meter값을 한번 받아오면 다시는 실행 안하게 해야 한다.
     *
     *****************************************************************************************/
    public double getBeaconDistanceMeter(String devAddress){
        try {
            DatabaseReference myRef = databaseReference.child("Beacon").child(devAddress);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        beaconDistance = Double.parseDouble(dataSnapshot.child("Distance(M)").getValue().toString());
//                        beaconType = Integer.parseInt(dataSnapshot.child("BEACON_CONTENT_TYPE").getValue().toString()); //저장확인!
//                        if(beaconType == 2){
//                            stampBeaconNumber = Integer.parseInt(dataSnapshot.child("stampnum").getValue().toString());
//                        }
                    }catch (Exception e){ //받은 비컨의 Address가 파이어베이스 서버에 저장되어 있지 않은경우 예외가 발생할 수 있다.
                        //e.printStackTrace();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("getBeaconDistanceMeter", "getBeaconDistanceMeter Error");
                }
            });
            return beaconDistance;
        }catch (Exception e){
            Log.d("getBeaconDistanceMeter", "예외 발생----------------------------------");
        }
        return 0;
    }

    /*****************************************************************************************
     *  Function: updateBleDeviceList
     *
     *  Description
     *      - GUI Custom List View에 beacon 정보를 새롭게 추가하거나 업데이트 함
     *
     *****************************************************************************************/
    public void updateBleDeviceList(BleDeviceInfo item)
    {
        boolean foundItem = false;

        //Scan된 비컨 정보가 ArrayList에 있을 때
        //비컨의 거리 변동으로 인한 달라진 Rssi(KalmanFilter적용), distance, distance2를 update하고, timeout값 Default로 초기화 한다.
        for(int i=0; i<mArrayListBleDevice.size(); i++){
           // Log.i("updateBleDeviceList", item.getDevAddress() +" " + mArrayListBleDevice.get(i).getDevAddress());
            if(item.getDevAddress().equals(mArrayListBleDevice.get(i).getDevAddress()) ){
                foundItem = true;

                //timeout시간을 다시 Default값으로 초기화
                mArrayListBleDevice.get(i).setTimeout(item.getTimeout());
                mArrayListBleDevice.get(i).setRssi((int) item.rssiKalmanFileter.update(item.rssi)); //rssi값 kalmanfilter 적용하여 보정
                mArrayListBleDevice.get(i).setDistance(mBleUtils.getDistance(item.getRssi(), item.getTxPower()));
                mArrayListBleDevice.get(i).setDistance2(mBleUtils.getDistance_20150515(item.getRssi(), item.getTxPower()));

            }
        }
        //Scan된 비컨 정보가 ArrayList에 없을 때
        //팝업을 띄우고 BeaconListInfo에 추가한다
        //그리고 파이어베이스 Storage에서 해당 비컨 address에 맞는 이미지를 다운받아서 팝업 액티비티를 활성화한다.
        if(!foundItem){
           // String content_title = getBeaconContentTitle(item.getDevAddress());
            mArrayListBleDevice.add(item);

            //어떤 비콘인지 확인함수 ------------------------------------------------------------------------전역이 아니라 에러발생가능성
            beaconType = getBeaconType(item);

//            //스탬프용 비콘이면
//            if(beaconType == 2) {
//             //   showBeaconStamp(item);
//                bringStampList();
//            }else if(beaconType == 1){ // 정보 비콘이면
//                showBeaconPopupImage(item);
//            }else{
//                Toast.makeText(getApplicationContext(), "BeaconType 3! 에러발생", Toast.LENGTH_SHORT).show();
//            }

            showBeaconPopupImage(item);
            bringStampList();
        }
    }

    /*****************************************************************************************
     *  Function: showBeaconPopupImage
     *
     *  Description
     *      - 해당 비컨에 저장된 이미지를 다운받아서 비트맵으로 변환 후 PopupActivity로 전달
     *
     *****************************************************************************************/
    void showBeaconPopupImage(final BleDeviceInfo item){

        StorageReference islandRef = storageReference.child("Beacon/"+item.getDevAddress()+".JPG");
        final String DevAddress  = item.getDevAddress();
        final long ONE_MEGABYTE = 1024 * 1024;

        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) { //다운로드 성공시 이미지를 byte[]로 리턴
                // Data for "images/island.jpg" is returns, use this as needed

                try {
                    Log.i("onSuccess", DevAddress);
                    Intent popupIntent = new Intent(BeaconScanService.this, PopupActivity.class);
                    popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    popupIntent.putExtra("textBdAddress", DevAddress);
                    popupIntent.putExtra("image", bytes); //다운로드한 byte[] 이미지

                    //Log.i("팝업이 뜬 비컨 Address", "--------------------------" +item.devAddress);
                    startActivity(popupIntent);
                }catch (Exception e){
                    e.printStackTrace();
                }

                //Toast.makeText(BeaconScanService.this, DevAddress, Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.i("onFailure", DevAddress);
            }
        });
    }

    //FIREBASE에서 사용자의 stamplist를 가져온다.
    //stamplist가 존재하지 않을 경우 리스트에 stampNum을 바로 저장한다.
    private void bringStampList(){
        try {
            DatabaseReference myRef = databaseReference.child("User").child(userUid).child("stamplist");
            Log.i("stampList", "************** "+userUid+ "bringStampList" );
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        stampList = Integer.parseInt(dataSnapshot.getValue().toString());

                        Log.i("stampList", "**************  "+stampList);
                        //받은 스탬프 넘버를 더하는 알고리즘 (중복 저장 방지)
                        if(stampBeaconNumber == 1 && (stampList % 10) == 1) {
                            stampList = stampList + 1;
                            Log.i("stampList", "**************  " + stampList);
                        }
                        else if(stampBeaconNumber == 2 && (stampList % 100) < 20) {
                            stampList = stampList + 10;
                            Log.i("stampList", "**************  " + stampList);
                        }
                        else if(stampBeaconNumber == 3 && (stampList % 1000) < 200) {
                            stampList = stampList + 100;
                            Log.i("stampList", "**************  " + stampList);
                        }
                        else if(stampBeaconNumber == 4 && (stampList % 10000) < 2000) {
                            stampList = stampList + 1000;
                            Log.i("stampList", "**************  " + stampList);
                        }
                        else if(stampBeaconNumber == 5 && (stampList % 100000) < 20000) {
                            stampList = stampList + 10000;
                            Log.i("stampList", "**************  " + stampList);
                        }
                        else if(stampBeaconNumber == 6 && (stampList) < 200000) {
                            stampList = stampList + 100000;
                            Log.i("stampList", "**************  " + stampList);
                        }

                        //임시처리 스탬프 번호를 받아왔을때만 실행한다.
                        if(stampBeaconNumber != 0) {
                            //더한 stampList를 파이어베이스 서버에 저장하는 함수
                            DatabaseReference myRef = databaseReference.child("User").child(userUid).child("stamplist");
                            myRef.setValue(stampList);
//                       //Log.i("beaconType", "**********************"+beaconType);

                            //Stamp획득시 notification 알림!
                            PendingIntent mPendingIntent = PendingIntent.getActivity(BeaconScanService.this,
                                    0,
                                    new Intent(getApplicationContext(), StampActivity.class),
                                    PendingIntent.FLAG_CANCEL_CURRENT
                            );

                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(BeaconScanService.this)
                                    .setSmallIcon(R.drawable.stamp)
                                    .setContentTitle("STAMP 획득!!")
                                    .setDefaults(Notification.DEFAULT_SOUND) //노티 발생시 소리나기
                                    .setContentIntent(mPendingIntent)
                                    .setContentText(stampBeaconNumber + "번 STAMP를 획득하셨습니다!");


                            NotificationManager mNotificationManger = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            mNotificationManger.notify(0, mBuilder.build());
                        }


                    }catch (Exception e){ //받은 비컨의 Address가 파이어베이스 서버에 저장되어 있지 않은경우 예외가 발생할 수 있다.
                        e.printStackTrace();
                        int init = 111111;
                        DatabaseReference myRef = databaseReference.child("User").child(userUid).child("stamplist");
                        myRef.setValue(init);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("getBeaconDistanceMeter", "getBeaconDistanceMeter Error");
                }
            });
            //stamplist가 저장 안되어 있는 경우. 즉 처음 저장된 스탬프인 경우
        }catch (Exception e){
            e.printStackTrace();

            Log.d("getBeaconDistanceMeter", "User정보가 존재하지 않습니다.");
        }
    }

    private int getBeaconType(BleDeviceInfo item){

        try {
            DatabaseReference myRef = databaseReference.child("Beacon").child(item.devAddress);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                          beaconType = Integer.parseInt(dataSnapshot.child("BEACON_CONTENT_TYPE").getValue().toString()); //저장확인!

                        if(beaconType == 2){
                            stampBeaconNumber = Integer.parseInt(dataSnapshot.child("stampnum").getValue().toString());
                        }
                    }catch (Exception e){ //받은 비컨의 Address가 파이어베이스 서버에 저장되어 있지 않은경우 예외가 발생할 수 있다.
                        //e.printStackTrace();
                    }
                    //Toast.makeText(BeaconScanService.this, "비컨타입 + 비컨 넘버" + beaconType+"  " + stampBeaconNumber, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("getBeaconDistanceMeter", "getBeaconDistanceMeter Error");
                }
            });
            return beaconType;
        }catch (Exception e){
            Log.d("getBeaconDistanceMeter", "예외 발생----------------------------------");
        }
        return 3;
    }

}

//    String getBeaconContentTitle(String devAddress){
//        try {
//            DatabaseReference myRef = databaseReference.child("Beacon").child(devAddress);
//            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    try {
//                        beaconContentTitle =(dataSnapshot.child("Title").getValue().toString());
//
//                    }catch (Exception e){ //받은 비컨의 Address가 파이어베이스 서버에 저장되어 있지 않은경우 예외가 발생할 수 있다.
//                        //e.printStackTrace();
//
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    Log.d("getBeaconDistanceMeter", "getBeaconDistanceMeter Error");
//                }
//            });
//
//            return beaconContentTitle;
//        }catch (Exception e){
//            Log.d("getBeaconDistanceMeter", "예외 발생----------------------------------");
//        }
//        return null;
//    }
