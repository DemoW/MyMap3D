package com.gdut.water.mymap3d.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.gdut.water.mymap3d.data.dao.GeocoderDao;
import com.gdut.water.mymap3d.data.dao.QueryDao;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.gdut.water.mymap3d.util.DbUtils.getLiteOrm;

/**
 * Created by Water on 2017/4/2.
 */

//地理编码和逆地理编码的实现工具类
public class Geocoder implements GeocodeSearch.OnGeocodeSearchListener {
    private GeocodeSearch geocoderSearch;

    private Context mContext;
    private int mType = 0;
    //sp存储
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    public  Geocoder(Context context){
        mContext = context;

        init(context);
    }
    public Geocoder(Context context,int type){
        mContext = context;
        mType = type;
        init(context);
    }

    private void init(Context context){
        geocoderSearch = new GeocodeSearch(context);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    //逆地理编码，经纬度转为具体的地址信息
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                RegeocodeAddress address = result.getRegeocodeAddress();
                RegeocodeQuery query = result.getRegeocodeQuery();
                addRegecoder(query,address);

                SharedPreferencesUtil sp = new SharedPreferencesUtil(mContext);
                sp.setParam(Constants.CURRENT_CITY,address.getCity());


            }else {
                Log.e("Regeocoder","未查询到逆地理编码结果");
            }
        } else {
            Log.e("Regeocoder","逆地理编码失败");
        }
    }



    /**
     * 响应地理编码
     */
    public void getLatlon(String name, String city) {
        GeocodeQuery query = new GeocodeQuery(name, city);// 第一个参数表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode，
        geocoderSearch.getFromLocationNameAsyn(query);// 设置同步地理编码请求
    }

    //地理编码:将具体的地址信息转为经纬度
    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeQuery query = result.getGeocodeQuery();
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                addGeocoder(query,address);


                SharedPreferencesUtil sp = new SharedPreferencesUtil(mContext);
                sp.setParam(Constants.DESTINY_CITY,address.getCity());

            } else {
                Log.e("Geocoder","未查询到地理编码结果");
            }
        } else {
            Log.e("Geocoder","地理编码失败");
        }
    }

    private void addGeocoder(GeocodeQuery query,GeocodeAddress address){
        if(getLiteOrm() == null){
            DbUtils.openDB(mContext,Constants.DB);
        }
        GeocoderDao geocoderDao = new GeocoderDao();
        geocoderDao.setName(query.getLocationName());
        geocoderDao.setLatitude(address.getLatLonPoint().getLatitude());
        geocoderDao.setLongitude(address.getLatLonPoint().getLongitude());
        geocoderDao.setAddress(address.getFormatAddress());
        geocoderDao.setCity(address.getCity());
        geocoderDao.setBuilding(address.getBuilding());
        geocoderDao.setDistract(address.getDistrict());
        geocoderDao.setCreateTime(getTodayDate());
        geocoderDao.setNeighborhood(address.getNeighborhood());
        geocoderDao.setCategory(-1);

        //插入到Query表
        QueryDao queryDao = new QueryDao();
        queryDao.setName(query.getLocationName());
        queryDao.setCategory(-1);
        queryDao.setLatitude(address.getLatLonPoint().getLatitude());
        queryDao.setLongitude(address.getLatLonPoint().getLongitude());
        queryDao.setType(mType);

        DbUtils.getLiteOrm().insert(queryDao,ConflictAlgorithm.Fail);
        long flag = DbUtils.getLiteOrm().insert(geocoderDao, ConflictAlgorithm.Fail);
        if(flag>0){
            Log.e("Geocoder","insert successfully");
        }

    }

    private void addRegecoder(RegeocodeQuery query,RegeocodeAddress address){
        if(getLiteOrm() == null){
            DbUtils.openDB(mContext,Constants.DB);
        }
        GeocoderDao geocoderDao = new GeocoderDao();
//        geocoderDao.setName(query.getLocationName());
        geocoderDao.setLatitude(query.getPoint().getLatitude());
        geocoderDao.setLongitude(query.getPoint().getLongitude());
        geocoderDao.setAddress(address.getFormatAddress());
        geocoderDao.setCity(address.getCity());
        geocoderDao.setBuilding(address.getBuilding());
        geocoderDao.setDistract(address.getDistrict());
        geocoderDao.setCreateTime(getTodayDate());
        geocoderDao.setNeighborhood(address.getNeighborhood());
        geocoderDao.setCategory(0);

        long flag = DbUtils.getLiteOrm().insert(geocoderDao,ConflictAlgorithm.Fail);
        if(flag>0){
            Log.e("Regeocoder","insert successfully");
        }

    }

    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }



}
