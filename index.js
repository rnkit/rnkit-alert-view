/* @flow */

/**
  * index.js
  *
  * @author SimMan (liwei0990#gmail.com)
  * @Time at 2016-12-04 12:04:33
  * Copyright 2011-2016 RNKit.io, Inc.
  */
'use strict';

import {
  NativeModules,
  processColor,
  Platform,
  NativeEventEmitter
} from 'react-native';

const { RNKitAlertView } = NativeModules;
const nativeEventEmitter = new NativeEventEmitter(RNKitAlertView);

type AlertType = $Enum<{
  'default': string,
  'plain-text': string
}>;

type AlertButtonStyle = $Enum<{
  'default': string,
  'cancel': string,
  'destructive': string,
}>;

type ButtonsArray = Array<{
  text?: string,
  onPress?: ?Function,
  style?: AlertButtonStyle,
}>;

class Alert {

  static alert(
    title: ?string,
    message?: ?string,
    callbackOrButtons?: ?(() => void) | ButtonsArray,
    type?: AlertType,
    placeholder?: string,
    doneButtonKey?: number,
  ): void {
    var callbacks = [];
    var buttons = [];
    if (typeof callbackOrButtons === 'function') {
      callbacks = [callbackOrButtons];
    }
    else if (callbackOrButtons instanceof Array) {
      callbackOrButtons.forEach((btn, index) => {
        callbacks[index] = btn.onPress;
        if (btn.text || index < (callbackOrButtons || []).length - 1) {
          var btnDef = {};
          btnDef['text'] = btn.text || '';
          btnDef['index'] = String(index);
          btnDef['style'] = btn.style || 'default';
          buttons.push(btnDef);
        }
      });
    }

    this.listener && this.listener.remove();
    this.listener = nativeEventEmitter.addListener('AlertViewEvent', event => {
      if (event.text.length && Platform.OS !== 'ios') {
        this.listener && this.listener.remove();
      }
      var cb = callbacks[doneButtonKey];
      cb && cb(event.text);
      // console.log('=========>' + event.text);
    });

    RNKitAlertView.alertWithArgs({
      title: title || undefined,
      message: message || undefined,
      buttons,
      type: type || undefined,
      placeholder
    }, (id, value) => {
      var cb = callbacks[id];
      cb && cb(value);
    });
  }
};

export default Alert;
