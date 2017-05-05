//
//  RNKitAlertView.m
//  RNKitAlertView
//
//  Created by SimMan on 2016/12/03.
//  Copyright © 2016年 RNKit.io. All rights reserved.
//

#import "RNKitAlertView.h"

#import <React/RCTConvert.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <React/RCTEventDispatcher.h>

#import "Masonry.h"
#import "MMPopupItem.h"
#import "MMAlertView.h"
#import "MMSheetView.h"
#import "MMPopupWindow.h"

typedef NS_ENUM(NSInteger, RNKAlertViewStyle) {
    RNKAlertViewStyleDefault = 0,
    RNKAlertViewStylePlainTextInput,
};

@implementation RCTConvert (UIAlertView)

RCT_ENUM_CONVERTER(RNKAlertViewStyle, (@{
    @"default": @(RNKAlertViewStyleDefault),
    @"plain-text": @(RNKAlertViewStylePlainTextInput)
}), RNKAlertViewStyleDefault, integerValue)

RCT_ENUM_CONVERTER(MMItemType, (@{
    @"default": @(MMItemTypeNormal),
    @"cancel": @(MMItemTypeNormal),
    @"destructive": @(MMItemTypeHighlight)
}), MMItemTypeNormal, integerValue)
@end

@implementation RNKitAlertView
{
    NSHashTable *_alerts;
    RCTResponseSenderBlock _callback;
}

@synthesize bridge = _bridge;


RCT_EXPORT_MODULE()

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"AlertViewEvent"];
}


- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (void) setUp
{
    MMAlertViewConfig *alertConfig = [MMAlertViewConfig globalConfig];
    MMSheetViewConfig *sheetConfig = [MMSheetViewConfig globalConfig];
    
    alertConfig.defaultTextOK = @"确定";
    alertConfig.defaultTextCancel = @"取消";
    alertConfig.defaultTextConfirm = @"确认";
    alertConfig.itemNormalColor = [UIColor colorWithRed:0.085 green:0.480 blue:0.983 alpha:1];
    alertConfig.itemHighlightColor = [UIColor colorWithRed:0.987 green:0.226 blue:0.190 alpha:1];
    
    sheetConfig.defaultTextCancel = @"取消";
}

RCT_EXPORT_METHOD(alertWithArgs:(NSDictionary *)args callback:(RCTResponseSenderBlock)callback)
{
    NSString *title = [RCTConvert NSString:args[@"title"]];
    NSString *message = [RCTConvert NSString:args[@"message"]];
    RNKAlertViewStyle type = [RCTConvert RNKAlertViewStyle:args[@"type"]];
    NSArray<NSDictionary *> *buttons = [RCTConvert NSDictionaryArray:args[@"buttons"]];
    NSString *placeholder = [RCTConvert NSString:args[@"placeholder"]];
    
    [self setUp];
    
    MMPopupItemHandler block = ^(NSInteger index){
        if (callback) {
            callback(@[@(index), @""]);
        }
    };
    
    MMPopupCompletionBlock completeBlock = ^(MMPopupView *popupView, BOOL finished){
        NSLog(@"animation complete");
    };
    
    NSMutableArray *items = [NSMutableArray arrayWithCapacity:buttons.count];
    
    for (NSDictionary *button in buttons) {
        [items addObject: MMItemMake(button[@"text"], [RCTConvert MMItemType:button[@"style"]], block)];
    }
    
    
    MMAlertView *alertView;
    
    if (type == RNKAlertViewStyleDefault) {
        alertView = [[MMAlertView alloc] initWithTitle:title
                                                             detail:message
                                                              items:items];
        alertView.attachedView.mm_dimBackgroundBlurEnabled = NO;
        
        [alertView show];
    } else {
        __weak __typeof(self) weakSelf = self;
        alertView = [[MMAlertView alloc] initWithInputTitle:title detail:message placeholder:placeholder handler:^(NSString *text) {
            
            __typeof(self) strongSelf = weakSelf;
            if (!strongSelf) {
                return;
            }
            [strongSelf sendEventWithName:@"AlertViewEvent" body:@{@"text": text}];
        }];
        alertView.attachedView.mm_dimBackgroundBlurEnabled = NO;
        [alertView showWithBlock:completeBlock];
    }

    [[self getAlerts] addObject:alertView];
}

- (NSHashTable *) getAlerts
{
    if (!_alerts) {
        _alerts = [NSHashTable weakObjectsHashTable];
    }
    return _alerts;
}

- (void) dealloc
{
    NSHashTable *alerts = [self getAlerts];
    for (MMAlertView *alert in alerts) {
        if (alert && [alert respondsToSelector:@selector(hide)] && !alert.inputView) {
            @try {
                [alert hide];
            } @catch (NSException *exception) {
                
            } @finally {
//                [alerts removeObject: alert];
            }
//            [alert hide];
        }
    }
}
@end
