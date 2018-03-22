package com.gdut.water.mymap3d.base;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.amap.api.maps.AMap.OnMapScreenShotListener;
import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.util.ToastUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BaseActivity extends AppCompatActivity implements OnMapScreenShotListener{

    private static final String TAG = "BaseActivity";

    public static Toolbar mToolbar;
    FrameLayout mContentFl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    /**
     * 对地图进行截屏
     */
//    public static void getMapScreenShot(View v) {
//        mAMap.getMapScreenShot(this);
//    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (layoutResID == R.layout.activity_base) {
            super.setContentView(layoutResID);
            mContentFl = (FrameLayout) this.findViewById(R.id.content_fl);
            mToolbar = (Toolbar) this.findViewById(R.id.toolbar);
            mToolbar.setTitle("");
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onNavBackClick();
                }
            });
            mContentFl.removeAllViews();
        } else {
            View view = View.inflate(this, layoutResID, null);
            mContentFl.addView(view);
        }
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
                ToastUtil.show(this, buffer.toString());
            }else {
                ToastUtil.show(this,"抱歉。手机未安装SD卡或无权限");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    protected void onNavBackClick() {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
