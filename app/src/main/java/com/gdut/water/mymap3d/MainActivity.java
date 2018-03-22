package com.gdut.water.mymap3d;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gdut.water.mymap3d.base.LocationActivity;
import com.gdut.water.mymap3d.locationsearch.ItemFragment;
import com.gdut.water.mymap3d.locationsearch.dummy.DummyContent;
import com.gdut.water.mymap3d.locationsearch.module.CalculateDistanceActivity;
import com.gdut.water.mymap3d.locationsearch.module.CloudActivity;
import com.gdut.water.mymap3d.locationsearch.module.DistrictWithBoundaryActivity;
import com.gdut.water.mymap3d.locationsearch.module.HeatMapActivity;
import com.gdut.water.mymap3d.locationsearch.module.PoiAroundSearchActivity;
import com.gdut.water.mymap3d.locationsearch.module.PoiKeywordSearchActivity;
import com.gdut.water.mymap3d.locationsearch.module.RoutePOIActivity;
import com.gdut.water.mymap3d.locationsearch.module.route.RouteActivity;
import com.gdut.water.mymap3d.morefunc.MoreFuncFragment;
import com.gdut.water.mymap3d.mylocation.MySiteFragment;
import com.gdut.water.mymap3d.mylocation.module.GlideActivity;
import com.gdut.water.mymap3d.mylocation.module.HistoryActivity;
import com.gdut.water.mymap3d.mylocation.module.SettingsActivity;
import com.gdut.water.mymap3d.mylocation.module.ShareActivity;
import com.gdut.water.mymap3d.mylocation.module.offlinemap.OfflineMapActivity;
import com.gdut.water.mymap3d.util.DbUtils;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

import static com.gdut.water.mymap3d.R.menu.drawer;

public class MainActivity extends CheckPermissionsActivity  implements
        NavigationView.OnNavigationItemSelectedListener,ItemFragment.OnListFragmentInteractionListener {

    Toolbar toolbar;

    //MyLocationFragment myLocationFragment;
    MySiteFragment mySiteFragment;
    //BlankFragment blankFragment;
    ItemFragment itemFragment;
    MoreFuncFragment moreFuncFragment;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    replaceFragment(mySiteFragment);
                    return true;
                case R.id.navigation_dashboard:
                    replaceFragment(itemFragment);
                    return true;
                case R.id.navigation_notifications:
                    replaceFragment(moreFuncFragment);
                    return true;
            }
            return false;
        }

    };

    private void initFragment() {
        //myLocationFragment = new MyLocationFragment();
        mySiteFragment = new MySiteFragment();
        itemFragment = new ItemFragment();
        moreFuncFragment = new MoreFuncFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.content, mySiteFragment, mySiteFragment.getClass().getName());
       // fragmentTransaction.add(R.id.content, myLocationFragment, myLocationFragment.getClass().getName());
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment) {

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, fragment.getClass().getName());
//        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
//        if(myLocationFragment != null){
//            fragmentTransaction.hide(myLocationFragment);
//        }
        if(mySiteFragment != null){
            fragmentTransaction.hide(mySiteFragment);
        }
        if(itemFragment != null){
            fragmentTransaction.hide(itemFragment);
        }
        if(moreFuncFragment != null){
            fragmentTransaction.hide(moreFuncFragment);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        initToolBar();

        initNavigationView();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initFragment();

        SQLiteStudioService.instance().start(this);
    }

    private void initNavigationView() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(drawer!=null) {
            if (drawer.isDrawerOpen(Gravity.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //检索内容
        if (id == R.id.action_search) {
            Intent intent = new Intent(MainActivity.this, LocationActivity.class);
            startActivity(intent);
            return true;
        }else if(id == R.id.refresh){

            BottomNavigationView bNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
            int navId = bNavigationView.getSelectedItemId();
            if(mySiteFragment!=null && R.id.navigation_home == navId){
                mySiteFragment.showRefresh();
                return true;
            }else{
                Toast.makeText(this,"当前页面无可用刷新",Toast.LENGTH_SHORT).show();
            }

        }else if(id == R.id.history){
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        BottomNavigationView bNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        int navId = bNavigationView.getSelectedItemId();

        if (id == R.id.nav_camera) {
            //截屏功能
            if(mySiteFragment!=null && R.id.navigation_home == navId){
                //mySiteFragment.getMapScreenShot(mySiteFragment.getView());
                screenShot();
            }else {
                Toast.makeText(MainActivity.this,"当前页面截屏失败！",Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.nav_gallery) {
            //截图相册
            startActivity(new Intent(MainActivity.this,GlideActivity.class));

        } else if (id == R.id.nav_offline_map) {
            //离线地图功能
            startActivity(new Intent(MainActivity.this,OfflineMapActivity.class));

        } else if (id == R.id.nav_manage) {
            //参数设置
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));

        } else if (id == R.id.nav_share) {
            //分享短串
            startActivity(new Intent(MainActivity.this, ShareActivity.class));

        } else if (id == R.id.nav_exit) {

            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //截屏功能用AsyncTask
    public void screenShot(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                mySiteFragment.getMapScreenShot(mySiteFragment.getView());
                return null;
            }
        }.execute((Void[]) null);
    }

    //位置检索功能  回归方法
    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        //调用相应的Activity

        switch(item.id){
            //Poi关键字检索
            case "1":
                startActivity(new Intent(MainActivity.this, PoiKeywordSearchActivity.class));
                break;
            //Poi周边检索
            case "2":
                startActivity(new Intent(MainActivity.this, PoiAroundSearchActivity.class));
                break;
            //沿途搜索
            case "3":
                startActivity(new Intent(MainActivity.this, RoutePOIActivity.class));
                break;
            //Route路径规划
            case "4":
                startActivity(new Intent(MainActivity.this, RouteActivity.class));
                break;
            //云图检索:58d279307bbf195ae80cfd1a
            case "5":
                startActivity(new Intent(MainActivity.this, CloudActivity.class));
                break;
            //行政区域边界查询
            case "6":
                startActivity(new Intent(MainActivity.this, DistrictWithBoundaryActivity.class));
                break;
            //两点间的距离
            case "7":
                startActivity(new Intent(MainActivity.this, CalculateDistanceActivity.class));
                break;
            //搜索历史热力图
            case "8":
                startActivity(new Intent(MainActivity.this, HeatMapActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        SQLiteStudioService.instance().stop();
        super.onDestroy();
        DbUtils.closeDb();
    }
}
