package com.gdut.water.mymap3d.mylocation.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gdut.water.mymap3d.R;
import com.gdut.water.mymap3d.data.pojo.Location;

import java.util.List;

/**
 * Created by Water on 2017/4/22.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    private List<Location> historyDatas;
    private LayoutInflater mInflater;
    private Context mContext;

    public void setHistoryDatas(List<Location> historyDatas) {
        this.historyDatas = historyDatas;
    }

    public HistoryAdapter(Context context, List<Location> datas){
        historyDatas = datas;
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
        historyDatas.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public HistoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HistoryAdapter.MyViewHolder holder, int position) {
        if(historyDatas!=null){
            holder.mName.setText(historyDatas.get(position).getName());
            holder.mDistract.setText(historyDatas.get(position).getDistract());
        }


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
        return historyDatas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        public final View mView;
        public final TextView mName;
        public final TextView mDistract;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.name);
            mDistract = (TextView) view.findViewById(R.id.distract);
        }

    }

}
