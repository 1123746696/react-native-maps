package com.bnq.react.maps;

import com.baidu.mapapi.SDKInitializer;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;


/**
 * Created by user on 16/6/15.
 */
public class BMapManager extends ReactContextBaseJavaModule {

    public BMapManager(ReactApplicationContext reactContext) {
        super(reactContext);
    }
    @Override
    public String getName() {
        return "BMapManager";
    }

    /***
     * 设置baiduMap的Key
     * @param key     baiduMap的key
     */
    @ReactMethod
    public void start(String key) {//该key暂时没用，已经写死在AndroidManifest.xml里边
        SDKInitializer.initialize(getReactApplicationContext().getApplicationContext());
    }

}