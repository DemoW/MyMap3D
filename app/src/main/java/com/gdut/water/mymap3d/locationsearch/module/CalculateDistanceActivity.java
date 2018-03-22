package com.gdut.water.mymap3d.locationsearch.module;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.base.BaseActivity;
import com.gdut.water.mymap3d.base.BaseRefresh;
import com.gdut.water.mymap3d.util.Constants;

/**
 * 两点间距离计算 示例
 */
public class CalculateDistanceActivity extends BaseActivity implements OnMarkerDragListener {
	private AMap aMap;
	private MapView mapView;
	private LatLng latlngA;
	private LatLng latlngB;
	private Marker makerA;
	private Marker makerB;
	private TextView Text;
	private float distance;
	private double area;

	//初始化sp
	SharedPreferences sharedPreferences;
	private SharedPreferences defaultPreferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.arc_activity);

		BaseActivity.mToolbar.setTitle("当前位置测距/面积");
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		sharedPreferences = getSharedPreferences(Constants.BASIC_MAP_DATA,MODE_PRIVATE);

		init();

		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isFlush = defaultPreferences.getBoolean("MapTypeToAll",false);
		if(isFlush){
			BaseRefresh.init(this);
			BaseRefresh.showRefresh(aMap);
		}
		distance = AMapUtils.calculateLineDistance(makerA.getPosition(), makerB.getPosition());
		area =  AMapUtils.calculateArea(makerA.getPosition(),makerB.getPosition());
		Text.setText("长按Marker可拖动\n两点间距离为："+distance+"m"+"\n两点构成的方形面积为："+area/1000000+"km²");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.base_menu,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int itemId = item.getItemId();

		if(itemId == R.id.screenshot_menu){
			//截取当前图
			aMap.getMapScreenShot(this);
		}

		return super.onOptionsItemSelected(item);
	}

	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		Text = (TextView) findViewById(R.id.info_text);
	}
	private void setUpMap() {
		aMap.setOnMarkerDragListener(this);

		double latitude = Double.parseDouble(sharedPreferences.getString(Constants.CURRENT_LATITUDE,null));
		double longitude = Double.parseDouble(sharedPreferences.getString(Constants.CURRENT_LONGITUDE,null));
		LatLng latLng = new LatLng(latitude,longitude);

		latlngA = new LatLng(latitude+0.001,longitude+0.004);
		latlngB = new LatLng(latitude-0.001,longitude-0.003);
		makerA = aMap.addMarker(new MarkerOptions().position(latlngA)
				.draggable(true)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
		makerB = aMap.addMarker(new MarkerOptions().position(latlngB)
				.draggable(true)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
//		
	}

	/**
	 *  在marker拖动过程中回调此方法, 这个marker的位置可以通过getPosition()方法返回。
	 *  这个位置可能与拖动的之前的marker位置不一样。
	 *  marker 被拖动的marker对象。
     */
	@Override
	public void onMarkerDrag(Marker marker) {

		distance = AMapUtils.calculateLineDistance(makerA.getPosition(), makerB.getPosition());
		area =  AMapUtils.calculateArea(makerA.getPosition(),makerB.getPosition());
		Text.setText("长按Marker可拖动\n两点间距离为："+distance+"m"+"\n两点构成的方形面积为："+area/1000000+"km²");
	}

	/**
	 * 在marker拖动完成后回调此方法, 这个marker的位置可以通过getPosition()方法返回。
	 * 这个位置可能与拖动的之前的marker位置不一样。
	 * marker 被拖动的marker对象。
	 */
	@Override
	public void onMarkerDragEnd(Marker arg0) {
		
	}

	/** 当marker开始被拖动时回调此方法, 这个marker的位置可以通过getPosition()方法返回。
	 * 这个位置可能与拖动的之前的marker位置不一样。
	 * marker 被拖动的marker对象。
	 */
	@Override
	public void onMarkerDragStart(Marker arg0) {
		
	}


	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

}
