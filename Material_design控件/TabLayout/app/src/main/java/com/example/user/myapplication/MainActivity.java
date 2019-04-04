package com.example.user.myapplication;

import  android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager=findViewById(R.id.viewpager);
        initViewPager();
    }

    private void initViewPager(){
        mTabLayout = findViewById(R.id.tabs);
        final List<String> titles = new ArrayList<>();
        titles.add("a");
        titles.add("b");
        titles.add("c");
        titles.add("d");
        titles.add("a");
        titles.add("b");
        titles.add("c");
        titles.add("d");
        titles.add("a");
        titles.add("b");
        titles.add("c");
        titles.add("d");
        titles.add("a");
        titles.add("b");
        titles.add("c");
        titles.add("d");
        for(int i=0;i<titles.size();i++){
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(i)));
        }
        final List<Fragment> fragments=new ArrayList<>();
        for(int i=0;i<titles.size();i++){
            ListFragment listFragment = new ListFragment();
            ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,titles);
            listFragment.setListAdapter(mAdapter);
            fragments.add(listFragment);
        }
        FragmentAdapter fragmentStatePagerAdapter =new FragmentAdapter(getSupportFragmentManager(),fragments,titles);
        mViewPager.setAdapter(fragmentStatePagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(fragmentStatePagerAdapter);
    }
}




