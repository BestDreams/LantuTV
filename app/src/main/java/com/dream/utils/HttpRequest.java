package com.dream.utils;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/5.
 */

public class HttpRequest {

    private RequestQueue requestQueue;

    public HttpRequest(Context context) {
        requestQueue= Volley.newRequestQueue(context);
    }

    public void sendVideoRequest(int method, String url, final OnRequestFinish requestFinish){
        StringRequest stringRequest=new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                requestFinish.success(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                requestFinish.error(volleyError.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Channel-Code", "official");
                headers.put("X-Client-Agent", "Xiaomi");
                headers.put("X-Client-Hash", "2f3d6ffkda95dlz2fhju8d3s6dfges3t");
                headers.put("X-Client-ID", "123456789123456");
                headers.put("X-Client-Version", "2.3.2");
                headers.put("X-Long-Token", "");
                headers.put("X-Platform-Type", "0");
                headers.put("X-Platform-Version", "5.0");
                headers.put("X-Serial-Num", "1492140134");
                headers.put("X-User-ID", "");
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void sendRequest(int method, String url, final Map<String,String> params, final OnRequestFinish requestFinish){
        StringRequest stringRequest=new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                requestFinish.success(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                requestFinish.error(volleyError.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    public interface OnRequestFinish{
        void success(String response);
        void error(String error);
    }

    public void close(){
        requestQueue.stop();
    }

}