package com.zerobin.www.beacon_client;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Byun YB on 2017-05-21.
 */

public class CouponAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Bitmap> data;
    private int layout;
    private LayoutInflater inflater;

    CouponAdapter(Context context, ArrayList<Bitmap> data, int layout){
        this.context = context;
        this.data = data;
        this.layout = layout;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //보여줄 list 수
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){

            //여기서 inflater를 할 때 뒤에 false 옵션을 주지 않으면 예외가 떠서 실행이 안된다.
            convertView = inflater.inflate(layout, parent, false);
        }
        ImageView coupon = (ImageView) convertView.findViewById(R.id.listViewItemImage);

        coupon.setImageBitmap(data.get(position));

        return convertView;
    }
}
