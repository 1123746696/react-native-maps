//
// Created by Leland Richardson on 12/27/15.
// Copyright (c) 2015 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <BaiduMapAPI_Map/BMKMapComponent.h>
#import <UIKit/UIKit.h>

#import "RCTConvert+AirMap.h"
#import "RCTComponent.h"
#import "AIRMapCoordinate.h"
#import "AIRMap.h"
#import "RCTView.h"



@interface AIRMapCircle: BMKAnnotationView <MKOverlay>

@property (nonatomic, weak) AIRMap *map;

@property (nonatomic, strong) BMKCircle *circle;
@property (nonatomic, strong) MKCircleRenderer *renderer;

@property (nonatomic, assign) CLLocationCoordinate2D centerCoordinate;
@property (nonatomic, assign) CLLocationDistance radius;

@property (nonatomic, strong) UIColor *fillColor;
@property (nonatomic, strong) UIColor *strokeColor;
@property (nonatomic, assign) CGFloat strokeWidth;
@property (nonatomic, assign) CGFloat miterLimit;
@property (nonatomic, assign) CGLineCap lineCap;
@property (nonatomic, assign) CGLineJoin lineJoin;
@property (nonatomic, assign) CGFloat lineDashPhase;
@property (nonatomic, strong) NSArray <NSNumber *> *lineDashPattern;

#pragma mark MKOverlay protocol

@property(nonatomic, readonly) CLLocationCoordinate2D coordinate;
@property(nonatomic, readonly) BMKMapRect boundingMapRect;
- (BOOL)intersectsMapRect:(BMKMapRect)mapRect;
- (BOOL)canReplaceMapContent;

@end
