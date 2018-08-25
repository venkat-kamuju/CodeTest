package com.inmar.retailstore;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SkuListViewAdapter extends BaseAdapter {

    public static final String TAG = SkuListViewAdapter.class.getSimpleName();

    public ArrayList<SKU> skuList;

    Activity activity;

    public SkuListViewAdapter(Activity activity, ArrayList<SKU> skuList) {
        super();
        this.activity = activity;
        this.skuList = skuList;
    }

    public void updateList(ArrayList<SKU> skuList) {
        if (this.skuList != null) {
            this.skuList.clear();
        }
        this.skuList = skuList;
    }

    @Override
    public int getCount() {
        return skuList.size();
    }

    @Override
    public Object getItem(int position) {
        return skuList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView mTvSKU;
        TextView mTvLocation;
        TextView mTvDept;
        TextView mTvCategory;
        TextView mTvSubCategory;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView:position:" + position);
        ViewHolder holder;
        LayoutInflater inflater = activity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_row, null);
            holder = new ViewHolder();
            holder.mTvSKU = convertView.findViewById(R.id.tv_sku);
            holder.mTvLocation = convertView.findViewById(R.id.tv_location);
            holder.mTvDept = convertView.findViewById(R.id.tv_dept);
            holder.mTvCategory = convertView.findViewById(R.id.tv_category);
            holder.mTvSubCategory = convertView.findViewById(R.id.tv_sub_category);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SKU item = skuList.get(position);
        holder.mTvSKU.setText(item.skuDescription);
        holder.mTvLocation.setText(item.locationName);
        holder.mTvDept.setText(item.deptName);
        holder.mTvCategory.setText(item.categoryName);
        holder.mTvSubCategory.setText(item.subCategoryName);

        return convertView;
    }
}
