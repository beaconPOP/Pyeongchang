package com.zerobin.www.beacon_client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Byun YB on 2017-04-24.
 */

public class ComunicationActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    //    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayoutManager mLayoutManager;
    //
    private StorageReference mStorageRef;
    //
    int [] ImageId = { R.drawable.a1, R.drawable.a3, R.drawable.a5 };
    ImageView iv;

    TextView ID;
    EditText etText;
    Button btnSend;
    List<Chat> mChat;
    private String numimg="aa";
    String userEmail;


    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comunication_activity);

        database = FirebaseDatabase.getInstance();
        //
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mLayoutManager);

        //  mLayoutManager= new mGridLayoutManager(this);
        //  mRecyclerView.setLayoutManager(mGridLayoutManager);

        mChat = new ArrayList<>();
        mAdapter = new MyAdapter(mChat);
        mRecyclerView.setAdapter(mAdapter);

        //저장된 userUid 가져오기
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        userEmail = sharedPreferences.getString("userEmail","userEmail을 가져오지 못했습니다.");


        ID= (TextView) findViewById(R.id.etTextOn);
        etText=(EditText) findViewById(R.id.etText);
        btnSend=(Button) findViewById(R.id.btnSend);

        iv=(ImageView) findViewById(R.id.image);
        iv.setImageResource(R.drawable.a1);
        ID.setText(userEmail);

        iv.setOnClickListener(new View.OnClickListener() {
            int i= 1;
            int length = ImageId.length;

            @Override
            public void onClick(View v) {

                if(i==0) {numimg="aa";}
                else if(i==1) {numimg="bb";}
                else if(i==2) {numimg="cc";}

                iv.setImageResource(ImageId[i]);
                i+=1;
                if(i == ImageId.length) i = 0;

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String stText=etText.getText().toString();
                //
                String idText=ID.getText().toString();
                //
                String num_img="0";

                if(stText.equals("") || stText.isEmpty() || idText.equals("") || idText.isEmpty())
                {
                    //Toast.makeText(ComunicationActivity.this, "Input Id & Content", Toast.LENGTH_SHORT).show();
                }else
                {
                    //Toast.makeText(ComunicationActivity.this, stText, Toast.LENGTH_SHORT).show();

                    Calendar c = Calendar.getInstance();
                    System.out.println("Current time => " + c.getTime());

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());


                    DatabaseReference myRef = database.getReference("Chat3").child(formattedDate);

                    Hashtable<String, String> chat = new Hashtable<String, String>();
                    chat.put("Content", stText);
                    chat.put("UserID", idText+"  ");
                    chat.put("Img_num", numimg);

                    myRef.setValue(chat);


                }
            }
        });

        DatabaseReference myRef = database.getReference("Chat3");
        myRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Chat chat = dataSnapshot.getValue(Chat.class);

                // TChat tchat = dataSnapshot.getValue(TChat.class);
                // chat.setImg();


                mChat.add(chat);
                mAdapter.notifyItemInserted(mChat.size() - 1);

                // 리사이클러뷰 스크롤 기능 잘 정리되어 있는 블로그!!!*****************************************************************
                // https://battleshippark.wordpress.com/2016/09/20/recyclerview-and-scroll/
                int itemTotalCount = mRecyclerView.getAdapter().getItemCount() -1 ;
                mRecyclerView.scrollToPosition(itemTotalCount);
                // *********************************************************************************************************************
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });




    }
}
