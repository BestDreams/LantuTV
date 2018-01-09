package com.dream.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.dream.utils.MyUtils;

import java.util.List;

/**
 * Created by Administrator on 2018/1/9.
 */

public class NetVideoViewpagerAdapter extends PagerAdapter {

    private List<View> viewList;
    private List<String> pagerTitleList;

    public NetVideoViewpagerAdapter(List<View> viewList,List<String> pagerTitleList) {
        this.viewList = viewList;
        this.pagerTitleList=pagerTitleList;
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view=viewList.get(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return MyUtils.strToSimple(pagerTitleList.get(position),"精选");
    }
}
