package com.gdut.water.mymap3d.mylocation;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.data.dao.HistoryDao;
import com.gdut.water.mymap3d.data.dao.LocationsDao;
import com.gdut.water.mymap3d.mylocation.module.NewInputTipsActivity;
import com.gdut.water.mymap3d.overlay.PoiOverlay;
import com.gdut.water.mymap3d.util.Constants;
import com.gdut.water.mymap3d.util.DbUtils;
import com.gdut.water.mymap3d.util.Geocoder;
import com.gdut.water.mymap3d.util.SharedPreferencesUtil;
import com.gdut.water.mymap3d.util.ToastUtil;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.gdut.water.mymap3d.util.DbUtils.getLiteOrm;

/**
 * A simple {@link Fragment} subclass.
 */
public class MySiteFragment extends Fragment implements
        OnMarkerClickListener, InfoWindowAdapter,OnMapScreenShotListener,
        OnPoiSearchListener, OnClickListener,AMap.OnMyLocationChangeListener {

    private TextureMapView textureMapView;
    private AMap mAMap;
    private String mKeyWords = "";// 要输入的poi搜索关键字
    private ProgressDialog progDialog = null;// 搜索时进度条

    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private TextView mKeywordsTextView;
    private Marker mPoiMarker;
    private ImageView mCleanKeyWords;

    public static final int REQUEST_CODE = 100;
    public static final int RESULT_CODE_INPUTTIPS = 101;
    public static final int RESULT_CODE_KEYWORDS = 102;

    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    //sp存储

    SharedPreferences defaultPreferences;
    SharedPreferencesUtil sp;

    private int Map_Type;

    public MySiteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_site, container, false);
//        mCleanKeyWords = (ImageView)rootView.findViewById(R.id.clean_keywords);
//        mCleanKeyWords.setOnClickListener(this);
//        //init();
//        mKeyWords = "";
        return rootView;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        textureMapView = (TextureMapView) getView().findViewById(R.id.map);

        if (textureMapView != null) {
            textureMapView.onCreate(savedInstanceState);
            mAMap = textureMapView.getMap();

            setUpMap();
            sp = new SharedPreferencesUtil(getActivity());
        }

        mCleanKeyWords = (ImageView)getView().findViewById(R.id.clean_keywords);
        mCleanKeyWords.setOnClickListener(this);
        init();
        showRefresh();
        mKeyWords = "";
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
//        if (mAMap == null) {
//            mAMap = ((SupportMapFragment) getActivity().getSupportFragmentManager()
//                    .findFragmentById(R.id.map)).getMap();
//
//            setUpMap();
//        }

        mKeywordsTextView = (TextView) getView().findViewById(R.id.main_keywords);
        mKeywordsTextView.setOnClickListener(this);
//        CameraPosition cameraPosition = mAMap.getCameraPosition();//相机位置，这个类包含了所有的可视区域的位置参数。
//        LatLng latlng = cameraPosition.target;   //获取当前位置的坐标点
//        Log.e("MySiteFragment","Now LocationsDao:"+latlng.toString());

    }

    /**
     * 设置页面监听,同时设置定位蓝点
     */
    private void setUpMap() {
        //设置定位蓝点
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.strokeColor(STROKE_COLOR); //设置定位蓝点精度圆圈的边框颜色的方法
        myLocationStyle.radiusFillColor(FILL_COLOR);  //设置定位蓝点精度圆圈的填充颜色的方法

        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Map_Type = Integer.valueOf(defaultPreferences.getString("MapType","1"));
        mAMap.setMapType(Map_Type);// 设置地图模式

        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        //设置页面监听
        mAMap.setOnMarkerClickListener(this);// 添加点击marker监听事件
        mAMap.setInfoWindowAdapter(this);// 添加显示infowindow监听事件
        mAMap.setOnMyLocationChangeListener(this); //添加位置改变监听事件
        mAMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    public void showRefresh(){
        Map_Type = Integer.valueOf(defaultPreferences.getString("MapType","1"));
        mAMap.setMapType(Map_Type);// 设置地图模式
        mAMap.showIndoorMap(defaultPreferences.getBoolean("MapIndoor",false));//室内地图
        UiSettings uiSettings = mAMap.getUiSettings();
        uiSettings.setScaleControlsEnabled(defaultPreferences.getBoolean("MapScale",false));//比例尺
        uiSettings.setAllGesturesEnabled(defaultPreferences.getBoolean("MapGestures",false));//手势
        int logoPosition = Integer.valueOf(defaultPreferences.getString("MapLogo","0"));
        uiSettings.setLogoPosition(logoPosition);
    }
    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(getActivity());
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + mKeyWords);
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String keywords) {
        showProgressDialog();// 显示进度框
        currentPage = 0;
        // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query = new PoiSearch.Query(keywords, "", Constants.DEFAULT_CITY);
        // 设置每页最多返回多少条poiitem
        query.setPageSize(10);
        // 设置查第一页
        query.setPageNum(currentPage);

        poiSearch = new PoiSearch(getActivity(), query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_keywords:
                Intent intent = new Intent(getActivity(), NewInputTipsActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.clean_keywords:
                mKeywordsTextView.setText("");
                mAMap.clear();
                mCleanKeyWords.setVisibility(View.GONE);
            default:
                break;
        }
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        //Bundle savedInstanceState ?
        Bundle savedInstanceState = null;
        View view = getLayoutInflater(savedInstanceState).inflate(R.layout.poikeywordsearch_uri,
                null);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());

        ImageButton button = (ImageButton) view
                .findViewById(R.id.start_amap_app);
        // 调起高德地图app
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAMapNavi(marker);
            }
        });
        return view;
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
        ToastUtil.show(getActivity(), infomation);

    }
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.e("MySiteFragment","onMarkerClick");
        marker.showInfoWindow();
        if(marker!=null){
            LocationsDao locationsDao = new LocationsDao();
            locationsDao.setName(marker.getTitle());
            locationsDao.setAddress(marker.getSnippet());
            //locationsDao.setDistract(tip.getDistrict());
            locationsDao.setLatitude(marker.getPosition().latitude);
            locationsDao.setLongitude(marker.getPosition().longitude);
            locationsDao.setCreateTime(getTodayDate());

            //插入数据到history表
            HistoryDao historyDao = new HistoryDao();
            historyDao.setName(marker.getTitle());
            historyDao.setAddress(marker.getSnippet());
            //historyDao.setDistract(tip.getDistrict());
            historyDao.setLatitude(marker.getPosition().latitude);
            historyDao.setLongitude(marker.getPosition().longitude);
            historyDao.setCreateTime(getTodayDate());

            if (getLiteOrm() == null) {
                DbUtils.openDB(getActivity(), Constants.DB);
            }
            long flag = getLiteOrm().insert(locationsDao);
            if(flag>0){
                getLiteOrm().insert(historyDao, ConflictAlgorithm.Fail);
            }

        }
        return false;
    }

    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
    /**
     * POI信息查询回调方法
     */
    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        dissmissProgressDialog();// 隐藏对话框
        if (rCode == 1000) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    poiResult = result;
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息

                    if (poiItems != null && poiItems.size() > 0) {
                        mAMap.clear();// 清理之前的图标
                        PoiOverlay poiOverlay = new PoiOverlay(mAMap, poiItems);
                        poiOverlay.removeFromMap();
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();
                    } else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(getActivity(),
                                R.string.no_result);
                    }
                }
            } else {
                ToastUtil.show(getActivity(),
                        R.string.no_result);
            }
        } else {
            ToastUtil.showerror(getActivity(), rCode);
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * 输入提示activity选择结果后的处理逻辑
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CODE_INPUTTIPS && data
                != null) {
            mAMap.clear();
            Tip tip = data.getParcelableExtra(Constants.EXTRA_TIP);
            if (tip.getPoiID() == null || tip.getPoiID().equals("")) {
                doSearchQuery(tip.getName());
            } else {
                addTipMarker(tip);
            }
            mKeywordsTextView.setText(tip.getName());
            if(!tip.getName().equals("")){
                mCleanKeyWords.setVisibility(View.VISIBLE);
            }

            //插入数据到表中
        } else if (resultCode == RESULT_CODE_KEYWORDS && data != null) {
            mAMap.clear();
            String keywords = data.getStringExtra(Constants.KEY_WORDS_NAME);
            if(keywords != null && !keywords.equals("")){
                doSearchQuery(keywords);
            }
            mKeywordsTextView.setText(keywords);
            if(!keywords.equals("")){
                mCleanKeyWords.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 用marker展示输入提示list选中数据
     *
     * @param tip
     */
    private void addTipMarker(Tip tip) {
        if (tip == null) {
            return;
        }
        mPoiMarker = mAMap.addMarker(new MarkerOptions());
        LatLonPoint point = tip.getPoint();
        if (point != null) {
            LatLng markerPosition = new LatLng(point.getLatitude(), point.getLongitude());
            mPoiMarker.setPosition(markerPosition);
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 17));
        }
        mPoiMarker.setTitle(tip.getName());
        mPoiMarker.setSnippet(tip.getAddress());
    }
    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        textureMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        textureMapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        textureMapView.onPause();

    }
    /**
     * 方法必须重写
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        textureMapView.onDestroy();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    //监听位置改变
    @Override
    public void onMyLocationChange(Location location) {
        LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(),location.getLongitude());
        Geocoder geocoder = new Geocoder(getActivity());
        geocoder.getAddress(latLonPoint);
        sp.setParam(Constants.CURRENT_LATITUDE,String.valueOf(location.getLatitude()));
        sp.setParam(Constants.CURRENT_LONGITUDE,String.valueOf(location.getLongitude()));
        Log.e("MySiteFragment","current_location:"+latLonPoint.toString());

    }
    /**
     * 调起高德地图导航功能，如果没安装高德地图，会进入异常，可以在异常中处理，调起高德地图app的下载页面
     */
    public void startAMapNavi(Marker marker) {
        // 构造导航参数
        NaviPara naviPara = new NaviPara();
        // 设置终点位置
        naviPara.setTargetPoint(marker.getPosition());
        // 设置导航策略，这里是避免拥堵
        naviPara.setNaviStyle(NaviPara.DRIVING_AVOID_CONGESTION);

        // 调起高德地图导航
        try {
            AMapUtils.openAMapNavi(naviPara, getActivity());
        } catch (com.amap.api.maps.AMapException e) {

            // 如果没安装会进入异常，调起下载页面
            AMapUtils.getLatestAMapApp(getActivity());

        }

    }

    /**
     * 对地图进行截屏
     */
    public void getMapScreenShot(View v) {
        mAMap.getMapScreenShot(this);
    }

    /**
     * 截屏时回调的方法。
     *
     * @param bitmap 调用截屏接口返回的截屏对象。
     */
    @Override
    public void onMapScreenShot(Bitmap bitmap) {

    }

    /**
     * 带有地图渲染状态的截屏回调方法。
     * 根据返回的状态码，可以判断当前视图渲染是否完成。
     *
     * @param bitmap 调用截屏接口返回的截屏对象。
     * @param arg1 地图渲染状态， 1：地图渲染完成，0：未绘制完成
     */
    @Override
    public void onMapScreenShot(Bitmap bitmap, int arg1) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        if(null == bitmap){
            return;
        }
        try {
            //如果手机插入SD卡，而且应用程序具有访问SD卡的权限
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/mymap3d_" + sdf.format(new Date()) + ".png");
                boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuffer buffer = new StringBuffer();
                if (b)
                    buffer.append("截屏成功 ");
                else {
                    buffer.append("截屏失败 ");
                }
                if (arg1 != 0)
                    buffer.append("地图渲染完成，截屏无网格");
                else {
                    buffer.append("地图未渲染完成，截屏有网格");
                }
                ToastUtil.show(getActivity(), buffer.toString());
            }else {
                ToastUtil.show(getActivity(),"抱歉。手机未安装SD卡或无权限");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
