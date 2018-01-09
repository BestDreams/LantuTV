package com.dream.fragment;


import android.support.v4.app.Fragment;
import android.view.View;

import com.dream.base.BaseFragment;
import com.dream.lantutv.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends BaseFragment {


    public MusicFragment() {
        // Required empty public constructor
    }

    @Override
    public View initView() {
        System.out.println("初始化本地音乐");
        View view=View.inflate(activity,R.layout.fragment_music,null);
        return view;
    }

    @Override
    public void initData() {
    }
}
