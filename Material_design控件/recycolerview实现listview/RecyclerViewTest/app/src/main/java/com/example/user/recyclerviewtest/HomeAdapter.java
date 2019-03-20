package com.example.user.recyclerviewtest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 19-3-19.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
    private List<String> mList;
    private Context mContext;
    public HomeAdapter(Context context,List<String> list){
        this.mContext = context;
        this.mList = list;
    }

    public void removeData(int position){//position 在调用onBindViewHolder操作是确定
        mList.remove(position);
        notifyItemRemoved(position);//显示动画效果
        notifyItemRangeChanged(position,mList.size());//调用onBindViewHolder
    }


    public int getItemCount(){
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv;
        public MyViewHolder(View view){
            super(view);
            tv = view.findViewById(R.id.tv_item);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.item_recycler,parent,false));
        return holder;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder,final int position){
        holder.tv.setText(mList.get(position));
    }
}
