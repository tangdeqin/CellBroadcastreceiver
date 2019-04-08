package com.example.user.myapplication;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import  android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_launcher_background);
        ab.setDisplayHomeAsUpEnabled(true);
        mViewPager=findViewById(R.id.viewpager);
        mDrawerLayout = findViewById(R.id.dl_mian_drawer);
        NavigationView navigationView = findViewById(R.id.nv_main_navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                String title = item.getTitle().toString();
                Toast.makeText(getApplicationContext(),title,Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawers();
                return false;
            }
        });

        initViewPager();
    }
 @Override
 public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
     return super.onOptionsItemSelected(item);
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




