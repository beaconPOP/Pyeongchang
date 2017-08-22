package com.zerobin.www.beacon_client;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

/**
 * Created by Byun YB on 2017-05-13.
 */

public class FirebaseUtils {
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference = firebaseStorage.getReference();

    Uri downloadUrl;
    //static HashMap<String, Object> map = new HashMap<String, Object>();


    void DatabaseInsert(String root, String childKey, User object){
        databaseReference.child(root).child(childKey).setValue(object);
//        Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
//        hashtable.put("user", object);
//        databaseReference.child(root).child(childKey).updateChildren(hashtable);

    }

    void insertCoupon(String[] path, Object object){
        HashMap<String,Object> hashMap = new HashMap<String,Object>();
        for(int i=0; i<path.length; i++){
            databaseReference = databaseReference.child(path[i]);
        }
        databaseReference.updateChildren((HashMap)object);
    }

    void couponSave(String root, String childKey, byte[] image, String url) {//root name, userUid, 이미지 고유값
        StorageReference mountainsRef = storageReference.child(root).child(childKey).child(url);

        UploadTask uploadTask = mountainsRef.putBytes(image);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() { //이미지 업로드가 성공했을 때 실행
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d("url", String.valueOf(downloadUrl)); //올린 사진의 uri를 로그로 보여준다.
                Log.i("사진 업로드가 완료되었습니다.", downloadUrl+"----------------------------------------------------");

            }
        });
    }
}
