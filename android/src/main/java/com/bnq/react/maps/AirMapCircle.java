package com.bnq.react.maps;

import android.content.Context;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Overlay;

public class AirMapCircle extends AirMapFeature {

    private OverlayOptions circleOptions;
    private Overlay circle;

    private LatLng center;
    private double radius;
    private int strokeColor;
    private int fillColor = 1;
    private int strokeWidth = 1;
    private float zIndex;

    public AirMapCircle(Context context) {
        super(context);
    }

    public void setCenter(LatLng center) {
        this.center = center;
        if (circle != null) {
            ((Circle) circle).setCenter(this.center);
        }
    }

    public void setRadius(double radius) {
        this.radius = radius;
        if (circle != null) {
            ((Circle) circle).setRadius((int)this.radius);
        }
    }

    public void setFillColor(int color) {
        this.fillColor = color;
        if (circle != null) {
            ((Circle) circle).setFillColor(color);
        }
    }

    public void setStrokeColor(int color) {
        this.strokeColor = color;
        Stroke stroke = new Stroke(strokeWidth, color);
        if (circle != null) {
            ((Circle) circle).setStroke(stroke);
        }
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = (int) width;
        Stroke stroke = new Stroke(strokeWidth, strokeColor);
        if (circle != null) {
            ((Circle) circle).setStroke(stroke);
        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (circle != null) {
            circle.setZIndex((int) zIndex);
        }
    }

    public OverlayOptions getCircleOptions() {
        if (circleOptions == null) {
            circleOptions = createCircleOptions();
        }
        return circleOptions;
    }

    private CircleOptions createCircleOptions() {
        CircleOptions options = new CircleOptions();
        options.center(center);
        options.radius((int) radius);
        options.fillColor(fillColor);

        Stroke stroke = new Stroke(strokeWidth, strokeColor);
        options.stroke(stroke);
        options.zIndex((int) zIndex);
        return options;
    }

    @Override
    public Object getFeature() {
        return circle;
    }

    @Override
    public void addToMap(BaiduMap map) {
        circle = map.addOverlay(getCircleOptions());
    }

    @Override
    public void removeFromMap(BaiduMap map) {
        circle.remove();
    }
}
