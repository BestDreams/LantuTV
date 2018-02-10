package com.dream.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.dream.adapter.NetVideoListviewAdapter;
import com.dream.adapter.NetVideoViewpagerAdapter;
import com.dream.base.BaseFragment;
import com.dream.bean.NetVideo;
import com.dream.lantutv.R;
import com.dream.lantutv.SystemVideoPlayer;
import com.dream.utils.Config;
import com.dream.utils.HttpRequest;
import com.dream.utils.MyUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkVideoFragment extends BaseFragment {


    private static final int MSG_SHOW_LIST = 0;
    private static final int MSG_ERROR_NET = 1;
    private static final int MSG_ERROR_DATA = 2;
    private static final int MSG_ERROR_EMPTY = 3;
    private static final String STR_NETVIDEO_CACHE = "netvideoCache";

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
    /**
     * 是否是缓存数据
     */
    private boolean isCacheData=false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public NetworkVideoFragment() {
    }

    private TabLayout netvideoTablayout;
    private RelativeLayout netvideoLoading;
    private ViewPager netVideoViewPager;
    private LinearLayout netvideoUi;
    private RelativeLayout netvideoError;
    private TextView netvideoErrorInfo;

    @Override
    public View initView() {
        System.out.println("初始化网络视频");
        View view=View.inflate(getActivity(),R.layout.fragment_netvideo,null);
        netvideoTablayout = (TabLayout) view.findViewById(R.id.netvideo_tablayout);
        netvideoTablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        netvideoLoading = (RelativeLayout) view.findViewById(R.id.netvideo_loading);
        netVideoViewPager = (ViewPager) view.findViewById(R.id.netVideoViewPager);
        netvideoUi = (LinearLayout) view.findViewById(R.id.netvideo_ui);
        netvideoError = (RelativeLayout) view.findViewById(R.id.netvideo_error);
        netvideoErrorInfo = (TextView) view.findViewById(R.id.netvideo_error_info);
        netvideoUi.setVisibility(View.GONE);
        netvideoLoading.setVisibility(View.VISIBLE);
        initData();
        initListener();
        return view;
    }

    @Override
    public void initData() {
        sharedPreferences = getActivity().getSharedPreferences("app", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();
        httpRequest=new HttpRequest(getActivity());
        netVideoViewList=new ArrayList<>();
        pagerTitleList=new ArrayList<>();
        //LoadNetVideoData();
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
                if (isCacheData){
                    Toast.makeText(getActivity(),"请连接网络后重试",Toast.LENGTH_SHORT).show();
                }else{
                    setPlayerDataSource(netVideoNodeMap.get(currNodeName).get(position));
                }
            }
        });
        return view;
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SHOW_LIST:
                    //数据缓存
                    editor.putString(STR_NETVIDEO_CACHE,MyUtils.mapToString(netVideoNodeMap)).commit();
                    inputUI();
                    break;
                case MSG_ERROR_DATA:
                    requestDataException("解析失败");
                    break;
                case MSG_ERROR_NET:
                    requestDataException("网络错误");
                    break;
                case MSG_ERROR_EMPTY:
                    requestDataException("数据异常");
                    break;
            }
        }
    };

    /**
     * 获取头条视频列表
     */
    public void LoadNetVideoData(){
        netVideoNodeMap=new HashMap<>();
        httpRequest.sendVideoRequest(Request.Method.GET, Config.CONFIG_URL_VIDEO_HOME, new HttpRequest.OnRequestFinish() {
            @Override
            public void success(String response) {
                try {
                    System.out.println(response);
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
                        handler.sendEmptyMessage(MSG_ERROR_EMPTY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(MSG_ERROR_DATA);
                }
            }

            @Override
            public void error(String error) {
                String cache = sharedPreferences.getString(STR_NETVIDEO_CACHE, "");
                if (!cache.equals("")){
                    isCacheData=true;
                    netVideoNodeMap=MyUtils.stringToMap(cache);;
                    inputUI();
                }else{
                    handler.sendEmptyMessage(MSG_ERROR_NET);
                }
            }
        });
    }

    /**
     * 获取数据异常处理
     */
    public void requestDataException(String info){
        netvideoLoading.setVisibility(View.GONE);
        netvideoUi.setVisibility(View.GONE);
        netvideoError.setVisibility(View.VISIBLE);
        netvideoErrorInfo.setText(info);
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
        httpRequest.sendVideoRequest(Request.Method.GET, Config.CONFIG_URL_VIDEO_SINGLE_ID + contId, new HttpRequest.OnRequestFinish() {
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
        netvideoUi.setVisibility(View.VISIBLE);
        netvideoLoading.setVisibility(View.GONE);
        pagerTitleList.clear();
        for(Map.Entry<String,List<NetVideo>> entry : netVideoNodeMap.entrySet()){
            pagerTitleList.add(entry.getKey());
            netVideoViewList.add(initNetVideoVidew(entry.getValue()));
        }
        netVideoViewPager.setAdapter(new NetVideoViewpagerAdapter(netVideoViewList,pagerTitleList));
        netvideoTablayout.setupWithViewPager(netVideoViewPager);
    }


    /**
     * 判断请求数据是否完成
     * @param total 总数据量
     * @param index 当前位置
     */
    public void onUriDataLoadingFinsh(int total,int index){
        if (index==total){
            handler.sendEmptyMessage(MSG_SHOW_LIST);
        }
    }

    @Override
    public void onDestroy() {
        if (httpRequest!=null){
            httpRequest.close();
        }
        if (handler!=null){
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
