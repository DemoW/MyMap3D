package com.gdut.water.mymap3d.morefunc;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.base.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MoreFuncFragment extends Fragment {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;


    public MoreFuncFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_more_func, container, false);
        init(rootView);

        return rootView;
    }

    private void init(View rootView) {
        //构造适配器
        List<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(new WeatherNowFragment());
        fragments.add(new LocationDetailsFragment());
        fragments.add(new DistrictFragment());

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager(),fragments);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }


}
