package com.gdut.water.mymap3d.morefunc;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.util.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationDetailsFragment extends Fragment implements
        RadioGroup.OnCheckedChangeListener,
        View.OnClickListener {
    private RadioGroup rgLocationMode;
    private EditText etInterval;
    private EditText etHttpTimeout;
    private CheckBox cbOnceLocation;
    private CheckBox cbAddress;
    private CheckBox cbGpsFirst;
    private CheckBox cbCacheAble;
    private CheckBox cbOnceLastest;
    private CheckBox cbSensorAble;
    private TextView tvResult;
    private Button btLocation;

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = new AMapLocationClientOption();

    public LocationDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_details, container, false);

        initView(rootView);

        //初始化定位
        initLocation();

        return rootView;
    }
    //初始化控件
    private void initView(View rootView){
        rgLocationMode = (RadioGroup) rootView.findViewById(R.id.rg_locationMode);

        etInterval = (EditText) rootView.findViewById(R.id.et_interval);
        etHttpTimeout = (EditText) rootView.findViewById(R.id.et_httpTimeout);

        cbOnceLocation = (CheckBox)rootView.findViewById(R.id.cb_onceLocation);
        cbGpsFirst = (CheckBox) rootView.findViewById(R.id.cb_gpsFirst);
        cbAddress = (CheckBox) rootView.findViewById(R.id.cb_needAddress);
        cbCacheAble = (CheckBox) rootView.findViewById(R.id.cb_cacheAble);
        cbOnceLastest = (CheckBox) rootView.findViewById(R.id.cb_onceLastest);
        cbSensorAble = (CheckBox)rootView.findViewById(R.id.cb_sensorAble);

        tvResult = (TextView) rootView.findViewById(R.id.tv_result);
        btLocation = (Button) rootView.findViewById(R.id.bt_location);

        rgLocationMode.setOnCheckedChangeListener(this);
        btLocation.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("LocationDetailsFragment","onDestroy");
        destroyLocation();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (null == locationOption) {
            locationOption = new AMapLocationClientOption();
        }
        switch (checkedId) {
            case R.id.rb_batterySaving :
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                break;
            case R.id.rb_deviceSensors :
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
                break;
            case R.id.rb_hightAccuracy :
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                break;
            default :
                break;
        }
    }

    /**
     * 设置控件的可用状态
     */
    private void setViewEnable(boolean isEnable) {
        for(int i=0; i<rgLocationMode.getChildCount(); i++){
            rgLocationMode.getChildAt(i).setEnabled(isEnable);
        }
        etInterval.setEnabled(isEnable);
        etHttpTimeout.setEnabled(isEnable);
        cbOnceLocation.setEnabled(isEnable);
        cbGpsFirst.setEnabled(isEnable);
        cbAddress.setEnabled(isEnable);
        cbCacheAble.setEnabled(isEnable);
        cbOnceLastest.setEnabled(isEnable);
        cbSensorAble.setEnabled(isEnable);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_location) {
            if (btLocation.getText().equals(
                    getResources().getString(R.string.startLocation))) {
                setViewEnable(false);
                btLocation.setText(getResources().getString(
                        R.string.stopLocation));
                tvResult.setText("正在定位...");
                startLocation();
            } else {
                setViewEnable(true);
                btLocation.setText(getResources().getString(
                        R.string.startLocation));
                stopLocation();
                tvResult.setText("定位停止");
            }
        }
    }

    private void initLocation(){
        //初始化client
        locationClient = new AMapLocationClient(getActivity().getApplicationContext());
        //设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     *
     */
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                String result = Utils.getLocationStr(loc);

                tvResult.setText(result);
            } else {
                tvResult.setText("定位失败，loc is null");
            }
        }
    };

    // 根据控件的选择，重新设置定位参数
    private void resetOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(cbAddress.isChecked());
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(cbGpsFirst.isChecked());
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(cbCacheAble.isChecked());
        // 设置是否单次定位
        locationOption.setOnceLocation(cbOnceLocation.isChecked());
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOption.setOnceLocationLatest(cbOnceLastest.isChecked());
        //设置是否使用传感器
        locationOption.setSensorEnable(cbSensorAble.isChecked());
        //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        String strInterval = etInterval.getText().toString();
        if (!TextUtils.isEmpty(strInterval)) {
            try{
                // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
                locationOption.setInterval(Long.valueOf(strInterval));
            }catch(Throwable e){
                e.printStackTrace();
            }
        }

        String strTimeout = etHttpTimeout.getText().toString();
        if(!TextUtils.isEmpty(strTimeout)){
            try{
                // 设置网络请求超时时间
                locationOption.setHttpTimeOut(Long.valueOf(strTimeout));
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始定位
     *
     *
     */
    private void startLocation(){
        //根据控件的选择，重新设置定位参数
        resetOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     *
     */
    private void stopLocation(){
        // 停止定位
        locationClient.stopLocation();
    }

    /**
     * 销毁定位
     */
    private void destroyLocation(){
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

}
