package com.bnq.react.maps;

import android.content.Context;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Overlay;
import java.util.ArrayList;
import java.util.List;

public class AirMapPolyline extends AirMapFeature {

    private OverlayOptions polylineOptions;
    private Overlay polyline;

    private List<LatLng> coordinates;
    private int color;
    private float width;
    private boolean geodesic;
    private float zIndex;

    public AirMapPolyline(Context context) {
        super(context);
    }

    public void setCoordinates(ReadableArray coordinates) {
        this.coordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableMap coordinate = coordinates.getMap(i);
            this.coordinates.add(i,
                    new LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
        }
        if (polyline != null) {
            ((Polyline) polyline).setPoints(this.coordinates);
        }
    }

    public void setColor(int color) {
        this.color = color;
        if (polyline != null) {
            ((Polyline) polyline).setColor(color);
        }
    }

    public void setWidth(float width) {
        this.width = width;
        if (polyline != null) {
            ((Polyline) polyline).setWidth((int) width);
        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (polyline != null) {
            polyline.setZIndex((int) zIndex);
        }
    }

    public void setGeodesic(boolean geodesic) {
        this.geodesic = geodesic;
//        if (polyline != null) {
//            polyline.setGeodesic(geodesic);
//        }
    }

    public OverlayOptions getPolylineOptions() {
        if (polylineOptions == null) {
            polylineOptions = createPolylineOptions();
        }
        return polylineOptions;
    }

    private PolylineOptions createPolylineOptions() {
        PolylineOptions options = new PolylineOptions();
        options.points(coordinates);
        options.color(color);
        options.width((int) width);
//        options.geodesic(geodesic);
        options.zIndex((int) zIndex);
        return options;
    }

    @Override
    public Object getFeature() {
        return polyline;
    }

    @Override
    public void addToMap(BaiduMap map) {
        polyline = map.addOverlay(getPolylineOptions());
    }

    @Override
    public void removeFromMap(BaiduMap map) {
        polyline.remove();
    }
}
