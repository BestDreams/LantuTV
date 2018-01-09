package com.dream.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.dream.adapter.NetVideoListviewAdapter;
import com.dream.adapter.NetVideoViewpagerAdapter;
import com.dream.base.BaseFragment;
import com.dream.bean.NetVideo;
import com.dream.lantutv.R;
import com.dream.lantutv.SystemVideoPlayer;
import com.dream.utils.HttpRequest;
import com.dream.view.NoTouchViewPager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.MyToast;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkVideoFragment extends BaseFragment {


    private static final int MSG_SHOW_LIST = 0;
    private static final int MSG_SHOW_TAB = 1;
    private String url_single_id="http://app.pearvideo.com/clt/jsp/v2/content.jsp?contId=";
    private String url_home="http://app.pearvideo.com/clt/jsp/v2/home.jsp?lastLikeIds=1063871%2C1063985%2C1064069%2C1064123%2C1064078%2C1064186%2C1062372%2C1064164%2C1064081%2C1064176%2C1064070%2C1064019";

    /**
     * Http请求类
     */
    private HttpRequest httpRequest;
    /**
     * 视频分类节点集合
     */
    private Map<String,List<NetVideo>> netVideoNodeMap;
    /**
     * 当前节点名
     */
    private String currNodeName;
    /**
     * ViewPager视图集合
     */
    private List<View> netVideoViewList;
    /**
     * 页面标题集合
     */
    private List<String> pagerTitleList;

    public NetworkVideoFragment() {
    }

    private TabLayout netvideoTablayout;
    private RelativeLayout netvideoLoading;
    private ViewPager netVideoViewPager;
    @Override
    public View initView() {
        System.out.println("初始化网络视频");
        View view=View.inflate(getActivity(),R.layout.fragment_netvideo,null);
        netvideoTablayout = (TabLayout) view.findViewById(R.id.netvideo_tablayout);
        netvideoTablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        netvideoLoading = (RelativeLayout) view.findViewById(R.id.netvideo_loading);
        netVideoViewPager = (ViewPager) view.findViewById(R.id.netVideoViewPager);
        netVideoViewPager.setVisibility(View.GONE);
        netvideoLoading.setVisibility(View.VISIBLE);
        MyToast.init(getActivity(),true,false);
        initData();
        initListener();
        return view;
    }

    @Override
    public void initData() {
        httpRequest=new HttpRequest(getActivity());
        netVideoViewList=new ArrayList<>();
        pagerTitleList=new ArrayList<>();
        getToutiaoList();
    }

    private void initListener() {
        netvideoTablayout.setOnTabSelectedListener(new MyOnTabSelectedListener());
    }

    class MyOnTabSelectedListener implements TabLayout.OnTabSelectedListener{

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            currNodeName=pagerTitleList.get(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    /**
     * 初始化每个视频页面
     * @param netviewList
     */
    public View initNetVideoVidew(List<NetVideo> netviewList){
        View view=View.inflate(getActivity(),R.layout.netvideo_viewpager_item,null);
        ListView netVideoListview = (ListView) view.findViewById(R.id.netVideoListview);
        netVideoListview.setAdapter(new NetVideoListviewAdapter(getActivity(),netviewList));
        netVideoListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setPlayerDataSource(netVideoNodeMap.get(currNodeName).get(position));
            }
        });
        return view;
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SHOW_LIST:
                    inputUI();
                    break;
            }
        }
    };

    /**
     * 获取头条视频列表
     */
    public void getToutiaoList(){
        netVideoNodeMap=new HashMap<>();
        httpRequest.sendRequest(Request.Method.GET, url_home, new HttpRequest.OnRequestFinish() {
            @Override
            public void success(String response) {
                try {
                    JSONObject jsonObject=new JSONObject(response);
                    String resultCode = jsonObject.getString("resultCode");
                    if (resultCode.equals("1")){
                        int total=0;
                        int currCount=0;
                        JSONArray dataList = jsonObject.optJSONArray("dataList");
                        if (dataList!=null&&dataList.length()>0){
                            for (int i = 0; i < dataList.length(); i++) {
                                JSONObject nodeListJson = new JSONObject(dataList.get(i).toString());
                                JSONArray contList=nodeListJson.optJSONArray("contList");
                                if (contList!=null&&contList.length()>0){
                                    for (int j = 0; j < contList.length(); j++) {
                                        if (contList!=null&&contList.length()>0){
                                            String nodeName = nodeListJson.getString("nodeName");
                                            if (!nodeName.contains("直播")){
                                                total++;
                                            }
                                        }
                                    }
                                }
                            }
                            for (int i = 0; i < dataList.length(); i++) {
                                JSONObject nodeListJson = new JSONObject(dataList.get(i).toString());
                                JSONArray contList=nodeListJson.optJSONArray("contList");
                                if (contList!=null&&contList.length()>0){
                                    String nodeName = nodeListJson.getString("nodeName");
                                    if (!nodeName.contains("直播")){
                                        netVideoNodeMap.put(nodeName,new ArrayList<NetVideo>());
                                        if (i==0){
                                            currNodeName=nodeName;
                                        }
                                        for (int j = 0; j < contList.length(); j++) {
                                            currCount++;
                                            getVideoById(total,currCount,nodeName,new JSONObject(contList.get(j).toString()).getString("contId"));
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        MyToast.warn("服务器异常，请重试");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void error(String error) {
                System.out.println(error);
                MyToast.warn("拉取数据失败，请重试");
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
    public void getVideoById(final int total, final int index, final String nodeName, String contId){
        httpRequest.sendRequest(Request.Method.GET, url_single_id + contId, new HttpRequest.OnRequestFinish() {
            @Override
            public void success(String response) {
                NetVideo standardVideoByJson = getStandardVideoByJson(response);
                if (standardVideoByJson!=null){
                    netVideoNodeMap.get(nodeName).add(standardVideoByJson);
                }
                onUriDataLoadingFinsh(total,index);
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
            JSONObject content = jsonObject.optJSONObject("content");
            JSONArray videos = content.optJSONArray("videos");
            String contId = content.optString("contId");
            String name = content.optString("name");
            String pic = content.optString("pic");
            String praiseTimes = content.optString("praiseTimes");
            String duration = videos.getJSONObject(0).optString("duration");
            String video = videos.getJSONObject(0).optString("url");
            return new NetVideo(contId,name,pic,praiseTimes,duration,video);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 填充界面
     */
    public void inputUI(){
        netVideoViewPager.setVisibility(View.VISIBLE);
        netvideoLoading.setVisibility(View.GONE);
        pagerTitleList.clear();
        for(Map.Entry<String,List<NetVideo>> entry : netVideoNodeMap.entrySet()){
            pagerTitleList.add(entry.getKey());
            netVideoViewList.add(initNetVideoVidew(entry.getValue()));
        }
        netVideoViewPager.setAdapter(new NetVideoViewpagerAdapter(netVideoViewList,pagerTitleList));
        netvideoTablayout.setupWithViewPager(netVideoViewPager);
    }


    public void onUriDataLoadingFinsh(int total,int index){
        if (index==total){
            handler.sendEmptyMessage(MSG_SHOW_LIST);
        }
    }

    @Override
    public void onDestroy() {
        httpRequest.close();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
