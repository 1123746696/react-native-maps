package com.bnq.react.maps;

import android.content.Context;

import com.facebook.react.views.view.ReactViewGroup;
import com.baidu.mapapi.map.BaiduMap;

public abstract class AirMapFeature extends ReactViewGroup {
    public AirMapFeature(Context context) {
        super(context);
    }

    public abstract void addToMap(BaiduMap map);

    public abstract void removeFromMap(BaiduMap map);

    public abstract Object getFeature();
}
