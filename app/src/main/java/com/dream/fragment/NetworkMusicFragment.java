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
public class NetworkMusicFragment extends Fragment {


    public NetworkMusicFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("初始化网络音乐");
        return inflater.inflate(R.layout.fragment_netmusic, container, false);
    }

}
