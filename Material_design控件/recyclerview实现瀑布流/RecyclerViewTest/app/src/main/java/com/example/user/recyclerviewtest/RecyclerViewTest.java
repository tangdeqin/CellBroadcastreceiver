package com.example.user.recyclerviewtest;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        RecyclerView mRecyclerView = findViewById(R.id.id_recyclerview);
        //设置布局管理器
     /*   LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);*/
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,4));
        //设置item增加和删除时的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //设置适配器
        List<String> mList = new ArrayList<>();
       for (int i= 1 ; i<48; i++){
           String s = i+"";
           mList.add(s);
       }
        final HomeAdapter mHomeAdapter = new HomeAdapter(this,mList);
        mRecyclerView.setAdapter(mHomeAdapter);
        mHomeAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(RecyclerViewTest.this,"点击第"+(position+1)+"条",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                new AlertDialog.Builder(RecyclerViewTest.this).
                        setTitle("删除吗？").
                        setNegativeButton("canel",null)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                mHomeAdapter.removeData(position);
                            }
                        }).show();
            }
        });
        //设置分割线
        /*mRecyclerView.addItemDecoration(new DividerItemDecoration(RecyclerViewTest.this,
                                        DividerItemDecoration.V));*/
        mRecyclerView.addItemDecoration(new DividerGridItemDecoration(this));

    }
}
