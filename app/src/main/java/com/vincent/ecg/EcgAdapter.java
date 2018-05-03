package com.vincent.ecg;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONArray;
import com.vincent.ecg.view.ECGView;
import com.vincent.ecg.view.EcgPointEntity;

import java.util.List;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name ECGView
 * @page com.vincent.ecg
 * @class describe
 * @date 2018/3/7 18:55
 */

public class EcgAdapter extends RecyclerView.Adapter<EcgAdapter.EcgViewHolder> {

    private List<List<EcgPointEntity>> data;
    private Context mContext;

    public EcgAdapter(Context mContext) {
        this.mContext = mContext;
    }


    public void setData(List<List<EcgPointEntity>> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EcgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_layout_ecg,parent,false);
        return new EcgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EcgViewHolder holder, int position) {
        List<EcgPointEntity> datas = data.get(position);
        //为了数据的连续性 需要把每个item的宽度减去2
        holder.itemView.getLayoutParams().width = datas.size() * 4-4;
        Log.d("Item", "onBindViewHolder: "+ JSONArray.toJSONString(datas));
        holder.ecgView.setDatas(datas);
    }

    @Override
    public int getItemCount() {
        return data == null?0:data.size();
    }

    class EcgViewHolder extends RecyclerView.ViewHolder{

        private ECGView ecgView;

        public EcgViewHolder(View itemView) {
            super(itemView);
            ecgView = itemView.findViewById(R.id.ecgView);
        }
    }
}
