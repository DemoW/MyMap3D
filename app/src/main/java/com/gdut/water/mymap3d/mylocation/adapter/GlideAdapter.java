package com.gdut.water.mymap3d.mylocation.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gdut.water.mymap3d.R;

import java.util.List;

/**
 * Created by Water on 2017/4/17.
 */

public class GlideAdapter extends RecyclerView.Adapter<GlideAdapter.MyViewHolder> {

    private List<String> glideDatas;
    private LayoutInflater mInflater;
    private Context mContext;

    public GlideAdapter(Context context, List<String> datas){
        glideDatas = datas;
        mContext = context;
        mInflater = LayoutInflater.from(context);

    }

    public interface OnItemClickLitener
    {
        void onItemClick(View view, int position);
        void onItemLongClick(View view , int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void removeData(int position) {
        glideDatas.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.glide_items_list, parent, false);
        //自定义添加
        //view.setOnClickListener(this);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        holder.mPathView.setText((position+1)+"、截图名称："+glideDatas.get(position).split("/")[3]);
        Glide.with(mContext).load(glideDatas.get(position))
//                .placeholder(R.drawable.ic_launcher)
                .into(holder.mContentView);

        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null)
        {
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return glideDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final TextView mPathView;
        public final ImageView mContentView;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            mPathView = (TextView) view.findViewById(R.id.file_path);
            mContentView = (ImageView) view.findViewById(R.id.image_content);
        }

    }

}
