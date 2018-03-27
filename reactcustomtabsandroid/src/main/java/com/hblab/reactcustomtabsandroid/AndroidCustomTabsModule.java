package com.hblab.reactcustomtabsandroid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class AndroidCustomTabsModule extends ReactContextBaseJavaModule {

    public static final String FLAG_ACTIVITY_NEW_TASK = "FLAG_ACTIVITY_NEW_TASK";
    public static final String ACTION_ALL_APPS = "ACTION_ALL_APPS";
    public static final String ACTION_CALL = "ACTION_CALL";
    public static final String ACTION_SEND = "ACTION_SEND";
    public static final String ACTION_CHOOSER = "ACTION_CHOOSER";


    private CustomTabsClient mCustomTabsClient = null;
    private CustomTabsSession mCustomTabsSession = null;
    private Context mContext;
    private CustomTabsIntent.Builder builder = null;

    public AndroidCustomTabsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
        builder = new CustomTabsIntent.Builder();
    }

    @Override
    public String getName() {
        return "CustomTabsModule";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(FLAG_ACTIVITY_NEW_TASK, Intent.FLAG_ACTIVITY_NEW_TASK);
        constants.put(ACTION_ALL_APPS, Intent.ACTION_ALL_APPS);
        constants.put(ACTION_CALL, Intent.ACTION_CALL);
        constants.put(ACTION_SEND, Intent.ACTION_SEND);
        constants.put(ACTION_CHOOSER, Intent.ACTION_CHOOSER);
        return constants;
    }

    @ReactMethod
    public void openURL(String url) {
        CustomTabsServiceConnection mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient = null;
            }
        };

        final String CUSTOM_TAB_PACKAGE_NAME = getCurrentActivity().getPackageName();
        CustomTabsClient.bindCustomTabsService(mContext, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.addCategory("android.intent.category.BROWSABLE");
        customTabsIntent.intent.setPackage("com.android.chrome");
        Activity activity = getCurrentActivity();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

    @ReactMethod
    public void openURL(String url, ReadableMap options) {
        CustomTabsServiceConnection mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
                mCustomTabsClient = customTabsClient;
                mCustomTabsClient.warmup(0L);
                mCustomTabsSession = mCustomTabsClient.newSession(null);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mCustomTabsClient = null;
            }
        };

        final String CUSTOM_TAB_PACKAGE_NAME = getCurrentActivity().getPackageName();
        CustomTabsClient.bindCustomTabsService(mContext, CUSTOM_TAB_PACKAGE_NAME, mCustomTabsServiceConnection);
        if (options.hasKey("tintColor")) {
            String toolbarColor = options.getString("tintColor");
            if (isHexColor(toolbarColor)) {
                builder.setToolbarColor(Color.parseColor(toolbarColor));
            } else if (isRGBA(toolbarColor)) {
                builder.setToolbarColor(parseRGBA(toolbarColor));
            } else if (isRGB(toolbarColor)) {
                builder.setToolbarColor(parseRGB(toolbarColor));
            }
        }

        if (options.hasKey("fromBottom")) {
            builder.setStartAnimations(mContext, R.anim.slide_in_up, R.anim.do_nothing);
            builder.setStartAnimations(mContext, R.anim.do_nothing, R.anim.slide_out_up);
        }

        if (options.hasKey("defaultShareMenuItem") && options.getBoolean("defaultShareMenuItem")) {
            builder.addDefaultShareMenuItem();
        }
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.addCategory("android.intent.category.BROWSABLE");
        customTabsIntent.intent.setPackage("com.android.chrome");
        Activity activity = getCurrentActivity();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

    @ReactMethod
    public void addMenuItems(String label, String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.addMenuItem(label, pendingIntent);
    }

    private boolean isHexColor(String color) {
        Pattern colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})");
        Matcher m = colorPattern.matcher(color);
        return m.matches();
    }

    public boolean isRGB(String color) {
        Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
        Matcher m = c.matcher(color);
        return m.matches();
    }

    public boolean isRGBA(String color) {
        Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
        Matcher m = c.matcher(color);
        return m.matches();
    }

    public static int parseRGB(String color) {
        Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
        Matcher m = c.matcher(color);
        int r = Integer.parseInt(m.group(1));
        int g = Integer.parseInt(m.group(2));
        int b = Integer.parseInt(m.group(3));
        return Color.argb(1, r, g, b);
    }

    public static int parseRGBA(String color) {
        Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
        Matcher m = c.matcher(color);
        int r = Integer.parseInt(m.group(1));
        int g = Integer.parseInt(m.group(2));
        int b = Integer.parseInt(m.group(3));
        int a = Integer.parseInt(m.group(4));
        return Color.argb(a, r, g, b);
    }
}
