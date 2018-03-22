package com.gdut.water.mymap3d.locationsearch.module;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Gradient;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.TileOverlayOptions;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.base.BaseActivity;
import com.gdut.water.mymap3d.base.BaseRefresh;
import com.gdut.water.mymap3d.data.dao.LocationsDao;
import com.gdut.water.mymap3d.util.Constants;
import com.gdut.water.mymap3d.util.DbUtils;
import com.gdut.water.mymap3d.util.SharedPreferencesUtil;

import java.util.Arrays;
import java.util.List;

import static com.gdut.water.mymap3d.util.DbUtils.getLiteOrm;

public class HeatMapActivity extends BaseActivity {

	private MapView mMapView;
	private AMap mAMap;
	private SharedPreferences defaultPreferences;
	private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
			Color.argb(0, 0, 255, 255),
			Color.argb(255 / 3 * 2, 0, 255, 0),
			Color.rgb(125, 191, 0),
			Color.rgb(185, 71, 0),
			Color.rgb(255, 0, 0)
			};

	public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = { 0.0f,
			0.10f, 0.20f, 0.60f, 1.0f };

	public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(
			ALT_HEATMAP_GRADIENT_COLORS, ALT_HEATMAP_GRADIENT_START_POINTS);

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.heatmap_activity);
		BaseActivity.mToolbar.setTitle("历史搜索构建热力图");
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		mAMap = mMapView.getMap();
		initMap();
		initDataAndHeatMap();
		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isFlush = defaultPreferences.getBoolean("MapTypeToAll",false);
		if(isFlush){
			BaseRefresh.init(this);
			BaseRefresh.showRefresh(mAMap);
		}
	}

	private void initMap() {
		SharedPreferencesUtil sp = new SharedPreferencesUtil(this);
		double latitude = Double.parseDouble(sp.getParam(Constants.CURRENT_LATITUDE,"23.043806"));
		double longitude = Double.parseDouble(sp.getParam(Constants.CURRENT_LONGITUDE,"113.386496"));
		//mStartPoint = new LatLonPoint(startLatitude,startLongitude);
		LatLng point = new LatLng(latitude,longitude);
		//当前地图移到起点
		mAMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
				point, 9, 30, 30)));
	}

	private void initDataAndHeatMap() {

		if(getLiteOrm() == null){
			DbUtils.openDB(this,Constants.DB);
		}
		List<LocationsDao> locationsDaos =DbUtils.getLiteOrm().query(LocationsDao.class);
		if(!locationsDaos.isEmpty()){
			// 第一步： 生成热力点坐标列表
			LatLng[] latLngs = new LatLng[locationsDaos.size()];
			for (int i = 0;i<locationsDaos.size();i++){
				latLngs[i] = new LatLng(locationsDaos.get(i).getLatitude(),locationsDaos.get(i).getLongitude());
			}

			// 第二步： 构建热力图 TileProvider
			HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
			builder.data(Arrays.asList(latLngs)) // 设置热力图绘制的数据
					.gradient(ALT_HEATMAP_GRADIENT); // 设置热力图渐变，有默认值 DEFAULT_GRADIENT，可不设置该接口
			// Gradient 的设置可见参考手册
			// 构造热力图对象
			HeatmapTileProvider heatmapTileProvider = builder.build();

			// 第三步： 构建热力图参数对象
			TileOverlayOptions tileOverlayOptions = new TileOverlayOptions();
			tileOverlayOptions.tileProvider(heatmapTileProvider); // 设置瓦片图层的提供者

			// 第四步： 添加热力图
			mAMap.addTileOverlay(tileOverlayOptions);
		} else {
			Toast.makeText(this,"热力图数据为空！",Toast.LENGTH_SHORT).show();
		}
//		LatLng[] latlngs = new LatLng[500];
//		double x = 39.904979;
//		double y = 116.40964;
//
//		for (int i = 0; i < 500; i++) {
//			double x_ = 0;
//			double y_ = 0;
//			x_ = Math.random() * 0.5 - 0.25;
//			y_ = Math.random() * 0.5 - 0.25;
//			latlngs[i] = new LatLng(x + x_, y + y_);
//		}


 
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}
}
