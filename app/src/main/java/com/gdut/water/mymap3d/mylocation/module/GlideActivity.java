package com.gdut.water.mymap3d.mylocation.module;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.base.BaseActivity;
import com.gdut.water.mymap3d.mylocation.adapter.GlideAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GlideActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private List<String> mDatas;
    private GlideAdapter glideAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glide);
        BaseActivity.mToolbar.setTitle("截屏相册");

        recyclerView = (RecyclerView) findViewById(R.id.glide_list);
        mDatas = getImagePathFromSD();
        glideAdapter = new GlideAdapter(this,mDatas);
        initListener();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.addItemDecoration(new DividerItemDecoration(this,
//                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(glideAdapter);
    }

    private void initListener(){
        glideAdapter.setOnItemClickLitener(new GlideAdapter.OnItemClickLitener()
        {

            @Override
            public void onItemClick(View view, int position)
            {
//                Toast.makeText(GlideActivity.this, position+1 + " click",
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position)
            {
//                Toast.makeText(GlideActivity.this, position+1 + " long click",
//                        Toast.LENGTH_SHORT).show();
//                        glideAdapter.removeData(position);
            }
        });
    }
    /**
     * 从sd卡获取图片资源
     * @return
     */
    private List<String> getImagePathFromSD() {
        // 图片列表
        List<String> imagePathList = new ArrayList<String>();
        // 得到sd卡内image文件夹的路径   File.separator(/)
        String filePath = Environment.getExternalStorageDirectory().toString() ;//+ File.separator + "MyMap3dScreenShot";
        // 得到该路径文件夹下所有的文件
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (checkIsImageFile(file.getPath())) {
                imagePathList.add(file.getPath());
            }
        }
        Log.e("MyGlide","path:"+imagePathList.toString());
        // 返回得到的图片列表
        return imagePathList;
    }

    /**
     * 检查扩展名，得到图片格式的文件
     * @param fName  文件名
     * @return
     */
    @SuppressLint("DefaultLocale")
    private boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("png")  ) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }

        return isImageFile;
    }
}
