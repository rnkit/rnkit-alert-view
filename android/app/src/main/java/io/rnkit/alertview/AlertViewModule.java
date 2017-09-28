package io.rnkit.alertview;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnDismissListener;
import com.bigkoo.alertview.OnItemClickListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;


/**
 * Created by SimMan on 2016/11/30.
 */

public class AlertViewModule extends ReactContextBaseJavaModule implements Application.ActivityLifecycleCallbacks, OnItemClickListener, OnDismissListener {

    private static final String REACT_CLASS = "RNKitAlertView";
    private static final String ERROR_NO_ACTIVITY = "E_NO_ACTIVITY";
    final ReactApplicationContext reactContext;
    private Bundle args;
    private RNKAlertView mAlertView;
    private EditText etName;
    private InputMethodManager imm;
    private Callback retCallback;

    /* package */ static final String TITLE = "title";
    /* package */ static final String MESSAGE = "message";
    /* package */ static final String TYPE = "type";
    /* package */ static final String PLACEHOLDER = "placeholder";
    /* package */ static final String BUTTONS = "buttons";


    public AlertViewModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    public enum RNKAlertViewStyle {
        RNKAlertViewStyleDefault,
        RNKAlertViewStylePlainTextInput
    }

    public enum RNKAlertButtonStyle {
        RNKAlertButtonStyleDefault,
        RNKAlertButtonStyleCancel,
        RNKAlertButtonStyleDestructive
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void alertWithArgs(@Nullable final ReadableMap options, @Nullable final Callback callback) {
        this.retCallback = callback;
        
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    throw new JSApplicationIllegalArgumentException("Tried to open a Alert view while not attached to an Activity");
                }

                args = createFragmentArguments(options);

                RNKAlertView.Builder builder = new RNKAlertView.Builder()
                                                        .setContext(activity)
                                                        .setStyle(AlertView.Style.Alert)
                                                        .setTitle(args.getString(TITLE))
                                                        .setMessage(args.getString(MESSAGE));

                if (!options.hasKey(BUTTONS) || options.isNull(BUTTONS)) {
                    throw new JSApplicationIllegalArgumentException("Alert View button must contain one or more");
                }

                ReadableArray buttons = options.getArray(BUTTONS);

                ArrayList<String> defaultButtons = new ArrayList<>();
                ArrayList<String> destructiveButtons = new ArrayList<>();

                for (int i = 0; i < buttons.size(); i++) {
                    ReadableMap b = buttons.getMap(i);

                    String text = b.hasKey("text") ? b.getString("text") : "";
                    String style = b.hasKey("style") ? b.getString("style") : "";

                    if (style.equals("default") || style.isEmpty() || style.equals("cancel")) {
                        defaultButtons.add(text);
                    } else if (style.equals("destructive")) {
                        destructiveButtons.add(text);
                    }
                }

                if (defaultButtons.size() > 0) {
                    builder.setOthers(defaultButtons.toArray(new String[defaultButtons.size()]));
                }

                if (destructiveButtons.size() > 0) {
                    builder.setDestructive(destructiveButtons.toArray(new String[destructiveButtons.size()]));
                }

                builder.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (retCallback != null) {
                            WritableArray array = Arguments.createArray();
                            array.pushInt(position);
                            retCallback.invoke(array);
                            retCallback = null;
                        }
                    }
                });

                mAlertView = builder.build();

                if (args.getString(TYPE).equals("plain-text")) {
                    ViewGroup extView = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.alerttext_form,null);
                    etName = (EditText) extView.findViewById(R.id.etName);

                    imm = (InputMethodManager)reactContext.getSystemService(reactContext.INPUT_METHOD_SERVICE);
                    etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View view, boolean focus) {
                            boolean isOpen = imm.isActive();
                            mAlertView.setMarginBottom(isOpen&&focus ? 120 :0);
                            if (isOpen) {
                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                            }

                            WritableMap map = Arguments.createMap();
                            String text = etName.getText().toString();
                            map.putString("text", text);

                            sendEvent(reactContext, "AlertViewEvent", map);
                        }
                    });
                    mAlertView.addExtView(extView);
                }

                mAlertView.show();
            }
        });
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {

        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private Bundle createFragmentArguments(ReadableMap options) {

        final Bundle args = new Bundle();

        if (options.hasKey(TITLE) && !options.isNull(TITLE)) {
            args.putString(TITLE, options.getString(TITLE));
        }
        if (options.hasKey(MESSAGE) && !options.isNull(MESSAGE)) {
            args.putString(MESSAGE, options.getString(MESSAGE));
        }

        if (options.hasKey(TYPE) && !options.isNull(TYPE)) {
            args.putString(TYPE, options.getString(TYPE));
        } else {
            args.putString(TYPE, "default");
        }

        if (options.hasKey(PLACEHOLDER) && !options.isNull(PLACEHOLDER)) {
            args.putString(PLACEHOLDER, options.getString(PLACEHOLDER));
        }

        return args;
    }


    @Override
    public void onItemClick(Object o,int position) {
        Log.d("onItemClick", o.toString() + " -- " +position);
    }

    @Override
    public void onDismiss(Object o) {
        Log.d("OnDismiss", o.toString());
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
