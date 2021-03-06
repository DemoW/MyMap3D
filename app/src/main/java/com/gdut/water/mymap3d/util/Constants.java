package com.gdut.water.mymap3d.util;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

public class Constants {

	public static final int ERROR = 1001;// 网络异常
	public static final int ROUTE_START_SEARCH = 2000;
	public static final int ROUTE_END_SEARCH = 2001;
	public static final int ROUTE_BUS_RESULT = 2002;// 路径规划中公交模式
	public static final int ROUTE_DRIVING_RESULT = 2003;// 路径规划中驾车模式
	public static final int ROUTE_WALK_RESULT = 2004;// 路径规划中步行模式
	public static final int ROUTE_NO_RESULT = 2005;// 路径规划没有搜索到结果

	public static final int GEOCODER_RESULT = 3000;// 地理编码或者逆地理编码成功
	public static final int GEOCODER_NO_RESULT = 3001;// 地理编码或者逆地理编码没有数据

	public static final int POISEARCH = 4000;// poi搜索到结果
	public static final int POISEARCH_NO_RESULT = 4001;// poi没有搜索到结果
	public static final int POISEARCH_NEXT = 5000;// poi搜索下一页

	public static final int BUSLINE_LINE_RESULT = 6001;// 公交线路查询
	public static final int BUSLINE_id_RESULT = 6002;// 公交id查询
	public static final int BUSLINE_NO_RESULT = 6003;// 异常情况

	public static final String BASIC_MAP_DATA = "BaseMapData";//SharedPreferences存储文件名

	public static final String DEFAULT_CITY = "广州";
	public static final String EXTRA_TIP = "ExtraTip";
	public static final String KEY_WORDS_NAME = "KeyWord";
	public static final String DEFAULT_INTENNT_KEY = "DefaultIntentKey";

	public static final LatLng BEIJING = new LatLng(39.90403, 116.407525);// 北京市经纬度
	public static final LatLng ZHONGGUANCUN = new LatLng(39.983456, 116.3154950);// 北京市中关村经纬度
	public static final LatLng FANGHENG = new LatLng(39.989614, 116.481763);// 方恒国际中心经纬度
	public static final LatLng SHANGHAI = new LatLng(31.238068, 121.501654);// 上海市经纬度
	public static final LatLng CHENGDU = new LatLng(30.679879, 104.064855);// 成都市经纬度
	public static final LatLng XIAN = new LatLng(34.341568, 108.940174);// 西安市经纬度
	public static final LatLng ZHENGZHOU = new LatLng(34.7466, 113.625367);// 郑州市经纬度
	public static final LatLng GUANGZHOU = new LatLng(23.043806, 113.386496);// 广州市经纬度
	public static LatLonPoint LATLONPOINT = null;

	//SharedPreference 存储的数据key
	public static final String ADDRESS_SEARCH = "AddressSearch";
	public static final String DESTINY_IF_SET = "DestinyIfSet";
	public static final String DESTINY_FORMAT_ADDRESS = "DestinyFormatAddress";
	public static final String DESTINY_CITY = "DestinyCity";
	public static final String DESTINY_LATITUDE = "DestinyLatitude";
	public static final String DESTINY_LONGITUDE = "DestinyLongitude";
	public static final String CURRENT_SITE = "CurrentSite";
	public static final String CURRENT_LATITUDE = "CurrentLatitude";
	public static final String CURRENT_LONGITUDE = "CurrentLongitude";
    public static final String CURRENT_FORMAT_ADDRESS = "CurrentFormatAddress";
	public static final String CURRENT_CITY = "CurrentCity";

	public static final String START_LATITUDE = "StartLatitude";
	public static final String END_LATITUDE = "EndLatitude";
	public static final String START_LONGITUDE = "StartLongitude";
	public static final String END_LONGITUDE = "EndLongitude";

	public static final String BUS_MODE = "BusMode";
	public static final String DRIVING_MODE = "DrivingMode";

	//databas
	public static final String DB = "mymap";
}
