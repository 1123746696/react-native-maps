
import React, {Component } from 'react';
import {
    View,
    NativeMethodsMixin,
    requireNativeComponent,
    StyleSheet,
} from 'react-native';
import PropTypes from 'prop-types'

class MapCallout extends Component{
  mixins = [NativeMethodsMixin]
  static defaultProps = {
    tooltip: false,
  }
  static propTypes = {
    ...View.propTypes,
    tooltip: PropTypes.bool,
    onPress: PropTypes.func,
  }
  render() {
    return <AIRMapCallout {...this.props} style={[styles.callout, this.props.style]} />;
  }
}

var styles = StyleSheet.create({
  callout: {
    position: 'absolute',
    //flex: 0,
    //backgroundColor: 'transparent',
  },
});

var AIRMapCallout = requireNativeComponent('AIRMapCallout', MapCallout);

export default MapCallout;
