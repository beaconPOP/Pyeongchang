package com.zerobin.www.beacon_client;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.beaconpop.pyeongchang.R;

import java.util.List;

import static com.beaconpop.pyeongchang.R.layout.my_text_view;


/**
     * Created by seonyeong on 2017-05-05.
     */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private String[] mDataset;
    List<Chat> mChat;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImageView;
        public TextView removeView;
        public Button mButton;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.mTextView);
            mImageView = (ImageView) itemView.findViewById(R.id.mImageView);
            removeView = (TextView) itemView.findViewById(R.id.remove);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(List<Chat> mChat) {
        this.mChat = mChat;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(my_text_view, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.mTextView.setText("ID : "+mChat.get(position).UserID+"\n"+"\n"+mChat.get(position).Content);

        if(mChat.get(position).Img_num.equals("aa"))
            holder.mImageView.setImageResource(mChat.get(position).img1);
        else if(mChat.get(position).Img_num.equals("bb"))
            holder.mImageView.setImageResource(mChat.get(position).img2);
        else if(mChat.get(position).Img_num.equals("cc"))
            holder.mImageView.setImageResource(mChat.get(position).img3);
        else {
            holder.mImageView.setImageResource(mChat.get(position).img1);
            holder.mTextView.setText(mChat.get(position).Img_num);
        }

        holder.removeView.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){

                mChat.remove(mChat.get(position));
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,mChat.size());


            }
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mChat.size();
    }
    }