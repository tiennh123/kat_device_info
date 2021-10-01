package com.notrait.deviceid;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * DeviceIdPlugin
 */
public class DeviceIdPlugin implements MethodCallHandler {
    private final Activity activity;
    private final TelephonyManager mTelephonyManager;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {

        final MethodChannel channel = new MethodChannel(registrar.messenger(), "device_id");
        channel.setMethodCallHandler(new DeviceIdPlugin(registrar.activity()));
    }

    private DeviceIdPlugin(Activity activity) {
        this.activity = activity;
        mTelephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
    }

    private boolean isSimStateReady() {
        return TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState();
    }

    private String getCarrierName() {
        String networkOperatorName = mTelephonyManager.getNetworkOperatorName();
        if (networkOperatorName == null) return "";
        return networkOperatorName;
    }

    @SuppressLint("HardwareIds")
    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "getID":
                result.success(Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID));
                break;
            case "getIMEI": {
                TelephonyManager manager = (TelephonyManager) activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String imei = manager.getImei();
                    if (imei == null) {
                        result.error("1", "Error getting IMEI", "");
                    }
                    result.success(imei);
                } else {
                    result.error("1", "IMEI is not available for API versions lower than 26", "");
                }
                break;
            }
            case "getMEID": {
                TelephonyManager manager = (TelephonyManager) activity.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String imei = manager.getMeid();
                    if (imei == null) {
                        result.error("1", "Error getting MEID", "");
                    }
                    result.success(imei);
                } else {
                    result.error("1", "MEID is not available for API versions lower than 26", "");
                }
                break;
            }
            case "getGsf": {
                Uri URI = Uri.parse("content://com.google.android.gsf.gservices");
                String ID_KEY = "android_id";
                String params[] = {ID_KEY};
                Cursor c = activity.getContentResolver().query(URI, null, null, params, null);
                if (!c.moveToFirst() || c.getColumnCount() < 2) {
                    result.error("1", "Error getting Gsf", "");
                }
                try
                {
                    result.success(Long.toHexString(Long.parseLong(c.getString(1))));
                }
                catch (NumberFormatException e)
                {
                    result.error("1", "Error getting Gsf", "");
                }
                break;
            }
            case "getMacAddress": {
                try {
                    List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                    for (NetworkInterface nif : all) {
                        byte[] macBytes = nif.getHardwareAddress();
                        if (macBytes == null) {
                            result.success("02:00:00:00:00:00");
                        }

                        StringBuilder res1 = new StringBuilder();
                        for (byte b : macBytes) {
                            res1.append(String.format("%02X:",b));
                        }

                        if (res1.length() > 0) {
                            res1.deleteCharAt(res1.length() - 1);
                        }
                        result.success(res1.toString());
                    }
                } catch (Exception ex) {
                    result.success("02:00:00:00:00:00");
                }
                break;
            }
            case "carrierName": {
                if (!isSimStateReady()) {
                    result.success("");
                    return;
                }
                result.success(getCarrierName());
                break;
            }
            default:
                result.notImplemented();
                break;
        }
    }
}
