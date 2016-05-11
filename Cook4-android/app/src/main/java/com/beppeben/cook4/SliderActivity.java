package com.beppeben.cook4;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.beppeben.cook4.ui.PhotoFragment;
import com.beppeben.cook4.utils.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.List;


public class SliderActivity extends FragmentActivity {

    private ViewPager pager;
    public PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);
        Intent i = getIntent();
        long[] ids = i.getLongArrayExtra("ids");
        adapter = new SliderFragAdapter(getSupportFragmentManager(), ids);
        pager = (ViewPager) findViewById(R.id.slider_pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);
        pager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    private class SliderFragAdapter extends FragmentStatePagerAdapter {

        private List<Long> idList;

        public SliderFragAdapter(FragmentManager fm, long[] imageIds) {
            super(fm);
            idList = new ArrayList<Long>();
            for (int i = 0; i < imageIds.length; i++) idList.add(imageIds[i]);
        }

        @Override
        public Fragment getItem(int position) {
            return PhotoFragment.newInstance(idList.get(position));
        }

        @Override
        public int getCount() {
            return idList.size();
        }
    }

}
