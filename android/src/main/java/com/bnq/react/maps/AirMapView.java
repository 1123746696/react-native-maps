package com.bnq.react.maps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class AirMapView extends LinearLayout implements BaiduMap.OnMarkerDragListener,
        BaiduMap.OnMapLoadedCallback
{
    public BaiduMap map;
    private MapView mapView;

    private ProgressBar mapLoadingProgressBar;
    private RelativeLayout mapLoadingLayout;
    private ImageView cacheImageView;
    private Boolean isMapLoaded = false;
    private Integer loadingBackgroundColor = null;
    private Integer loadingIndicatorColor = null;

    private LatLngBounds boundsToMove;
    private boolean showUserLocation = false;
    private boolean isMonitoringRegion = false;
    private boolean isTouchDown = false;
    private boolean handlePanDrag = false;
    private boolean cacheEnabled = false;
    private boolean loadingEnabled = false;

    private static final String[] PERMISSIONS = new String[] {
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

    private final List<AirMapFeature> features = new ArrayList<>();
    private final Map<Marker, AirMapMarker> markerMap = new HashMap<>();
    private final Map<Polyline, AirMapPolyline> polylineMap = new HashMap<>();
    private final Map<Polygon, AirMapPolygon> polygonMap = new HashMap<>();
    private final Map<Circle, AirMapCircle> circleMap = new HashMap<>();

    private ScaleGestureDetector scaleDetector = null;
    private GestureDetectorCompat gestureDetector = null;
    private AirMapManager manager = null;
    private LifecycleEventListener lifecycleListener;
    private OnLayoutChangeListener onLayoutChangeListener;
    private boolean paused = false;
    private ThemedReactContext context;

    private EventDispatcher eventDispatcher = null;
    private int screenWidth = 0;
    private int screenHeight = 0;

    public AirMapView(Context context) {
        super(context);
        this.setLayoutParams(new ReactViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT
        ));

        mapView = new MapView(context);
        this.addView(mapView, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

        map = mapView.getMap();

        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public AirMapView(ThemedReactContext context, Context appContext, final AirMapManager manager) {
        this(context.getBaseContext());

        this.manager = manager;
        this.context = context;


        final AirMapView view = this;
        scaleDetector =
                new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        view.startMonitoringRegion();
                        return true; // stop recording this gesture. let mapview handle it.
                    }
                });

        gestureDetector =
                new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        view.startMonitoringRegion();
                        return false;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                            float distanceY) {
                        if (handlePanDrag) {
                            onPanDrag(e2);
                        }
                        view.startMonitoringRegion();
                        return false;
                    }
                });

        onLayoutChangeListener = new OnLayoutChangeListener() {
            @Override public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                                 int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (!AirMapView.this.paused) {
                    AirMapView.this.cacheView();
                }
            }
        };
        mapView.addOnLayoutChangeListener(onLayoutChangeListener);

        eventDispatcher = context.getNativeModule(UIManagerModule.class).getEventDispatcher();

//        map.setInfoWindowAdapter(this);
        map.setOnMarkerDragListener(this);

//        manager.pushEvent(this, "onMapReady", new WritableNativeMap());

        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                WritableMap event;

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                manager.pushEvent(view, "onMarkerPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                manager.pushEvent(markerMap.get(marker), "onPress", event);

                return false; // returning false opens the callout window, if possible
            }
        });

//        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//            @Override
//            public void onInfoWindowClick(Marker marker) {
//                WritableMap event;
//
//                event = makeClickEventData(marker.getPosition());
//                event.putString("action", "callout-press");
//                manager.pushEvent(view, "onCalloutPress", event);
//
//                event = makeClickEventData(marker.getPosition());
//                event.putString("action", "callout-press");
//                AirMapMarker markerView = markerMap.get(marker);
//                manager.pushEvent(markerView, "onCalloutPress", event);
//
//                event = makeClickEventData(marker.getPosition());
//                event.putString("action", "callout-press");
//                AirMapCallout infoWindow = markerView.getCalloutView();
//                if (infoWindow != null) manager.pushEvent(infoWindow, "onPress", event);
//            }
//        });

        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                WritableMap event = makeClickEventData(point);
                event.putString("action", "press");
                manager.pushEvent(view, "onPress", event);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

        map.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                WritableMap event = makeClickEventData(point);
                event.putString("action", "long-press");
                manager.pushEvent(view, "onLongPress", makeClickEventData(point));
            }
        });

//        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(CameraPosition position) {
//                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
//                LatLng center = position.target;
//                lastBoundsEmitted = bounds;
//                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), bounds, center, isTouchDown));
//                view.stopMonitoringRegion();
//            }
//        });

        map.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override public void onMapLoaded() {
                isMapLoaded = true;
                mapView.buildDrawingCache();
            }
        });

        // We need to be sure to disable location-tracking when app enters background, in-case some
        // other module
        // has acquired a wake-lock and is controlling location-updates, otherwise, location-manager
        // will be left
        // updating location constantly, killing the battery, even though some other location-mgmt
        // module may
        // desire to shut-down location-services.
        lifecycleListener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                if (hasPermissions()) {
                    //noinspection MissingPermission
                    map.setMyLocationEnabled(showUserLocation);
                }
                synchronized (AirMapView.this) {
                    mapView.onResume();
                    paused = false;
                }
            }

            @Override
            public void onHostPause() {
                if (hasPermissions()) {
                    //noinspection MissingPermission
                    map.setMyLocationEnabled(false);
                }
                synchronized (AirMapView.this) {
                    mapView.onPause();
                    paused = true;
                }
            }

            @Override
            public void onHostDestroy() {
                mapView.onDestroy();
                doDestroy();
            }
        };

        context.addLifecycleEventListener(lifecycleListener);
    }

    @Override
    public void onMapLoaded() {
        manager.pushEvent(this, "onMapReady", new WritableNativeMap());
    }

    private boolean hasPermissions() {
        return checkSelfPermission(mapView.getContext(), PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(mapView.getContext(), PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    /*
    onDestroy is final method so I can't override it.
     */
    public synchronized void doDestroy() {
        if (lifecycleListener != null) {
            context.removeLifecycleEventListener(lifecycleListener);
            lifecycleListener = null;
        }
    }

    public void setRegion(ReadableMap region) {
        if (region == null) return;

        Double lng = region.getDouble("longitude");
        Double lat = region.getDouble("latitude");
        Double lngDelta = region.getDouble("longitudeDelta");
        Double latDelta = region.getDouble("latitudeDelta");

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds = builder
                .include(new LatLng(lat - latDelta / 2, lng - lngDelta / 2))
                .include(new LatLng(lat + latDelta / 2, lng + lngDelta / 2))
                .build();

        if (mapView.getHeight() <= 0 || mapView.getWidth() <= 0) {
            // in this case, our map has not been laid out yet, so we save the bounds in a local
            // variable, and make a guess of zoomLevel 10. Not to worry, though: as soon as layout
            // occurs, we will move the camera to the saved bounds. Note that if we tried to move
            // to the bounds now, it would trigger an exception.
            map.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds));
            boundsToMove = bounds;
        } else {
            map.setMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds));
            boundsToMove = null;
        }
    }

    public void setShowsUserLocation(boolean showUserLocation) {
        this.showUserLocation = showUserLocation; // hold onto this for lifecycle handling
        if (hasPermissions()) {
            //noinspection MissingPermission
            map.setMyLocationEnabled(showUserLocation);
        }
    }

    public void setToolbarEnabled(boolean toolbarEnabled) {
        if (hasPermissions()) {
            map.getUiSettings().setScrollGesturesEnabled (toolbarEnabled);
        }
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        this.cacheView();
    }

    public void enableMapLoading(boolean loadingEnabled) {
        if (loadingEnabled && !this.isMapLoaded) {
            this.getMapLoadingLayoutView().setVisibility(View.VISIBLE);
        }
    }

    public void setLoadingBackgroundColor(Integer loadingBackgroundColor) {
        this.loadingBackgroundColor = loadingBackgroundColor;

        if (this.mapLoadingLayout != null) {
            if (loadingBackgroundColor == null) {
                this.mapLoadingLayout.setBackgroundColor(Color.WHITE);
            } else {
                this.mapLoadingLayout.setBackgroundColor(this.loadingBackgroundColor);
            }
        }
    }

    public void setLoadingIndicatorColor(Integer loadingIndicatorColor) {
        this.loadingIndicatorColor = loadingIndicatorColor;
        if (this.mapLoadingProgressBar != null) {
            Integer color = loadingIndicatorColor;
            if (color == null) {
                color = Color.parseColor("#606060");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList progressTintList = ColorStateList.valueOf(loadingIndicatorColor);
                ColorStateList secondaryProgressTintList = ColorStateList.valueOf(loadingIndicatorColor);
                ColorStateList indeterminateTintList = ColorStateList.valueOf(loadingIndicatorColor);

                this.mapLoadingProgressBar.setProgressTintList(progressTintList);
                this.mapLoadingProgressBar.setSecondaryProgressTintList(secondaryProgressTintList);
                this.mapLoadingProgressBar.setIndeterminateTintList(indeterminateTintList);
            } else {
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    mode = PorterDuff.Mode.MULTIPLY;
                }
                if (this.mapLoadingProgressBar.getIndeterminateDrawable() != null)
                    this.mapLoadingProgressBar.getIndeterminateDrawable().setColorFilter(color, mode);
                if (this.mapLoadingProgressBar.getProgressDrawable() != null)
                    this.mapLoadingProgressBar.getProgressDrawable().setColorFilter(color, mode);
            }
        }
    }

    public void setHandlePanDrag(boolean handlePanDrag) {
        this.handlePanDrag = handlePanDrag;
    }

    public void addFeature(View child, int index) {
        // Our desired API is to pass up annotations/overlays as children to the mapview component.
        // This is where we intercept them and do the appropriate underlying mapview action.
        if (child instanceof AirMapMarker) {
            AirMapMarker annotation = (AirMapMarker) child;
            annotation.addToMap(map);
            features.add(index, annotation);
            Marker marker = (Marker) annotation.getFeature();
            markerMap.put(marker, annotation);
        } else if (child instanceof AirMapPolyline) {
            AirMapPolyline polylineView = (AirMapPolyline) child;
            polylineView.addToMap(map);
            features.add(index, polylineView);
            Polyline polyline = (Polyline) polylineView.getFeature();
            polylineMap.put(polyline, polylineView);
        } else if (child instanceof AirMapPolygon) {
            AirMapPolygon polygonView = (AirMapPolygon) child;
            polygonView.addToMap(map);
            features.add(index, polygonView);
            Polygon polygon = (Polygon) polygonView.getFeature();
            polygonMap.put(polygon, polygonView);
        } else if (child instanceof AirMapCircle) {
            AirMapCircle circleView = (AirMapCircle) child;
            circleView.addToMap(map);
            features.add(index, circleView);
            Circle circle = (Circle) circleView.getFeature();
            circleMap.put(circle, circleView);
        } else {
            // TODO(lmr): throw? User shouldn't be adding non-feature children.
        }
    }

    public int getFeatureCount() {
        return features.size();
    }

    public View getFeatureAt(int index) {
        return features.get(index);
    }

    public void removeFeatureAt(int index) {
        AirMapFeature feature = features.remove(index);


        if (feature instanceof AirMapMarker) {
            markerMap.remove(feature.getFeature());
        } else if (feature instanceof AirMapPolyline) {
            polylineMap.remove(feature.getFeature());
        } else if (feature instanceof AirMapPolygon) {
            polygonMap.remove(feature.getFeature());
        } else if (feature instanceof AirMapCircle) {
            circleMap.remove(feature.getFeature());
        }
        feature.removeFromMap(map);
    }

    public WritableMap makeClickEventData(LatLng point) {
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

    public void updateExtraData(Object extraData) {
        // if boundsToMove is not null, we now have the MapView's width/height, so we can apply
        // a proper camera move
        if (boundsToMove != null) {
            HashMap<String, Float> data = (HashMap<String, Float>) extraData;
            float width = data.get("width");
            float height = data.get("height");
            map.animateMapStatus(
                    MapStatusUpdateFactory.newLatLngBounds(
                            boundsToMove,
                            (int) width,
                            (int) height
                    )
            );
            boundsToMove = null;
        }
    }

    public void animateToRegion(LatLngBounds bounds, int duration) {
        startMonitoringRegion();
//        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0), duration, null);
        MapStatusUpdate cu = MapStatusUpdateFactory.newLatLngBounds(bounds);
        map.animateMapStatus(cu);
    }

    public void animateToCoordinate(LatLng coordinate, int duration) {
        startMonitoringRegion();
//        map.animateCamera(CameraUpdateFactory.newLatLng(coordinate), duration, null);
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(coordinate);
        LatLngBounds bounds = builder.build();
        MapStatusUpdate cu = MapStatusUpdateFactory.newLatLngBounds(bounds);

        map.animateMapStatus(cu);
    }

    public void fitToElements(boolean animated) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (AirMapFeature feature : features) {
            if (feature instanceof AirMapMarker) {
                Marker marker = (Marker) feature.getFeature();
                builder.include(marker.getPosition());
            }
            // TODO(lmr): may want to include shapes / etc.
        }
        LatLngBounds bounds = builder.build();
        MapStatusUpdate cu = MapStatusUpdateFactory.newLatLngBounds(bounds);
        if (animated) {
            startMonitoringRegion();
            map.animateMapStatus(cu);
        } else {
            map.animateMapStatus(cu);
        }
    }

    // InfoWindowAdapter interface

    public View getInfoWindow(Marker marker) {
        AirMapMarker markerView = markerMap.get(marker);
        return markerView.getCallout();
    }

    public View getInfoContents(Marker marker) {
        AirMapMarker markerView = markerMap.get(marker);
        return markerView.getInfoContents();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        scaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);

        int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                this.getParent().requestDisallowInterceptTouchEvent(true);
                isTouchDown = true;
                break;
            case (MotionEvent.ACTION_MOVE):
                startMonitoringRegion();
                break;
            case (MotionEvent.ACTION_UP):
                this.getParent().requestDisallowInterceptTouchEvent(false);
                isTouchDown = false;
                break;
        }
        super.onTouchEvent(ev);
        return true;
    }

    // Timer Implementation

    public void startMonitoringRegion() {
        if (isMonitoringRegion) return;
        timerHandler.postDelayed(timerRunnable, 100);
        isMonitoringRegion = true;
    }

    public void stopMonitoringRegion() {
        if (!isMonitoringRegion) return;
        timerHandler.removeCallbacks(timerRunnable);
        isMonitoringRegion = false;
    }

    private LatLngBounds lastBoundsEmitted;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            Projection pro = map.getProjection();
            LatLng leftUp = pro.fromScreenLocation(new Point(0, 0));
            LatLng rightUp = pro.fromScreenLocation(new Point(screenWidth, 0));
            LatLng leftDown = pro.fromScreenLocation(new Point(0, screenHeight));
            LatLng rightDown = pro.fromScreenLocation(new Point(screenWidth, screenHeight));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            LatLngBounds bounds = builder
                    .include(leftUp)
                    .include(leftDown)
                    .include(rightUp)
                    .include(rightDown)
                    .build();

            if (lastBoundsEmitted == null ||
                    LatLngBoundsUtils.BoundsAreDifferent(bounds, lastBoundsEmitted)) {
                LatLng center = map.getMapStatus().target;
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), bounds, center, true));
            }

            timerHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onMarkerDragStart(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDragStart", event);

        AirMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDragStart", event);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDrag", event);

        AirMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDrag", event);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDragEnd", event);

        AirMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDragEnd", event);
    }

    private ProgressBar getMapLoadingProgressBar() {
        if (this.mapLoadingProgressBar == null) {
            this.mapLoadingProgressBar = new ProgressBar(mapView.getContext());
            this.mapLoadingProgressBar.setIndeterminate(true);
        }
        if (this.loadingIndicatorColor != null) {
            this.setLoadingIndicatorColor(this.loadingIndicatorColor);
        }
        return this.mapLoadingProgressBar;
    }

    private RelativeLayout getMapLoadingLayoutView() {
        if (this.mapLoadingLayout == null) {
            this.mapLoadingLayout = new RelativeLayout(mapView.getContext());
            this.mapLoadingLayout.setBackgroundColor(Color.LTGRAY);
            mapView.addView(this.mapLoadingLayout,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            this.mapLoadingLayout.addView(this.getMapLoadingProgressBar(), params);

            this.mapLoadingLayout.setVisibility(View.INVISIBLE);
        }
        this.setLoadingBackgroundColor(this.loadingBackgroundColor);
        return this.mapLoadingLayout;
    }

    private ImageView getCacheImageView() {
        if (this.cacheImageView == null) {
            this.cacheImageView = new ImageView(mapView.getContext());
            mapView.addView(this.cacheImageView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            this.cacheImageView.setVisibility(View.INVISIBLE);
        }
        return this.cacheImageView;
    }

    private void removeCacheImageView() {
        if (this.cacheImageView != null) {
            ((ViewGroup)this.cacheImageView.getParent()).removeView(this.cacheImageView);
            this.cacheImageView = null;
        }
    }

    private void removeMapLoadingProgressBar() {
        if (this.mapLoadingProgressBar != null) {
            ((ViewGroup)this.mapLoadingProgressBar.getParent()).removeView(this.mapLoadingProgressBar);
            this.mapLoadingProgressBar = null;
        }
    }

    private void removeMapLoadingLayoutView() {
        this.removeMapLoadingProgressBar();
        if (this.mapLoadingLayout != null) {
            ((ViewGroup)this.mapLoadingLayout.getParent()).removeView(this.mapLoadingLayout);
            this.mapLoadingLayout = null;
        }
    }

    private void cacheView() {
        if (this.cacheEnabled) {
            final ImageView cacheImageView = this.getCacheImageView();
            final RelativeLayout mapLoadingLayout = this.getMapLoadingLayoutView();
            cacheImageView.setVisibility(View.INVISIBLE);
            mapLoadingLayout.setVisibility(View.VISIBLE);
            if (this.isMapLoaded) {
                this.map.snapshot(new BaiduMap.SnapshotReadyCallback() {
                    @Override public void onSnapshotReady(Bitmap bitmap) {
                        cacheImageView.setImageBitmap(bitmap);
                        cacheImageView.setVisibility(View.VISIBLE);
                        mapLoadingLayout.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
        else {
            this.removeCacheImageView();
            if (this.isMapLoaded) {
                this.removeMapLoadingLayoutView();
            }
        }
    }

    public void onPanDrag(MotionEvent ev) {
        Point point = new Point((int) ev.getX(), (int) ev.getY());
        LatLng coords = this.map.getProjection().fromScreenLocation(point);
        WritableMap event = makeClickEventData(coords);
        manager.pushEvent(this, "onPanDrag", event);
    }

    public AirMapMarker getCurrent(Marker marker) {
        return markerMap.get(marker);
    }
}