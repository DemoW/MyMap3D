package com.gdut.water.mymap3d.base;

import android.content.Context;
import android.util.Log;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.data.dao.HistoryDao;
import com.gdut.water.mymap3d.data.dao.LocationsDao;
import com.gdut.water.mymap3d.data.dao.QueryDao;
import com.gdut.water.mymap3d.util.Constants;
import com.gdut.water.mymap3d.util.DbUtils;
import com.gdut.water.mymap3d.util.SharedPreferencesUtil;
import com.gdut.water.mymap3d.util.ToastUtil;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.gdut.water.mymap3d.util.DbUtils.getLiteOrm;

/**
 * Created by Water on 2017/4/23.
 */

public class PoiSearchUtil implements PoiSearch.OnPoiSearchListener {

    private String mKeyword;
    private String mPoiType;
    private String mCity;
    private int mCategory;
    private static Context mContext;
    public static LatLonPoint start;
    public static LatLonPoint end;

    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private PoiResult poiResult; // poi返回的结果


    public PoiSearchUtil(Context context, String name, String type, String city,int category) {
        mContext = context;
        mKeyword = name;
        mPoiType = type;
        mCity = city;
        mCategory = category;
    }
    public void doSearchQuery() {

        query = new PoiSearch.Query(mKeyword, mPoiType, mCity);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(2);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);// 设置查第一页

        poiSearch = new PoiSearch(mContext, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    public LatLonPoint getLatLonPoint(int category){
        LatLonPoint point = null;
        if(getLiteOrm() == null){
            DbUtils.openDB(mContext,Constants.DB);
        }

        if(category == 1){
            List<QueryDao> queryDaos = getLiteOrm().query(new QueryBuilder<QueryDao>(QueryDao.class)
                    .whereEquals("category",1)
                    .appendOrderDescBy("_id"));
            if(!queryDaos.isEmpty()) {
                double latitude = Double.valueOf(queryDaos.get(0).getLatitude());
                double longitude = Double.valueOf(queryDaos.get(0).getLongitude());
                LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
                point = latLonPoint;
                return point;
            }
        }else if (category == 2){
            List<QueryDao> queryDaos = getLiteOrm().query(new QueryBuilder<QueryDao>(QueryDao.class)
                    .whereEquals("category",2)
                    .appendOrderDescBy("_id"));
            if(!queryDaos.isEmpty()) {
                double latitude = Double.valueOf(queryDaos.get(0).getLatitude());
                double longitude = Double.valueOf(queryDaos.get(0).getLongitude());
                LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
                point = latLonPoint;
                return point;
            }
        }else {
            Log.e("PoiSearchUtil","获取起点终点失败:");
        }
        return point;
    }

    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {

        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        double latitude = poiItems.get(0).getLatLonPoint().getLatitude();
                        double longitude = poiItems.get(0).getLatLonPoint().getLongitude();
                        PoiItem poiItem = poiItems.get(0);
//                        if(getLiteOrm() == null){
//                            DbUtils.openDB(mContext, Constants.DB);
//                        }
                        //int i = DbUtils.getLiteOrm().deleteAll(QueryDao.class);

                        QueryDao queryDao = new QueryDao();
                        queryDao.setName(mKeyword);
                        queryDao.setLatitude(latitude);
                        queryDao.setLongitude(longitude);
                        queryDao.setCategory(mCategory);
                        Log.e("PoiSearchUtil","category:"+mCategory);
                        getLiteOrm().insert(queryDao);


                        String distract = poiItem.getCityName()+poiItem.getAdName();
                        LocationsDao locationsDao = new LocationsDao();
                        locationsDao.setName(poiItem.getTitle());
                        locationsDao.setAddress(poiItem.getSnippet());
                        locationsDao.setDistract(distract);
                        locationsDao.setLatitude(poiItem.getLatLonPoint().getLatitude());
                        locationsDao.setLongitude(poiItem.getLatLonPoint().getLongitude());
                        locationsDao.setCreateTime(getTodayDate());
                        //插入数据到history表
                        HistoryDao historyDao = new HistoryDao();
                        historyDao.setName(poiItem.getTitle());
                        historyDao.setAddress(poiItem.getSnippet());
                        historyDao.setDistract(distract);
                        historyDao.setLatitude(poiItem.getLatLonPoint().getLatitude());
                        historyDao.setLongitude(poiItem.getLatLonPoint().getLongitude());
                        historyDao.setCreateTime(getTodayDate());

                        long flag = getLiteOrm().insert(locationsDao);
                        if(flag>0){
                            getLiteOrm().insert(historyDao, ConflictAlgorithm.Fail);
                        }

                        //起点
                        if(mCategory == 1){
                            SharedPreferencesUtil sp = new SharedPreferencesUtil(mContext);
                            sp.setParam(Constants.START_LATITUDE,String.valueOf(latitude));
                            sp.setParam(Constants.START_LONGITUDE,String.valueOf(longitude));
                            start = poiItem.getLatLonPoint();
                        }

                        //终点
                        if(mCategory == 2){
                            SharedPreferencesUtil sp = new SharedPreferencesUtil(mContext);
                            sp.setParam(Constants.END_LATITUDE,String.valueOf(latitude));
                            sp.setParam(Constants.END_LONGITUDE,String.valueOf(longitude));
                            end = poiItem.getLatLonPoint();
                        }


                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(mContext,
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(mContext,
                        R.string.no_result);
            }
        } else {
            ToastUtil.showerror(mContext, rCode);
        }

    }

    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(mContext, infomation);

    }

}
