package com.gdut.water.mymap3d.locationsearch.module.route;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.base.BaseActivity;
import com.gdut.water.mymap3d.base.BaseRefresh;
import com.gdut.water.mymap3d.base.PoiSearchUtil;
import com.gdut.water.mymap3d.data.dao.QueryDao;
import com.gdut.water.mymap3d.overlay.DrivingRouteOverlay;
import com.gdut.water.mymap3d.overlay.WalkRouteOverlay;
import com.gdut.water.mymap3d.util.AMapUtil;
import com.gdut.water.mymap3d.util.Constants;
import com.gdut.water.mymap3d.util.DbUtils;
import com.gdut.water.mymap3d.util.SharedPreferencesUtil;
import com.gdut.water.mymap3d.util.ToastUtil;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Route路径规划: 驾车规划、公交规划、步行规划
 */
public class RouteActivity extends BaseActivity implements OnMapClickListener,
        OnMarkerClickListener, OnInfoWindowClickListener, InfoWindowAdapter, OnRouteSearchListener {
	private AMap aMap;
	private MapView mapView;
	private Context mContext;
	private RouteSearch mRouteSearch;
	private DriveRouteResult mDriveRouteResult;
	private BusRouteResult mBusRouteResult;
	private WalkRouteResult mWalkRouteResult;
	private LatLonPoint mStartPoint;//起点，116.335891,39.942295
	private LatLonPoint mEndPoint;//终点，116.481288,39.995576

	private String mCurrentCityName ;
	//路径规划类别
	private final int ROUTE_TYPE_BUS = 1;
	private final int ROUTE_TYPE_DRIVE = 2;
	private final int ROUTE_TYPE_WALK = 3;
	private final int ROUTE_TYPE_CROSSTOWN = 4;
	
	private LinearLayout mBusResultLayout;
	private RelativeLayout mBottomLayout;
	private TextView mRotueTimeDes, mRouteDetailDes;
	private ImageView mBus;
	private ImageView mDrive;
	private ImageView mWalk;
	private ListView mBusResultList;
	private ProgressDialog progDialog = null;// 搜索时进度条

	private AutoCompleteTextView startPlace;
	private AutoCompleteTextView endPlace;
	private Button btnSearch;
	private View placeView;
	private View busResult;

	private View driveClick;
	private View busClick;
	private View walkClick;
	private View crossBusClick;
	private boolean isSearch = true;
	LatLonPoint centerPoint;
	public static final int REQUEST_CODE = 1000;
	public static final int RESULT_CODE_INPUTTIPS = 1001;
	public static final int RESULT_CODE_INPUTTIPS_SECOND = 1002;
	public static final int RESULT_CODE_KEYWORDS = 1003;

	private int busMode;
	private int drivingMode;

	public static final int UPDATE_VIEW = 1;
	public static final int UPDATE_MAP = 2;
	//初始化sp
	SharedPreferences defaultPreferences;
	SharedPreferencesUtil sp;


	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.route_activity);

		BaseActivity.mToolbar.setTitle("Route路径规划");
		mContext = this.getApplicationContext();
		mapView = (MapView) findViewById(R.id.route_map);
		mapView.onCreate(bundle);// 此方法必须重写
		init();

        boolean isFlush = defaultPreferences.getBoolean("MapTypeToAll",false);
        if(isFlush){
            BaseRefresh.init(this);
            BaseRefresh.showRefresh(aMap);
        }
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
	
	private void setfromandtoMarker() {
		if(mStartPoint!=null && mEndPoint!=null){
			aMap.addMarker(new MarkerOptions()
					.position(AMapUtil.convertToLatLng(mStartPoint))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
			aMap.addMarker(new MarkerOptions()
					.position(AMapUtil.convertToLatLng(mEndPoint))
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
		}
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
		mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
		mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
		mRotueTimeDes = (TextView) findViewById(R.id.firstline);
		mRouteDetailDes = (TextView) findViewById(R.id.secondline);
		mDrive = (ImageView)findViewById(R.id.route_drive);
		mBus = (ImageView)findViewById(R.id.route_bus);
		mWalk = (ImageView)findViewById(R.id.route_walk);
		mBusResultList = (ListView) findViewById(R.id.bus_result_list);

		startPlace = (AutoCompleteTextView) findViewById(R.id.start_point);
		endPlace = (AutoCompleteTextView) findViewById(R.id.end_point);
		btnSearch = (Button) findViewById(R.id.route_button);
		placeView = findViewById(R.id.search_place_frame);
		busResult = findViewById(R.id.bus_result);

		//功能选项初始化
		driveClick = findViewById(R.id.drive_click);
		busClick = findViewById(R.id.bus_click);
		walkClick = findViewById(R.id.walk_click);
		crossBusClick = findViewById(R.id.crosstownbus_click);

		driveClick.setEnabled(true);
		busClick.setEnabled(true);
		walkClick.setEnabled(true);
		crossBusClick.setEnabled(true);
		//地图sp初始化
		sp = new SharedPreferencesUtil(this);
		double startLatitude = Double.valueOf(sp.getParam(Constants.CURRENT_LATITUDE,null));
		double startLongitude =  Double.valueOf(sp.getParam(Constants.CURRENT_LONGITUDE,null));
		centerPoint = new LatLonPoint(startLatitude,startLongitude);
		//mStartPoint = centerPoint;
		mCurrentCityName = sp.getParam(Constants.CURRENT_CITY,"广州市");


		//移到目前位置视图
		aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
				AMapUtil.convertToLatLng(centerPoint), 12, 30, 30)));

		//参数设置初始化sp
		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		busMode = Integer.valueOf(defaultPreferences.getString(Constants.BUS_MODE,"0"));
		drivingMode = Integer.valueOf(defaultPreferences.getString(Constants.DRIVING_MODE,"8"));
		registerListener();
	}

	//建立一个主线程handler
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
				case UPDATE_VIEW:
					btnSearch.setText("正在进行路径规划....");
					driveClick.setEnabled(true);
					busClick.setEnabled(true);
					walkClick.setEnabled(true);
					crossBusClick.setEnabled(true);
					startPlace.setEnabled(false);
					endPlace.setEnabled(false);
					break;
				case UPDATE_MAP:
					btnSearch.setText("开始进行路径规划");
					//移到起点视图
					aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(
							AMapUtil.convertToLatLng(centerPoint), 12, 30, 30)));
					aMap.clear();// 清理地图上的所有覆盖物
					driveClick.setEnabled(false);
					busClick.setEnabled(false);
					walkClick.setEnabled(false);
					crossBusClick.setEnabled(false);
					mBottomLayout.setVisibility(View.GONE);
					busResult.setVisibility(View.GONE);
					startPlace.setEnabled(true);
					endPlace.setEnabled(true);
					break;
			}
		}
	};
	/**
	 * 注册监听
	 */
	private void registerListener() {
		aMap.setOnMapClickListener(RouteActivity.this);
		aMap.setOnMarkerClickListener(RouteActivity.this);
		aMap.setOnInfoWindowClickListener(RouteActivity.this);
		aMap.setInfoWindowAdapter(RouteActivity.this);

		startPlace.addTextChangedListener(startTextWatcher);
		startPlace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				return false;
			}
		});
		endPlace.addTextChangedListener(endTextWatcher);
		endPlace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
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
				InputtipsQuery inputquery = new InputtipsQuery(newText, mCurrentCityName);
				Inputtips inputTips = new Inputtips(RouteActivity.this, inputquery);
				inputTips.setInputtipsListener(startInputtips);
				inputTips.requestInputtipsAsyn();
			}
		}
		@Override
		public void afterTextChanged(Editable editable) {

		}
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
				InputtipsQuery inputquery = new InputtipsQuery(newText, mCurrentCityName);
				Inputtips inputTips = new Inputtips(RouteActivity.this, inputquery);
				inputTips.setInputtipsListener(endInputtips);
				inputTips.requestInputtipsAsyn();
			}
		}
		@Override
		public void afterTextChanged(Editable editable) {

		}
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
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onSearchClick(View view){
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
					String start = startPlace.getText().toString().trim();
					String end = endPlace.getText().toString().trim();
					initData(start,1);
					initData(end,2);
					isSearch = false;
					//DbUtils.getLiteOrm().deleteAll(QueryDao.class);
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
//        if(getLiteOrm() == null){
//			DbUtils.openDB(this,Constants.DB);
//		}

        if(category == 1){
			List<QueryDao> queryDaos = DbUtils.getLiteOrm().query(new QueryBuilder<QueryDao>(QueryDao.class)
					.whereEquals("category",1)
//					.whereAppendAnd()
                    .appendOrderDescBy("_id"));
			if(!queryDaos.isEmpty()) {
				double latitude = Double.valueOf(queryDaos.get(0).getLatitude());
				double longitude = Double.valueOf(queryDaos.get(0).getLongitude());
				LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
				point = latLonPoint;
				Log.e("RouteActivity","mStartPoint:"+point.toString());
                return point;
			}
        }else if (category == 2){
			List<QueryDao> queryDaos = DbUtils.getLiteOrm().query(new QueryBuilder<QueryDao>(QueryDao.class)
					.whereEquals("category",2)
//					.whereAppendAnd()
                    .appendOrderDescBy("_id"));
			if(!queryDaos.isEmpty()) {
				double latitude = Double.valueOf(queryDaos.get(0).getLatitude());
				double longitude = Double.valueOf(queryDaos.get(0).getLongitude());
				LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
				point = latLonPoint;
				Log.e("RouteActivity","mEndPoint:"+point.toString());
                return point;
			}
        }else {
            Log.e("RouteActivity","获取起点终点失败:");

        }
        return point;
    }
	private void initData(String name,int category){
		PoiSearchUtil poiSearchUtil = new PoiSearchUtil(mContext,name,"",mCurrentCityName,category);
		poiSearchUtil.doSearchQuery();
	}
	/**
	 * 公交路线搜索
     */
	public void onBusClick(View view) {
		searchRouteResult(ROUTE_TYPE_BUS,busMode);
		mDrive.setImageResource(R.drawable.route_drive_normal);
		mBus.setImageResource(R.drawable.route_bus_select);
		mWalk.setImageResource(R.drawable.route_walk_normal);
		mapView.setVisibility(View.GONE);
		mBusResultLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * 驾车路线搜索
	 */
	public void onDriveClick(View view) {
		searchRouteResult(ROUTE_TYPE_DRIVE, drivingMode);
		mDrive.setImageResource(R.drawable.route_drive_select);
		mBus.setImageResource(R.drawable.route_bus_normal);
		mWalk.setImageResource(R.drawable.route_walk_normal);
		mapView.setVisibility(View.VISIBLE);
		mBusResultLayout.setVisibility(View.GONE);
	}

	/**
	 * 步行路线搜索
     */
	public void onWalkClick(View view) {
		searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WALK_DEFAULT);
		mDrive.setImageResource(R.drawable.route_drive_normal);
		mBus.setImageResource(R.drawable.route_bus_normal);
		mWalk.setImageResource(R.drawable.route_walk_select);
		mapView.setVisibility(View.VISIBLE);
		mBusResultLayout.setVisibility(View.GONE);
	}

	/**
	 * 跨城公交路线搜索
     */
	public void onCrosstownBusClick(View view) {
		searchRouteResult(ROUTE_TYPE_CROSSTOWN, RouteSearch.BUS_DEFAULT);
		mDrive.setImageResource(R.drawable.route_drive_normal);
		mBus.setImageResource(R.drawable.route_bus_normal);
		mWalk.setImageResource(R.drawable.route_walk_normal);
		mapView.setVisibility(View.GONE);
		mBusResultLayout.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 开始搜索路径规划方案
	 */
	public void searchRouteResult(int routeType, int mode) {

		if(mStartPoint==null||mEndPoint==null){
			mStartPoint = getData(1);
			mEndPoint = getData(2);

		}
		if (mStartPoint == null ) {
			ToastUtil.show(mContext, "起点未设置");
			return;
		}
		if (mEndPoint == null) {
			ToastUtil.show(mContext, "终点未设置");
			return;
		}
		showProgressDialog();
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				mStartPoint, mEndPoint);
		if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
			BusRouteQuery query = new BusRouteQuery(fromAndTo, mode,
					mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
			mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
		} else if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
			DriveRouteQuery query = new DriveRouteQuery(fromAndTo, mode, null,
					null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
			mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
		} else if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
			WalkRouteQuery query = new WalkRouteQuery(fromAndTo, mode);
			mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
		} else if (routeType == ROUTE_TYPE_CROSSTOWN) {
			RouteSearch.FromAndTo fromAndTo_bus = new RouteSearch.FromAndTo(
					mStartPoint, mEndPoint);
			BusRouteQuery query = new BusRouteQuery(fromAndTo, mode,
					mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
			//query.setCityd("番禺区");
			mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
		}
	}

	/**
	 * 公交路线搜索结果方法回调
     */
	@Override
	public void onBusRouteSearched(BusRouteResult result, int errorCode) {
		dissmissProgressDialog();
		mBottomLayout.setVisibility(View.GONE);
		aMap.clear();// 清理地图上的所有覆盖物
		if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
					mBusRouteResult = result;
					BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
					mBusResultList.setAdapter(mBusResultListAdapter);		
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

	/**
	 * 驾车路线搜索结果方法回调
	 */
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
					drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
					drivingRouteOverlay.removeFromMap();
					drivingRouteOverlay.addToMap();
					drivingRouteOverlay.zoomToSpan();
					mBottomLayout.setVisibility(View.VISIBLE);
					int dis = (int) drivePath.getDistance();
					int dur = (int) drivePath.getDuration();
					String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
					mRotueTimeDes.setText(des);
					mRouteDetailDes.setVisibility(View.VISIBLE);
					int taxiCost = (int) mDriveRouteResult.getTaxiCost();
					mRouteDetailDes.setText("打车约"+taxiCost+"元");
					mBottomLayout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mContext,
									DriveRouteDetailActivity.class);
							intent.putExtra("drive_path", drivePath);
							intent.putExtra("drive_result",
									mDriveRouteResult);
							startActivity(intent);
						}
					});
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

	/**
	 * 步行路线搜索结果方法回调
	 */
	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
		dissmissProgressDialog();
		aMap.clear();// 清理地图上的所有覆盖物
		if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
					mWalkRouteResult = result;
					final WalkPath walkPath = mWalkRouteResult.getPaths()
							.get(0);
					WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
							this, aMap, walkPath,
							mWalkRouteResult.getStartPos(),
							mWalkRouteResult.getTargetPos());
					walkRouteOverlay.removeFromMap();
					walkRouteOverlay.addToMap();
					walkRouteOverlay.zoomToSpan();
					mBottomLayout.setVisibility(View.VISIBLE);
					int dis = (int) walkPath.getDistance();
					int dur = (int) walkPath.getDuration();
					String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
					mRotueTimeDes.setText(des);
					mRouteDetailDes.setVisibility(View.GONE);
					mBottomLayout.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(mContext,
									WalkRouteDetailActivity.class);
							intent.putExtra("walk_path", walkPath);
							intent.putExtra("walk_result",
									mWalkRouteResult);
							startActivity(intent);
						}
					});
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
        Log.e("RouteActivity","-----onResume");


	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
        Log.e("RouteActivity","-----onPause");
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

	/**
	 * 骑行路线搜索结果方法回调
	 */
	@Override
	public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("RouteActivity","-----onStop");
    }



	//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		if (resultCode == RESULT_CODE_INPUTTIPS && data != null) {
//
//			Tip tip = data.getParcelableExtra(Constants.EXTRA_TIP);
//
//			Log.e("RouteActivity","name:"+tip.getName()+" point:"+tip.getPoint().toString());
//			mEndPoint = tip.getPoint();
//			endPlace.setText(tip.getName());
//
//		}
//	}
}

