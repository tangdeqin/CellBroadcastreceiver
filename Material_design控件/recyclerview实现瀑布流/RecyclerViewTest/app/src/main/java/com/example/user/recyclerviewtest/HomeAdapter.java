package com.example.user.recyclerviewtest;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 19-3-19.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
    private List<String> mList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private List<Integer> mHights ;
    public HomeAdapter(Context context,List<String> list){
        this.mContext = context;
        this.mList = list;
    }
    public interface OnItemClickListener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view,int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public void removeData(int position){//position 在调用onBindViewHolder操作是确定
        mList.remove(position);
        notifyItemRemoved(position);//显示动画效果
        notifyItemRangeChanged(0,mList.size());//调用onBindViewHolder

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
        View view= LayoutInflater.from(
                mContext).inflate(R.layout.item_recycler,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder,final int position){
        holder.tv.setText(mList.get(position));
        mHights = new ArrayList<Integer>();
        for (int i =0;i<mList.size();i++){
            mHights.add((int)(100+Math.random()*300));
        }
        ViewGroup.LayoutParams lp = holder.tv.getLayoutParams();
        lp.height = mHights.get(position);
        holder.tv.setLayoutParams(lp);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener!=null){
                    mOnItemClickListener.onItemClick(v,holder.getAdapterPosition());
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mOnItemClickListener!=null){
                    mOnItemClickListener.onItemLongClick(v,holder.getAdapterPosition());
                }
                return false;
            }
        });
    }
}
