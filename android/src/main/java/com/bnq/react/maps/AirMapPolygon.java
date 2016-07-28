package com.bnq.react.maps;

import android.content.Context;

import com.baidu.mapapi.map.Stroke;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Overlay;
import java.util.ArrayList;
import java.util.List;

public class AirMapPolygon extends AirMapFeature {

    private OverlayOptions polygonOptions;
    private Overlay polygon;

    private List<LatLng> coordinates;
    private int strokeColor;
    private int fillColor;
    private float strokeWidth;
    private boolean geodesic;
    private float zIndex;

    public AirMapPolygon(Context context) {
        super(context);
    }

    @Override
    public void removeFromMap(BaiduMap map) {
        polygon.remove();
    }

    public void setCoordinates(ReadableArray coordinates) {
        // it's kind of a bummer that we can't run map() or anything on the ReadableArray
        this.coordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableMap coordinate = coordinates.getMap(i);
            this.coordinates.add(i,
                    new LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
        }
        if (polygon != null) {
            ((Polygon) polygon).setPoints(this.coordinates);
        }
    }

    public void setFillColor(int color) {
        this.fillColor = color;
        if (polygon != null) {
            ((Polygon) polygon).setFillColor(color);
        }
    }

    public void setStrokeColor(int color) {
        this.strokeColor = color;
        Stroke stroke = new Stroke((int) strokeWidth, color);
        if (polygon != null) {
            ((Polygon) polygon).setStroke(stroke);
        }
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        Stroke stroke = new Stroke((int)strokeWidth, strokeColor);
        if (polygon != null) {
            ((Polygon) polygon).setStroke(stroke);
        }
    }

    public void setGeodesic(boolean geodesic) {
        this.geodesic = geodesic;
//        if (polygon != null) {
//            polygon.setGeodesic(geodesic);
//        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (polygon != null) {
            polygon.setZIndex((int) zIndex);
        }
    }

    public OverlayOptions getPolygonOptions() {
        if (polygonOptions == null) {
            polygonOptions = createPolygonOptions();
        }
        return polygonOptions;
    }

    private PolygonOptions createPolygonOptions() {
        PolygonOptions options = new PolygonOptions();
        options.points(coordinates);
        options.fillColor(fillColor);

        Stroke stroke = new Stroke((int)strokeWidth, strokeColor);
        options.stroke(stroke);
        //options.geodesic(geodesic);
        options.zIndex((int) zIndex);
        return options;
    }

    @Override
    public Object getFeature() {
        return polygon;
    }

    @Override
    public void addToMap(BaiduMap map) {
        polygon = map.addOverlay(getPolygonOptions());
    }
}
