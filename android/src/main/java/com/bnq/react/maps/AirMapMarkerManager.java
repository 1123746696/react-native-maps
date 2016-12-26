package com.bnq.react.maps;

import android.graphics.Color;
import android.graphics.Point;
import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.baidu.mapapi.map.Marker;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class AirMapMarkerManager extends ViewGroupManager<AirMapMarker> {

    public static final int SHOW_INFO_WINDOW = 1;
    public static final int HIDE_INFO_WINDOW = 2;

    private ReactContext reactContext;

    public AirMapMarkerManager() {
    }

    @Override
    public String getName() {
        return "AIRMapMarker";
    }

    @Override
    public AirMapMarker createViewInstance(ThemedReactContext context) {
        reactContext = context;
        return new AirMapMarker(context);
    }

    @ReactProp(name = "coordinate")
    public void setCoordinate(AirMapMarker view, ReadableMap map) {
        view.setCoordinate(map);
    }

    @ReactProp(name = "title")
    public void setTitle(AirMapMarker view, String title) {
        view.setTitle(title);
    }

    @ReactProp(name = "description")
    public void setDescription(AirMapMarker view, String description) {
        view.setSnippet(description);
    }

    // NOTE(lmr):
    // android uses normalized coordinate systems for this, and is provided through the
    // `anchor` property  and `calloutAnchor` instead.  Perhaps some work could be done
    // to normalize iOS and android to use just one of the systems.
//    @ReactProp(name = "centerOffset")
//    public void setCenterOffset(AirMapMarker view, ReadableMap map) {
//
//    }
//
//    @ReactProp(name = "calloutOffset")
//    public void setCalloutOffset(AirMapMarker view, ReadableMap map) {
//
//    }

    @ReactProp(name = "anchor")
    public void setAnchor(AirMapMarker view, ReadableMap map) {
        // should default to (0.5, 1) (bottom middle)
        double x = map != null && map.hasKey("x") ? map.getDouble("x") : 0.5;
        double y = map != null && map.hasKey("y") ? map.getDouble("y") : 1.0;
        view.setAnchor(x, y);
    }

    @ReactProp(name = "calloutAnchor")
    public void setCalloutAnchor(AirMapMarker view, ReadableMap map) {
        // should default to (0.5, 0) (top middle)
        double x = map != null && map.hasKey("x") ? map.getDouble("x") : 0.5;
        double y = map != null && map.hasKey("y") ? map.getDouble("y") : 0.0;
        view.setCalloutAnchor(x, y);
    }

    @ReactProp(name = "image")
    public void setImage(AirMapMarker view, @Nullable String source) {
        view.setImage(source);
    }
//    public void setImage(AirMapMarker view, ReadableMap image) {
//        view.setImage(image);
//    }

    @ReactProp(name = "pinColor", defaultInt = Color.RED, customType = "Color")
    public void setPinColor(AirMapMarker view, int pinColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(pinColor, hsv);
        // NOTE: android only supports a hue
        view.setMarkerHue(hsv[0]);
    }

    @ReactProp(name = "rotation", defaultFloat = 0.0f)
    public void setMarkerRotation(AirMapMarker view, float rotation) {
        view.setRotation(rotation);
    }

    @ReactProp(name = "flat", defaultBoolean = false)
    public void setFlat(AirMapMarker view, boolean flat) {
        view.setFlat(flat);
    }

    @ReactProp(name = "draggable", defaultBoolean = false)
    public void setDraggable(AirMapMarker view, boolean draggable) {
        view.setDraggable(draggable);
    }

    @Override
    public void addView(AirMapMarker parent, View child, int index) {
        // if an <Callout /> component is a child, then it is a callout view, NOT part of the
        // marker.
        if (child instanceof AirMapCallout) {
            parent.setCalloutView((AirMapCallout) child);
        } else {
            super.addView(parent, child, index);
            parent.update();
        }
    }

    @Override
    public void removeViewAt(AirMapMarker parent, int index) {
        super.removeViewAt(parent, index);
        parent.update();
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "showCallout", SHOW_INFO_WINDOW,
                "hideCallout", HIDE_INFO_WINDOW
        );
    }

    @Override
    public void receiveCommand(AirMapMarker view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case SHOW_INFO_WINDOW:
//                ((Marker) view.getFeature()).showInfoWindow();
                showWindow(view);
                break;

            case HIDE_INFO_WINDOW:
//                ((Marker) view.getFeature()).hideInfoWindow();
                hideWindow(view);
                break;
        }
    }

    private void showWindow(final AirMapMarker view) {
        final Marker marker = (Marker) view.getFeature();
        if (marker != null) {
            final BaiduMap map = view.getMap();
            if (map != null) {
                map.hideInfoWindow();
                InfoWindow infoWindow = new InfoWindow(
                        BitmapDescriptorFactory.fromView(
                                view.getInfoView()
                        ),
                        marker.getPosition(), 0,
                        new InfoWindow.OnInfoWindowClickListener()
                        {

                            @Override
                            public void onInfoWindowClick()
                            {
                                WritableMap event;
//
//                                event = makeClickEventData(marker.getPosition(), map);
//                                event.putString("action", "callout-press");
//                                pushEvent(mapview, "onCalloutPress", event);

                                event = makeClickEventData(marker.getPosition(), map);
                                event.putString("action", "callout-press");

                                pushEvent(view, "onCalloutPress", event);

                                event = makeClickEventData(marker.getPosition(), map);
                                event.putString("action", "callout-press");
                                AirMapCallout infoWindow = view.getCalloutView();
                                if (infoWindow != null) {
                                    pushEvent(infoWindow, "onPress", event);
                                }
                            }
                        });
                //显示InfoWindow
                map.showInfoWindow(infoWindow);
            }
        }
    }

    private void hideWindow(AirMapMarker view) {
        final BaiduMap map = view.getMap();
        if (map != null) {
            map.hideInfoWindow();
        }
    }

    public WritableMap makeClickEventData(LatLng point, BaiduMap map) {
        WritableMap event = new WritableNativeMap();

        WritableMap coordinate = new WritableNativeMap();
        coordinate.putDouble("latitude", point.latitude);
        coordinate.putDouble("longitude", point.longitude);
        event.putMap("coordinate", coordinate);

        Projection projection = map.getProjection();
        Point screenPoint = projection.toScreenLocation(point);

        WritableMap position = new WritableNativeMap();
        position.putDouble("x", screenPoint.x);
        position.putDouble("y", screenPoint.y);
        event.putMap("position", position);

        return event;
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map map = MapBuilder.of(
                "onPress", MapBuilder.of("registrationName", "onPress"),
                "onCalloutPress", MapBuilder.of("registrationName", "onCalloutPress"),
                "onDragStart", MapBuilder.of("registrationName", "onDragStart"),
                "onDrag", MapBuilder.of("registrationName", "onDrag"),
                "onDragEnd", MapBuilder.of("registrationName", "onDragEnd")
        );

        map.putAll(MapBuilder.of(
                "onDragStart", MapBuilder.of("registrationName", "onDragStart"),
                "onDrag", MapBuilder.of("registrationName", "onDrag"),
                "onDragEnd", MapBuilder.of("registrationName", "onDragEnd")
        ));

        return map;
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        // we use a custom shadow node that emits the width/height of the view
        // after layout with the updateExtraData method. Without this, we can't generate
        // a bitmap of the appropriate width/height of the rendered view.
        return new SizeReportingShadowNode();
    }

    @Override
    public void updateExtraData(AirMapMarker view, Object extraData) {
        // This method is called from the shadow node with the width/height of the rendered
        // marker view.
        HashMap<String, Float> data = (HashMap<String, Float>) extraData;
        float width = data.get("width");
        float height = data.get("height");
        view.update((int) width, (int) height);
    }

    void pushEvent(View view, String name, WritableMap data) {
        reactContext.getJSModule(RCTEventEmitter.class)
                .receiveEvent(view.getId(), name, data);
    }
}