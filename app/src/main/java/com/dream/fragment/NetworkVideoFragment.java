package com.dream.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dream.lantutv.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkVideoFragment extends Fragment {


    public NetworkVideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("网络视频页面初始化");
        return inflater.inflate(R.layout.fragment_network_video, container, false);
    }

}
