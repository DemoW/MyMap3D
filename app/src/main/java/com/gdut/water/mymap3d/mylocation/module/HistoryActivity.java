package com.gdut.water.mymap3d.mylocation.module;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.base.BaseActivity;
import com.gdut.water.mymap3d.data.dao.HistoryDao;
import com.gdut.water.mymap3d.data.dao.QueryDao;
import com.gdut.water.mymap3d.data.pojo.Location;
import com.gdut.water.mymap3d.mylocation.adapter.HistoryAdapter;
import com.gdut.water.mymap3d.util.Constants;
import com.gdut.water.mymap3d.util.DbUtils;

import java.util.ArrayList;
import java.util.List;

import static com.gdut.water.mymap3d.R.menu.history_data_menu;
import static com.gdut.water.mymap3d.util.DbUtils.getLiteOrm;

public class HistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<Location> mDatas;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.mToolbar.setTitle("搜索历史记录");
        setContentView(R.layout.activity_history);

        recyclerView = (RecyclerView) findViewById(R.id.history_list);
        mDatas = initData();
        historyAdapter = new HistoryAdapter(this,mDatas);
        initListener();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.addItemDecoration(new DividerItemDecoration(this,
//                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(historyAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(history_data_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.get_all_data){
            if(getLiteOrm()==null){
                DbUtils.openDB(this,Constants.DB);
            }
            new Refresh().execute(initData());
        }
        else if(id == R.id.get_week_data){
            if(getLiteOrm()==null){
                DbUtils.openDB(this,Constants.DB);
            }
            new Refresh().execute(getWeekDatas());
        }
        else if(id == R.id.delete){
            AlertDialog.Builder dialog = new AlertDialog.Builder(HistoryActivity.this);
            dialog.setTitle("历史记录删除操作");
            dialog.setMessage("是否清除全部历史记录？数据不可恢复！");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int whitch) {
                    if(getLiteOrm()==null){
                        DbUtils.openDB(HistoryActivity.this,Constants.DB);
                    }
                    int i = DbUtils.getLiteOrm().deleteAll(HistoryDao.class);
                    DbUtils.getLiteOrm().deleteAll(QueryDao.class);//清除路径规划和沿途搜索的记录
                    if(i>0){
                        new Refresh().execute(initData());
                        //Toast.makeText(this,"数据清空完毕！",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(HistoryActivity.this,"数据已经清空了",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.setNegativeButton("Cancel",null);
            dialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private class Refresh extends AsyncTask<List<Location>,Void,Void>{

        @Override
        protected Void doInBackground(List<Location>... lists) {

            historyAdapter.setHistoryDatas(lists[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            historyAdapter.notifyDataSetChanged();
        }
    }

    private List<Location> initData() {
        List<Location> locations = new ArrayList<>();
        if(getLiteOrm()==null){
            DbUtils.openDB(this,Constants.DB);
        }
        List<HistoryDao> historyDaos = DbUtils.getQueryAll(HistoryDao.class);
        if(!historyDaos.isEmpty()){
            int length = historyDaos.size()-1;
            for (int i = length; i >= 0; i--){
                Location location = new Location();
                location.setName(historyDaos.get(i).getName());
                location.setDistract(historyDaos.get(i).getDistract());
                location.setLatitude(historyDaos.get(i).getLatitude());
                location.setLongitude(historyDaos.get(i).getLongitude());
                location.setCreateTime(historyDaos.get(i).getCreateTime());
                locations.add(location);
            }

        }else{
            Toast.makeText(this,"没有历史搜索记录",Toast.LENGTH_SHORT).show();
        }
        return locations;
    }

    private List<Location> getWeekDatas(){
        List<Location> locations = new ArrayList<>();
        if(getLiteOrm()==null){
            DbUtils.openDB(this,Constants.DB);
        }
        List<HistoryDao> historyDaos = DbUtils.getQueryInWeek();
        if(!historyDaos.isEmpty()) {
            int length = historyDaos.size() - 1;
            for (int i = length; i >= 0; i--) {
                Location location = new Location();
                location.setName(historyDaos.get(i).getName());
                location.setDistract(historyDaos.get(i).getDistract());
                location.setLatitude(historyDaos.get(i).getLatitude());
                location.setLongitude(historyDaos.get(i).getLongitude());
                location.setCreateTime(historyDaos.get(i).getCreateTime());
                locations.add(location);
            }
        }else{
            Toast.makeText(this,"一周内无历史搜索记录",Toast.LENGTH_SHORT).show();
        }
        return locations;
    }

    private void initListener(){
        historyAdapter.setOnItemClickLitener(new HistoryAdapter.OnItemClickLitener()
        {

            @Override
            public void onItemClick(View view, int position)
            {
                Toast.makeText(HistoryActivity.this, position+1 + " click",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position)
            {
                Toast.makeText(HistoryActivity.this, position+1 + " long click",
                        Toast.LENGTH_SHORT).show();
                historyAdapter.removeData(position);
            }
        });
    }
}
