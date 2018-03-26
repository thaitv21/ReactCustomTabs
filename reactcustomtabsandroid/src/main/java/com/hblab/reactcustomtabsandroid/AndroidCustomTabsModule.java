package com.hblab.reactcustomtabsandroid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class AndroidCustomTabsModule extends ReactContextBaseJavaModule {

    private CustomTabsClient mCustomTabsClient = null;
    private CustomTabsSession mCustomTabsSession = null;
    private Context mContext;

    public AndroidCustomTabsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mContext = reactContext;
    }

    @Override
    public String getName() {
        return "CustomTabsModule";
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
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.intent.addCategory("android.intent.category.BROWSABLE");
        customTabsIntent.intent.setPackage("com.android.chrome");
        Activity activity = getCurrentActivity();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }
}
