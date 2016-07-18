//
//  BMapManager.m
//  AirMaps
//
//  Created by user on 16/7/18.
//  Copyright © 2016年 Christopher. All rights reserved.
//

#import "BMapManager.h"
#import <BaiduMapAPI_Base/BMKBaseComponent.h>
@implementation BMapManager
+(id)shareInstance{
    static BMKMapManager *_mapManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _mapManager = [[BMKMapManager alloc] init];
    });
    return _mapManager;
    
}
RCT_EXPORT_MODULE();
RCT_EXPORT_METHOD(start:(NSString *)key){
    BMKMapManager *_mapManager = [BMapManager shareInstance];
    // 如果要关注网络及授权验证事件，请设定     generalDelegate参数
    BOOL ret = [_mapManager start:key  generalDelegate:_mapManager];
    if (!ret) {
        NSLog(@"manager start failed!");
    }else{
        NSLog(@"map success");
    }
}

- (void)onGetNetworkState:(int)iError
{
    if (0 == iError) {
        NSLog(@"联网成功");
    }
    else{
        NSLog(@"onGetNetworkState %d",iError);
    }
    
}

- (void)onGetPermissionState:(int)iError
{
    if (0 == iError) {
        NSLog(@"授权成功");
    }
    else {
        NSLog(@"onGetPermissionState %d",iError);
    }
}
@end
