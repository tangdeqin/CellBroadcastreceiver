package com.example.user.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by user on 19-4-4.
 */

public class  FragmentAdapter extends FragmentStatePagerAdapter{
    private List<Fragment> fragments;
    private List<String> titles;
    public FragmentAdapter(FragmentManager fm,List<Fragment> fr,List<String> ti){
        super(fm);
        fragments =fr;
        titles=ti;
    }
    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
    @Override
    public CharSequence getPageTitle(int position){
        return titles.get(position);
    }
}