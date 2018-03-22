package com.gdut.water.mymap3d.locationsearch.module;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.routepoisearch.RoutePOIItem;
import com.amap.api.services.routepoisearch.RoutePOISearch;
import com.amap.api.services.routepoisearch.RoutePOISearch.OnRoutePOISearchListener;
import com.amap.api.services.routepoisearch.RoutePOISearch.RoutePOISearchType;
import com.amap.api.services.routepoisearch.RoutePOISearchQuery;
import com.amap.api.services.routepoisearch.RoutePOISearchResult;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.base.BaseActivity;
import com.gdut.water.mymap3d.base.BaseRefresh;
import com.gdut.water.mymap3d.base.PoiSearchUtil;
import com.gdut.water.mymap3d.data.dao.QueryDao;
import com.gdut.water.mymap3d.overlay.DrivingRouteOverlay;
import com.gdut.water.mymap3d.util.AMapUtil;
import com.gdut.water.mymap3d.util.Constants;
import com.gdut.water.mymap3d.util.DbUtils;
import com.gdut.water.mymap3d.util.SharedPreferencesUtil;
import com.gdut.water.mymap3d.util.ToastUtil;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.gdut.water.mymap3d.util.DbUtils.getLiteOrm;

/**
 * 沿途搜索 示例
 */
public class RoutePOIActivity extends BaseActivity implements OnMapClickListener,
        OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter,
		OnRouteSearchListener, OnRoutePOISearchListener {
	private AMap aMap;
	private MapView mapView;
	private Context mContext;
	private RouteSearch mRouteSearch;
	private DriveRouteResult mDriveRouteResult;
	private LatLonPoint mStartPoint;//起点，116.335891,39.942295
	private LatLonPoint mEndPoint;//终点，116.481288,39.995576
	private final int ROUTE_TYPE_DRIVE = 1;
	private int mode = RouteSearch.DRIVING_SINGLE_DEFAULT;
	
	private ProgressDialog progDialog = null;// 搜索时进度条
	private myRoutePoiOverlay overlay;
	private TextView gasbtn,ATMbtn,Maibtn,Toibtn;
	private Button routeSearch;
	private AutoCompleteTextView startPlace,endPlace;
	private SharedPreferencesUtil sp;//初始化sp
	private SharedPreferences defaultPreferences;
	private String city;
	private View gasClick;
	private View AtmClick;
	private View maiClick;
	private View toiClick;
	private LatLng centerPoint;
	private boolean isSearch = true;
	public static final int UPDATE_VIEW = 1;
	public static final int UPDATE_MAP = 2;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.routepoi_activity);
		BaseActivity.mToolbar.setTitle("沿途搜索");
		mContext = this.getApplicationContext();
		mapView = (MapView) findViewById(R.id.route_map);
		mapView.onCreate(bundle);// 此方法必须重写
		sp = new SharedPreferencesUtil(this);
		init();


		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isFlush = defaultPreferences.getBoolean("MapTypeToAll",false);
		if(isFlush){
			BaseRefresh.init(this);
			BaseRefresh.showRefresh(aMap);
		}

		routeSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				onSearchClick();
			}
		});
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
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
				case UPDATE_VIEW:
					routeSearch.setText("正在进行路径搜索....");
					gasClick.setEnabled(true);
					AtmClick.setEnabled(true);
					maiClick.setEnabled(true);
					toiClick.setEnabled(true);
					startPlace.setEnabled(false);
					endPlace.setEnabled(false);
					break;
				case UPDATE_MAP:
					routeSearch.setText("开始进行沿途搜索");
					aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
							centerPoint, 12, 30, 30)));
					aMap.clear();// 清理地图上的所有覆盖物
					mStartPoint = null;
					mEndPoint = null;
					gasClick.setEnabled(false);
					AtmClick.setEnabled(false);
					maiClick.setEnabled(false);
					toiClick.setEnabled(false);
					startPlace.setEnabled(true);
					endPlace.setEnabled(true);
					break;
			}
		}
	};
	private void onSearchClick(){
		if( startPlace.getText().toString().trim().isEmpty()|| endPlace.getText().toString().trim().isEmpty()){
			Toast.makeText(getApplicationContext(),"起点或终点为空！",Toast.LENGTH_SHORT).show();
			return;
		}
		if( startPlace.getText().toString().trim().equals( endPlace.getText().toString().trim())){
			Toast.makeText(getApplicationContext(),"起点和终点相同！",Toast.LENGTH_SHORT).show();
			return;
		}

		if(isSearch){
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message message = new Message();
					message.what = UPDATE_VIEW;
//					String start = startPlace.getText().toString().trim();
//					String end = endPlace.getText().toString().trim();
//					initData(start,1);
//					initData(end,2);
					searchRouteResult(ROUTE_TYPE_DRIVE, mode);
					isSearch = false;
					handler.sendMessage(message);
				}
			}).start();
		}else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message message = new Message();
					message.what = UPDATE_MAP;
					mStartPoint = null;
					mEndPoint = null;
					isSearch = true;
					handler.sendMessage(message);
				}
			}).start();
		}
	}

	private LatLonPoint getData(int category){
		LatLonPoint point = null;
		if(getLiteOrm() == null){
			DbUtils.openDB(this,Constants.DB);
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
			Log.e("RoutePOIActivity","获取起点终点失败:");
		}
		return point;
	}
	/**
	 * 添加起点和终点标记
	 */
	private void setfromandtoMarker() {
		aMap.addMarker(new MarkerOptions()
		.position(AMapUtil.convertToLatLng(mStartPoint))
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
		aMap.addMarker(new MarkerOptions()
		.position(AMapUtil.convertToLatLng(mEndPoint))
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();	
		}
		mRouteSearch = new RouteSearch(this);
		mRouteSearch.setRouteSearchListener(this);
		gasbtn = (TextView)findViewById(R.id.gasbtn);
		ATMbtn= (TextView)findViewById(R.id.ATMbtn);
		Maibtn= (TextView)findViewById(R.id.Maibtn);
		Toibtn= (TextView)findViewById(R.id.Toibtn);

		startPlace = (AutoCompleteTextView) findViewById(R.id.start_place);

		endPlace = (AutoCompleteTextView) findViewById(R.id.end_place);
		routeSearch = (Button) findViewById(R.id.route_search);
		gasClick = findViewById(R.id.gas_click);
		AtmClick = findViewById(R.id.ATM_click);
		maiClick = findViewById(R.id.mai_click);
		toiClick = findViewById(R.id.toi_click);
		gasClick.setEnabled(false);
		AtmClick.setEnabled(false);
		maiClick.setEnabled(false);
		toiClick.setEnabled(false);
		city = sp.getParam(Constants.CURRENT_CITY,"广州市");
		double latitude = Double.parseDouble(sp.getParam(Constants.CURRENT_LATITUDE,"23.043806"));
		double longitude = Double.parseDouble(sp.getParam(Constants.CURRENT_LONGITUDE,"113.386496"));
		 centerPoint = new LatLng(latitude,longitude);
		//当前地图移到起点
		aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
				centerPoint, 10, 30, 30)));

		registerListener();
	}

	/**
	 * 注册监听
	 */
	private void registerListener() {
		aMap.setOnMapClickListener(RoutePOIActivity.this);
		aMap.setOnMarkerClickListener(RoutePOIActivity.this);
		aMap.setOnInfoWindowClickListener(RoutePOIActivity.this);
		aMap.setInfoWindowAdapter(RoutePOIActivity.this);

		startPlace.addTextChangedListener(startTextWatcher);
		startPlace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if(i == EditorInfo.IME_ACTION_DONE){
					String start = startPlace.getText().toString().trim();
					String end = endPlace.getText().toString().trim();
					initData(start,1);
					initData(end,2);
				}
				return false;
			}
		});
		endPlace.addTextChangedListener(endTextWatcher);
		endPlace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if(i == EditorInfo.IME_ACTION_DONE){
					String start = startPlace.getText().toString().trim();
					String end = endPlace.getText().toString().trim();
					initData(start,1);
					initData(end,2);
				}
				return false;
			}
		});
	}

	TextWatcher startTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String newText = s.toString().trim();
			if (!AMapUtil.IsEmptyOrNullString(newText)) {
				InputtipsQuery inputquery = new InputtipsQuery(newText, city);
				Inputtips inputTips = new Inputtips(RoutePOIActivity.this, inputquery);
				inputTips.setInputtipsListener(startInputtips);
				inputTips.requestInputtipsAsyn();
			}
		}
		@Override
		public void afterTextChanged(Editable editable) {}
	};
	Inputtips.InputtipsListener startInputtips = new Inputtips.InputtipsListener(){
		@Override
		public void onGetInputtips(List<Tip> tipList, int rCode) {
			if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
				List<String> listString = new ArrayList<String>();
				for (int i = 0; i < tipList.size(); i++) {
					listString.add(tipList.get(i).getName());
				}
				ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
						getApplicationContext(),
						R.layout.route_inputs, listString);
				startPlace.setAdapter(aAdapter);
				aAdapter.notifyDataSetChanged();
			} else {
				ToastUtil.showerror(getApplicationContext(), rCode);
			}
		}
	};
	TextWatcher endTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String newText = s.toString().trim();
			if (!AMapUtil.IsEmptyOrNullString(newText)) {
				InputtipsQuery inputquery = new InputtipsQuery(newText, city);
				Inputtips inputTips = new Inputtips(RoutePOIActivity.this, inputquery);
				inputTips.setInputtipsListener(endInputtips);
				inputTips.requestInputtipsAsyn();
			}
		}
		@Override
		public void afterTextChanged(Editable editable) {}
	};

	Inputtips.InputtipsListener endInputtips = new Inputtips.InputtipsListener(){
		@Override
		public void onGetInputtips(List<Tip> tipList, int rCode) {
			if (rCode == AMapException.CODE_AMAP_SUCCESS) {// 正确返回
				List<String> listString = new ArrayList<String>();
				for (int i = 0; i < tipList.size(); i++) {
					listString.add(tipList.get(i).getName());
				}
				ArrayAdapter<String> aAdapter = new ArrayAdapter<String>(
						getApplicationContext(),
						R.layout.route_inputs, listString);
				endPlace.setAdapter(aAdapter);
				aAdapter.notifyDataSetChanged();
			} else {
				ToastUtil.showerror(getApplicationContext(), rCode);
			}
		}
	};

	private void initData(String name,int category){
		PoiSearchUtil poiSearchUtil = new PoiSearchUtil(mContext,name,"",city,category);
		poiSearchUtil.doSearchQuery();
//		mStartPoint = poiSearchUtil.getLatLonPoint(1);
//		mEndPoint = poiSearchUtil.getLatLonPoint(2);
//		Log.e("start",mStartPoint.toString());
//		Log.e("end",mEndPoint.toString());
	}


	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 开始搜索路径规划方案
	 */
	public void searchRouteResult(int routeType, int mode) {
//		mStartPoint = PoiSearchUtil.start;
//		mEndPoint = PoiSearchUtil.end;
		mStartPoint = getData(1);
		mEndPoint = getData(2);
		if (mStartPoint == null) {
			ToastUtil.show(mContext, "起点未设置");
			return;
		}
		if (mEndPoint == null) {
			ToastUtil.show(mContext, "终点未设置");
		}
		showProgressDialog();
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				mStartPoint, mEndPoint);
		if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
			DriveRouteQuery query = new DriveRouteQuery(fromAndTo, mode, null,
					null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
			mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
		}
	}

	@Override
	public void onBusRouteSearched(BusRouteResult result, int errorCode) {
		
	}

	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
		dissmissProgressDialog();
		aMap.clear();// 清理地图上的所有覆盖物
		if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
					mDriveRouteResult = result;
					final DrivePath drivePath = mDriveRouteResult.getPaths()
							.get(0);
					DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
							mContext, aMap, drivePath,
							mDriveRouteResult.getStartPos(),
							mDriveRouteResult.getTargetPos(), null);
					drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
					drivingRouteOverlay.removeFromMap();
					drivingRouteOverlay.addToMap();
					drivingRouteOverlay.zoomToSpan();

				} else if (result != null && result.getPaths() == null) {
					ToastUtil.show(mContext, R.string.no_result);
				}

			} else {
				ToastUtil.show(mContext, R.string.no_result);
			}
		} else {
			ToastUtil.showerror(this.getApplicationContext(), errorCode);
		}
	}

	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
		
	}

	/**
	 * 显示进度框
	 */
	private void showProgressDialog() {
		if (progDialog == null)
			progDialog = new ProgressDialog(this);
		    progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		    progDialog.setIndeterminate(false);
		    progDialog.setCancelable(true);
		    progDialog.setMessage("正在搜索");
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

	@Override
	public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void ongasClick(View view) {
		searchRoutePOI(RoutePOISearchType.TypeGasStation);
		gasbtn.setTextColor(Color.BLUE);
		ATMbtn.setTextColor(Color.GRAY);
		Maibtn.setTextColor(Color.GRAY);
		Toibtn.setTextColor(Color.GRAY);
	}

	public void onATMClick(View view) {
		searchRoutePOI(RoutePOISearchType.TypeATM);
		gasbtn.setTextColor(Color.GRAY);
		ATMbtn.setTextColor(Color.BLUE);
		Maibtn.setTextColor(Color.GRAY);
		Toibtn.setTextColor(Color.GRAY);
	}

	public void onMaiClick(View view) {
		searchRoutePOI(RoutePOISearchType.TypeMaintenanceStation);
		gasbtn.setTextColor(Color.GRAY);
		ATMbtn.setTextColor(Color.GRAY);
		Maibtn.setTextColor(Color.BLUE);
		Toibtn.setTextColor(Color.GRAY);
	}
	
	public void onToiClick(View view) {
//		searchRouteResult(ROUTE_TYPE_DRIVE, mode);
		searchRoutePOI(RoutePOISearchType.TypeToilet);
		gasbtn.setTextColor(Color.GRAY);
		ATMbtn.setTextColor(Color.GRAY);
		Maibtn.setTextColor(Color.GRAY);
		Toibtn.setTextColor(Color.BLUE);
	}
	
	private void searchRoutePOI(RoutePOISearchType type) {
		if (overlay != null) {
			overlay.removeFromMap();
		}
		RoutePOISearchQuery query = new RoutePOISearchQuery(mStartPoint ,mEndPoint, mode, type, 250);
		final RoutePOISearch search = new RoutePOISearch(this, query);
		search.setPoiSearchListener(this);

		search.searchRoutePOIAsyn();
		
	}

	@Override
	public void onRoutePoiSearched(RoutePOISearchResult result, int errorCode) {
		if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
			if(result != null){
				List<RoutePOIItem> items = result.getRoutePois();
				if (items != null && items.size() > 0) {
					if (overlay != null) {
						overlay.removeFromMap();
					}
					overlay = new myRoutePoiOverlay(aMap, items);
					overlay.addToMap();
				} else {
					ToastUtil.show(RoutePOIActivity.this,R.string.no_result);
				}
			}
		}else{
			ToastUtil.showerror(RoutePOIActivity.this, errorCode);
		}
		
	}

	/**
	 * 自定义PoiOverlay
	 *
	 */
	
	private class myRoutePoiOverlay {
		private AMap mamap;
		private List<RoutePOIItem> mPois;
	    private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();
		public myRoutePoiOverlay(AMap amap , List<RoutePOIItem> pois) {
			mamap = amap;
	        mPois = pois;
		}

	    /**
	     * 添加Marker到地图中。
	     * @since V2.1.0
	     */
	    public void addToMap() {
	        for (int i = 0; i < mPois.size(); i++) {
	            Marker marker = mamap.addMarker(getMarkerOptions(i));
	            RoutePOIItem item = mPois.get(i);
				marker.setObject(item);
	            mPoiMarks.add(marker);
	        }
	    }

	    /**
	     * 去掉PoiOverlay上所有的Marker。
	     *
	     * @since V2.1.0
	     */
	    public void removeFromMap() {
	        for (Marker mark : mPoiMarks) {
	            mark.remove();
	        }
	    }

	    /**
	     * 移动镜头到当前的视角。
	     * @since V2.1.0
	     */
	    public void zoomToSpan() {
	        if (mPois != null && mPois.size() > 0) {
	            if (mamap == null)
	                return;
	            LatLngBounds bounds = getLatLngBounds();
	            mamap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
	        }
	    }

	    private LatLngBounds getLatLngBounds() {
	        LatLngBounds.Builder b = LatLngBounds.builder();
	        for (int i = 0; i < mPois.size(); i++) {
	            b.include(new LatLng(mPois.get(i).getPoint().getLatitude(),
	                    mPois.get(i).getPoint().getLongitude()));
	        }
	        return b.build();
	    }

	    private MarkerOptions getMarkerOptions(int index) {
	        return new MarkerOptions()
	                .position(
	                        new LatLng(mPois.get(index).getPoint()
	                                .getLatitude(), mPois.get(index)
	                                .getPoint().getLongitude()))
	                .title(getTitle(index)).snippet(getSnippet(index));
	    }

	    protected String getTitle(int index) {
	        return mPois.get(index).getTitle();
	    }

	    protected String getSnippet(int index) {
	        return mPois.get(index).getDistance() + "米  " + mPois.get(index).getDuration() + "秒";
	    }

	    /**
	     * 从marker中得到poi在list的位置。
	     *
	     * @param marker 一个标记的对象。
	     * @return 返回该marker对应的poi在list的位置。
	     * @since V2.1.0
	     */
	    public int getPoiIndex(Marker marker) {
	        for (int i = 0; i < mPoiMarks.size(); i++) {
	            if (mPoiMarks.get(i).equals(marker)) {
	                return i;
	            }
	        }
	        return -1;
	    }

	    /**
	     * 返回第index的poi的信息。
	     * @param index 第几个poi。
	     * @return poi的信息。poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
	     * @since V2.1.0
	     */
	    public RoutePOIItem getPoiItem(int index) {
	        if (index < 0 || index >= mPois.size()) {
	            return null;
	        }
	        return mPois.get(index);
	    }
	}

}

