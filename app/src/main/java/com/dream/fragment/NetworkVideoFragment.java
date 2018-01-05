package com.dream.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.dream.base.BaseFragment;
import com.dream.lantutv.R;
import com.dream.utils.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkVideoFragment extends BaseFragment {


    private HttpRequest httpRequest;
    private String url="http://app.pearvideo.com/clt/jsp/v2/content.jsp?contId=1064146";

    public NetworkVideoFragment() {
        httpRequest=new HttpRequest(getActivity());
    }

    @Override
    public View initView() {
        View view=View.inflate(getActivity(),R.layout.fragment_network_video,null);
        return view;
    }

    @Override
    public void initData() {
        httpRequest.sendRequest(Request.Method.GET, url, new HttpRequest.OnRequestFinish() {
            @Override
            public void success(String response) {
                try {
                    JSONObject jsonObject=new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(String error) {

            }
        });
    }
}
