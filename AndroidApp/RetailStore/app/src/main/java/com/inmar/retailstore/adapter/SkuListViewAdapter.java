package com.inmar.retailstore.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inmar.retailstore.R;
import com.inmar.retailstore.model.SKU;

import java.util.ArrayList;

/**
 * Adapter class to show list of SKU
 */
public class SkuListViewAdapter extends BaseAdapter {

    public static final String TAG = SkuListViewAdapter.class.getSimpleName();

    public ArrayList<SKU> mListSku;

    Activity mActivity;

    public SkuListViewAdapter(Activity activity, ArrayList<SKU> skuList) {
        super();
        this.mActivity = activity;
        this.mListSku = skuList;
    }

    @Override
    public int getCount() {
        return mListSku.size();
    }

    @Override
    public Object getItem(int position) {
        return mListSku.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Container to hold elements of each item of the SKU list
     */
    private class ViewHolder {
        TextView mTvSKU;
        TextView mTvLocation;
        TextView mTvDept;
        TextView mTvCategory;
        TextView mTvSubCategory;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.i(TAG, "getView:position:" + position);
        ViewHolder holder;
        LayoutInflater inflater = mActivity.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.sku_row, null);
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

        SKU item = mListSku.get(position);
        holder.mTvSKU.setText(item.skuDescription);
        holder.mTvLocation.setText(mActivity.getString(R.string.label_location, item.locationName));
        holder.mTvDept.setText(mActivity.getString(R.string.label_dept, item.deptName));
        holder.mTvCategory.setText(mActivity.getString(R.string.label_category, item.categoryName));
        holder.mTvSubCategory.setText(mActivity.getString(R.string.label_sub_category, item.subCategoryName));

        return convertView;
    }
}
