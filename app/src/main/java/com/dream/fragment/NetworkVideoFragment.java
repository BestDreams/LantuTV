package com.dream.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.dream.adapter.NetVideoAdapter;
import com.dream.base.BaseFragment;
import com.dream.bean.NetVideo;
import com.dream.lantutv.R;
import com.dream.lantutv.SystemVideoPlayer;
import com.dream.utils.HttpRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.MyToast;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkVideoFragment extends BaseFragment {


    private static final int MSG_SHOW_LIST = 0;
    private HttpRequest httpRequest;
    private String url_single_id="http://app.pearvideo.com/clt/jsp/v2/content.jsp?contId=";
    private String url_home="http://app.pearvideo.com/clt/jsp/v2/home.jsp?lastLikeIds=1063871%2C1063985%2C1064069%2C1064123%2C1064078%2C1064186%2C1062372%2C1064164%2C1064081%2C1064176%2C1064070%2C1064019";
    private ListView netVideoListview;
    private RelativeLayout netvideoLoading;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SHOW_LIST:
                    netvideoLoading.setVisibility(View.GONE);
                    netVideoListview.setAdapter(new NetVideoAdapter(getActivity(),netVideos));
                    break;
            }
        }
    };

    public NetworkVideoFragment() {
    }

    @Override
    public View initView() {
        View view=View.inflate(getActivity(),R.layout.fragment_netvideo,null);
        netVideoListview = (ListView) view.findViewById(R.id.netVideoListview);
        netvideoLoading = (RelativeLayout) view.findViewById(R.id.netvideo_loading);
        netvideoLoading.setVisibility(View.VISIBLE);
        MyToast.init(getActivity(),true,false);
        initData();
        initListener();
        return view;
    }

    private void initListener() {
        netVideoListview.setOnItemClickListener(new MyOnItemClickListener());
        handler.sendEmptyMessageDelayed(MSG_SHOW_LIST,5000);
    }

    @Override
    public void initData() {
        httpRequest=new HttpRequest(getActivity());

        getToutiaoList();

    }

    class MyOnItemClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setPlayerDataSource(netVideos.get(position));
        }
    }


    /**
     * 获取头条视频列表
     */
    private List<NetVideo> netVideos;
    public void getToutiaoList(){
        netVideos=new ArrayList<>();
        httpRequest.sendRequest(Request.Method.GET, url_home, new HttpRequest.OnRequestFinish() {
            @Override
            public void success(String response) {
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    if (resultCode.equals("1")){
                        JSONArray dataList = jsonObject.getJSONArray("dataList");
                        JSONArray touliao=new JSONObject(dataList.get(0).toString()).getJSONArray("contList");
                        for (int i = 0; i < touliao.length(); i++) {
                            getVideoById(new JSONObject(touliao.get(i).toString()).getString("contId"));
                        }
                    }else{
                        MyToast.warn("请求异常");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(String error) {

            }
        });
    }

    /**
     * 设置播放数据源并跳转
     */
    public void setPlayerDataSource(NetVideo netVideo){
        Intent intent=new Intent(getActivity(), SystemVideoPlayer.class);
        intent.setData(Uri.parse(netVideo.getVideo()));
        intent.putExtra("videoName",netVideo.getName());
        startActivity(intent);
    }

    /**
     * 通过Id获取视频信息
     * @param contId
     */
    public void getVideoById(String contId){
        httpRequest.sendRequest(Request.Method.GET, url_single_id+contId, new HttpRequest.OnRequestFinish() {
            @Override
            public void success(String response) {
                try {
                    netVideos.add(getStandardVideoByJson(response));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(String error) {

            }
        });
    }

    /**
     * 得到标准视频数据
     * @param json
     * @return
     */
    public NetVideo getStandardVideoByJson(String json){
        try {
            JSONObject jsonObject=new JSONObject(json);
            JSONObject content = jsonObject.getJSONObject("content");
            JSONArray videos = content.getJSONArray("videos");
            String contId = content.getString("contId");
            String name = content.getString("name");
            String pic = content.getString("pic");
            String praiseTimes = content.getString("praiseTimes");
            String duration = videos.getJSONObject(1).getString("duration");
            String video = videos.getJSONObject(1).getString("url");
            return new NetVideo(contId,name,pic,praiseTimes,duration,video);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
