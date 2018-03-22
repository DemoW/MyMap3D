package com.gdut.water.mymap3d.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amap.api.maps.AMap;
import com.amap.api.maps.UiSettings;

/**
 * Created by Water on 2017/4/19.
 */

public class BaseRefresh {
    private static SharedPreferences defaultPreferences;
    private static Context mContext;

    public static void init(Context context){
        mContext = context;
    }

    public static void showRefresh(AMap aMap){
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int mapType = Integer.valueOf(defaultPreferences.getString("MapType","1"));
        aMap.setMapType(mapType);

        aMap.showIndoorMap(defaultPreferences.getBoolean("MapIndoor",false));//室内地图
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setScaleControlsEnabled(defaultPreferences.getBoolean("MapScale",false));//比例尺
        uiSettings.setAllGesturesEnabled(defaultPreferences.getBoolean("MapGestures",false));//手势
        int logoPosition = Integer.valueOf(defaultPreferences.getString("MapLogo","0"));
        uiSettings.setLogoPosition(logoPosition);
    }
}
