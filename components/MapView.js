'use strict';


import React, {Component } from 'react';
import {
    EdgeInsetsPropType,
    NativeMethodsMixin,
    Platform,
    ReactNativeViewAttributes,
    View,
    Animated,
    requireNativeComponent,
    NativeModules,
    ColorPropType,
} from 'react-native';
import PropTypes from 'prop-types'

import MapMarker from './MapMarker';
import MapPolyline from './MapPolyline';
import MapPolygon from './MapPolygon';
import MapCircle from './MapCircle';
import MapCallout from './MapCallout';

const  BMapManager = require('../js/BMapManager');
export default class MapView extends Component{
  mixins= [NativeMethodsMixin]

  viewConfig= {
    uiViewClassName: 'AIRMap',
    validAttributes: {
      region: true,
    },
  }
  static
  statics= {
    BMapManager,
  }
  static propTypes =  {
    ...View.propTypes,
    /**
     * Used to style and layout the `MapView`.  See `StyleSheet.js` and
     * `ViewStylePropTypes.js` for more info.
     */
//     style: View.propTypes.style,

    /**
     * If `true` the app will ask for the user's location.
     * Default value is `false`.
     *
     * **NOTE**: You need to add NSLocationWhenInUseUsageDescription key in
     * Info.plist to enable geolocation, otherwise it is going
     * to *fail silently*!
     */
    showsUserLocation: PropTypes.bool,

    /**
     * If `false` hide the button to move map to the current user's location.
     * Default value is `true`.
     *
     * @platform android
     */
    showsMyLocationButton: PropTypes.bool,

    /**
     * If `true` the map will focus on the user's location. This only works if
     * `showsUserLocation` is true and the user has shared their location.
     * Default value is `false`.
     *
     * @platform ios
     */
    followsUserLocation: PropTypes.bool,

    /**
     * If `false` points of interest won't be displayed on the map.
     * Default value is `true`.
     *
     */
    showsPointsOfInterest: PropTypes.bool,

    /**
     * If `false` compass won't be displayed on the map.
     * Default value is `true`.
     *
     * @platform ios
     */
    showsCompass: PropTypes.bool,

    /**
     * If `false` the user won't be able to pinch/zoom the map.
     * Default value is `true`.
     *
     */
    zoomEnabled: PropTypes.bool,

    /**
     * If `false` the user won't be able to pinch/rotate the map.
     * Default value is `true`.
     *
     */
    rotateEnabled: PropTypes.bool,

    /**
     * If `true` the map will be cached to an Image for performance
     * Default value is `false`.
     *
     */
    cacheEnabled: PropTypes.bool,

    /**
     * If `true` the map will be showing a loading indicator
     * Default value is `false`.
     *
     */
    loadingEnabled: PropTypes.bool,

    /**
     * Loading background color while generating map cache image or loading the map
     * Default color is light gray.
     *
     */
    loadingBackgroundColor: ColorPropType,

    /**
     * Loading indicator color while generating map cache image or loading the map
     * Default color is gray color for iOS, theme color for Android.
     *
     */
    loadingIndicatorColor: ColorPropType,

    /**
     * If `false` the user won't be able to change the map region being displayed.
     * Default value is `true`.
     *
     */
    scrollEnabled: PropTypes.bool,

    /**
     * If `false` the user won't be able to adjust the camera’s pitch angle.
     * Default value is `true`.
     *
     */
    pitchEnabled: PropTypes.bool,

    /**
     * If `false` will hide 'Navigate' and 'Open in Maps' buttons on marker press
     * Default value is `true`.
     *
     * @platform android
     */
    toolbarEnabled: PropTypes.bool,

    /**
     * A Boolean indicating whether the map shows scale information.
     * Default value is `false`
     *
     */
    showsScale: PropTypes.bool,

    /**
     * A Boolean indicating whether the map displays extruded building information.
     * Default value is `true`.
     */
    showsBuildings: PropTypes.bool,

    /**
     * A Boolean value indicating whether the map displays traffic information.
     * Default value is `false`.
     */
    showsTraffic: PropTypes.bool,

    /**
     * A Boolean indicating whether indoor maps should be enabled.
     * Default value is `false`
     *
     * @platform android
     */
    showsIndoors: PropTypes.bool,

    /**
     * The map type to be displayed.
     *
     * - standard: standard road map (default)
     * - satellite: satellite view
     * - hybrid: satellite view with roads and points of interest overlayed
     * - terrain: (Android only) topographic view
     */
    mapType: PropTypes.oneOf([
      'standard',
      'satellite',
      'hybrid',
      'terrain',
    ]),

    /**
     * The region to be displayed by the map.
     *
     * The region is defined by the center coordinates and the span of
     * coordinates to display.
     */
    region: PropTypes.shape({
      /**
       * Coordinates for the center of the map.
       */
      latitude: PropTypes.number.isRequired,
      longitude: PropTypes.number.isRequired,

      /**
       * Difference between the minimun and the maximum latitude/longitude
       * to be displayed.
       */
      latitudeDelta: PropTypes.number.isRequired,
      longitudeDelta: PropTypes.number.isRequired,
    }),

    /**
     * The initial region to be displayed by the map.  Use this prop instead of `region`
     * only if you don't want to control the viewport of the map besides the initial region.
     *
     * Changing this prop after the component has mounted will not result in a region change.
     *
     * This is similar to the `initialValue` prop of a text input.
     */
    initialRegion: PropTypes.shape({
      /**
       * Coordinates for the center of the map.
       */
      latitude: PropTypes.number.isRequired,
      longitude: PropTypes.number.isRequired,

      /**
       * Difference between the minimun and the maximum latitude/longitude
       * to be displayed.
       */
      latitudeDelta: PropTypes.number.isRequired,
      longitudeDelta: PropTypes.number.isRequired,
    }),

    /**
     * Maximum size of area that can be displayed.
     *
     * @platform ios
     */
    maxDelta: PropTypes.number,

    /**
     * Minimum size of area that can be displayed.
     *
     * @platform ios
     */
    minDelta: PropTypes.number,

    /**
     * Insets for the map's legal label, originally at bottom left of the map.
     * See `EdgeInsetsPropType.js` for more information.
     */
    legalLabelInsets: EdgeInsetsPropType,

    /**
     * Callback that is called continuously when the user is dragging the map.
     */
    onRegionChange: PropTypes.func,

    /**
     * Callback that is called once, when the user is done moving the map.
     */
    onRegionChangeComplete: PropTypes.func,

    /**
     * Callback that is called when user taps on the map.
     */
    onPress: PropTypes.func,

    /**
     * Callback that is called when user makes a "long press" somewhere on the map.
     */
    onLongPress: PropTypes.func,

    /**
     * Callback that is called when user makes a "drag" somewhere on the map
     */
    onPanDrag: PropTypes.func,

    /**
     * Callback that is called when a marker on the map is tapped by the user.
     */
    onMarkerPress: PropTypes.func,

    /**
     * Callback that is called when a marker on the map becomes selected. This will be called when
     * the callout for that marker is about to be shown.
     *
     * @platform ios
     */
    onMarkerSelect: PropTypes.func,

    /**
     * Callback that is called when a marker on the map becomes deselected. This will be called when
     * the callout for that marker is about to be hidden.
     *
     * @platform ios
     */
    onMarkerDeselect: PropTypes.func,

    /**
     * Callback that is called when a callout is tapped by the user.
     */
    onCalloutPress: PropTypes.func,

    /**
     * Callback that is called when the user initiates a drag on a marker (if it is draggable)
     */
    onMarkerDragStart: PropTypes.func,

    /**
     * Callback called continuously as a marker is dragged
     */
    onMarkerDrag: PropTypes.func,

    /**
     * Callback that is called when a drag on a marker finishes. This is usually the point you
     * will want to setState on the marker's coordinate again
     */
    onMarkerDragEnd: PropTypes.func,
  }
  constructor(props: any) {
    super(props);
    this._onMapReady = this._onMapReady.bind(this)
    this._onLayout = this._onLayout.bind(this)
    this._onChange = this._onChange.bind(this)
    this.animateToRegion = this.animateToRegion.bind(this)
    this.animateToCoordinate = this.animateToCoordinate.bind(this)
    this.fitToElements = this.fitToElements.bind(this)
    this.takeSnapshot = this.takeSnapshot.bind(this)

    this._getHandle = this._getHandle.bind(this)
    this._runCommand = this._runCommand.bind(this)


    this.state = {
      isReady: true,//Platform.OS === 'ios',
    };
  }

  componentDidMount() {
    const { region, initialRegion } = this.props;
    if (region && this.state.isReady) {
      this.map.setNativeProps({ region });
    } else if (initialRegion && this.state.isReady) {
      this.map.setNativeProps({ region: initialRegion });
    }
  }
  componentWillUpdate(nextProps) {
    var a = this.__lastRegion;
    var b = nextProps.region;
    if (!b) return;
    if (!a ||
          a.latitude !== b.latitude ||
          a.longitude !== b.longitude ||
          a.latitudeDelta !== b.latitudeDelta ||
          a.longitudeDelta !== b.longitudeDelta
          ) {
      this.map.setNativeProps({ region: b });
    }
  }

  _onMapReady() {
    const { region, initialRegion } = this.props;
    if (region) {
      this.map.setNativeProps({ region });
    } else if (initialRegion) {
      this.map.setNativeProps({ region: initialRegion });
    }
    this.setState({ isReady: true });
  }

  _onLayout(e) {
    const { region, initialRegion, onLayout } = this.props;
    const { isReady } = this.state;
    if (region && isReady && !this.__layoutCalled) {
      this.__layoutCalled = true;
      this.map.setNativeProps({ region });
    } else if (initialRegion && isReady && !this.__layoutCalled) {
      this.__layoutCalled = true;
      this.map.setNativeProps({ region: initialRegion });
    }
    onLayout && onLayout(e);
  }

  _onChange(event: Event) {
    this.__lastRegion = event.nativeEvent.region;
    if (event.nativeEvent.continuous) {
      this.props.onRegionChange &&
      this.props.onRegionChange(event.nativeEvent.region);
    } else {
      this.props.onRegionChangeComplete &&
      this.props.onRegionChangeComplete(event.nativeEvent.region);
    }
  }

  animateToRegion (region, duration) {
    this._runCommand('animateToRegion', [region, duration || 500]);
  }

  animateToCoordinate (latLng, duration) {
    this._runCommand('animateToCoordinate', [latLng, duration || 500]);
  }

  fitToElements(animated) {
    this._runCommand('fitToElements', [animated]);
  }

  takeSnapshot (width, height, region, callback) {
    if (!region) {
      region = this.props.region || this.props.initialRegion;
    }
    this._runCommand('takeSnapshot', [width, height, region, callback]);
  }

  _getHandle() {
    return ReactNative.findNodeHandle(this.map);
  }

  _runCommand (name, args) {
    switch (Platform.OS) {
      case 'android':
        NativeModules.UIManager.dispatchViewManagerCommand(
            this._getHandle(),
            NativeModules.UIManager.AIRMap.Commands[name],
            args
        );
        break;

      case 'ios':
        NativeModules.AIRMapManager[name].apply(
            NativeModules.AIRMapManager[name],
            [this._getHandle(), ...args]
        );
        break;
    }
  }

  render() {
    let props;

    if (this.state.isReady) {
      props = {
        ...this.props,
        region: null,
        initialRegion: null,
        onChange: this._onChange,
        onMapReady: this._onMapReady,
        onLayout: this._onLayout,
      };
      if (Platform.OS === 'ios' && props.mapType === 'terrain') {
        props.mapType = 'standard';
      }
      props.handlePanDrag = !!props.onPanDrag;
    } else {
      props = {
        region: null,
        initialRegion: null,
        onChange: this._onChange,
        onMapReady: this._onMapReady,
        onLayout: this._onLayout,
      };
    }

    return (
        <AIRMap
            ref={ref => { this.map = ref; }}
            {...props} />
    );
  }

}

var AIRMap = requireNativeComponent('AIRMap', MapView, {
  nativeOnly: {
    onChange: true,
    onMapReady: true,
    handlePanDrag: true,
  },
});

MapView.Marker = MapMarker;
MapView.Polyline = MapPolyline;
MapView.Polygon = MapPolygon;
MapView.Circle = MapCircle;
MapView.Callout = MapCallout;


MapView.Animated = Animated.createAnimatedComponent(MapView);

