//
// Created by Leland Richardson on 12/27/15.
// Copyright (c) 2015 Facebook. All rights reserved.
//

#import "AIRMapCallout.h"


@implementation AIRMapCallout {
}
- (void)layoutSubviews{
    if (self.subviews&&self.subviews.count>0) {
        UIView *subview = [self.subviews objectAtIndex:0];
//        self.frame=CGRectMake(self.frame.origin.x, self.frame.origin.y, subview.frame.size.width, subview.frame.size.height);
        self.frame=subview.frame;
    }
}
@end