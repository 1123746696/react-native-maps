var BMap = require('react-native').NativeModules.BMapManager;
var BMapManager = {
    start(key:String) {
        BMap.start(key);
    }
};
module.exports = BMapManager;