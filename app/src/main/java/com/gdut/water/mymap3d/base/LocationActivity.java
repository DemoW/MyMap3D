package com.gdut.water.mymap3d.base;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.data.dao.HistoryDao;
import com.gdut.water.mymap3d.data.dao.LocationsDao;
import com.gdut.water.mymap3d.mylocation.adapter.InputTipsAdapter;
import com.gdut.water.mymap3d.util.Constants;
import com.gdut.water.mymap3d.util.DbUtils;
import com.gdut.water.mymap3d.util.ToastUtil;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.gdut.water.mymap3d.util.DbUtils.getLiteOrm;

/**
 * 输入提示功能实现
 */
public class LocationActivity extends BaseActivity implements SearchView.OnQueryTextListener,
		Inputtips.InputtipsListener, OnItemClickListener, View.OnClickListener{

	private SearchView mSearchView;// 输入搜索关键字
	private ImageView mBack;
	private ListView mInputListView;
	private List<Tip> mCurrentTipList;
	private InputTipsAdapter mIntipAdapter;

	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input_tips);
		BaseActivity.mToolbar.setTitle("位置搜索页面");
		//打开数据库
		DbUtils.openDB(this,Constants.DB);
		initSearchView();
		mInputListView = (ListView) findViewById(R.id.inputtip_list);
		mInputListView.setOnItemClickListener(this);
		mBack = (ImageView) findViewById(R.id.back);
		mBack.setOnClickListener(this);
	}

	private void initSearchView() {
		mSearchView = (SearchView) findViewById(R.id.keyWord);
		mSearchView.setOnQueryTextListener(this);
		//设置SearchView默认为展开显示
		mSearchView.setIconified(false);
		mSearchView.onActionViewExpanded();
		mSearchView.setIconifiedByDefault(true);
		mSearchView.setSubmitButtonEnabled(false);

	}

	/**
	 * 输入提示回调
	 *
	 * @param tipList
	 * @param rCode
	 */
	@Override
	public void onGetInputtips(List<Tip> tipList, int rCode) {
		if (rCode == 1000) {// 正确返回
			mCurrentTipList = tipList;
			List<String> listString = new ArrayList<String>();
			for (int i = 0; i < tipList.size(); i++) {
				listString.add(tipList.get(i).getName());
			}
			mIntipAdapter = new InputTipsAdapter(
					getApplicationContext(),
					mCurrentTipList);
			mInputListView.setAdapter(mIntipAdapter);
			mIntipAdapter.notifyDataSetChanged();
		} else {
			ToastUtil.showerror(this, rCode);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		if (mCurrentTipList != null) {
			Tip tip = (Tip) adapterView.getItemAtPosition(i);

//			Intent intent = new Intent();
//			intent.putExtra(Constants.EXTRA_TIP, tip);
//			setResult(RouteActivity.RESULT_CODE_INPUTTIPS, intent);

			if(tip.getPoint()!=null){
				//插入数据到location表
				LocationsDao locationsDao = new LocationsDao();
				locationsDao.setName(tip.getName());
				locationsDao.setAddress(tip.getAddress());
				locationsDao.setDistract(tip.getDistrict());
				locationsDao.setLatitude(tip.getPoint().getLatitude());
				locationsDao.setLongitude(tip.getPoint().getLongitude());
				locationsDao.setPoiID(tip.getPoiID());
				locationsDao.setTypeCode(tip.getTypeCode());
				locationsDao.setCreateTime(getTodayDate());

				//插入数据到query表
//				QueryDao queryDao = new QueryDao();
//				queryDao.setLatitude(tip.getPoint().getLatitude());
//				queryDao.setLongitude(tip.getPoint().getLongitude());
//				queryDao.setName(tip.getName());

				//插入数据到history表
				HistoryDao historyDao = new HistoryDao();
				historyDao.setName(tip.getName());
				historyDao.setAddress(tip.getAddress());
				historyDao.setDistract(tip.getDistrict());
				historyDao.setLatitude(tip.getPoint().getLatitude());
				historyDao.setLongitude(tip.getPoint().getLongitude());
				historyDao.setCreateTime(getTodayDate());

				//开启事务，对于name重复的数据进行提示
				if (getLiteOrm() == null) {
					DbUtils.openDB(this, Constants.DB);
				}
				long flag = DbUtils.getLiteOrm().insert(locationsDao);
				Log.e("LoacationActivity","locatin_value:"+flag);
				if(flag>0){
//					DbUtils.getLiteOrm().insert(queryDao,ConflictAlgorithm.Fail);
					DbUtils.getLiteOrm().insert(historyDao, ConflictAlgorithm.Fail);
				}
				this.finish();
			}else{
				Toast.makeText(this,"您选择的数据无效，请重新选择有效数据",Toast.LENGTH_SHORT).show();
			}

		}
	}

	private String getTodayDate() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}
	/**
	 * 按下确认键触发，本例为键盘回车或搜索键
	 *
	 * @param query
	 * @return
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {

		return false;
	}

	/**
	 * 输入字符变化时触发
	 *
	 * @param newText
	 * @return
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
		if (!IsEmptyOrNullString(newText)) {
			InputtipsQuery inputquery = new InputtipsQuery(newText, Constants.DEFAULT_CITY);
			Inputtips inputTips = new Inputtips(LocationActivity.this.getApplicationContext(), inputquery);
			inputTips.setInputtipsListener(this);
			inputTips.requestInputtipsAsyn();
		} else {
			if (mIntipAdapter != null && mCurrentTipList != null) {
				mCurrentTipList.clear();
				mIntipAdapter.notifyDataSetChanged();
			}
		}
		return false;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.back) {
			this.finish();
		}
	}

	public static boolean IsEmptyOrNullString(String s) {
		return (s == null) || (s.trim().length() == 0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}
}
