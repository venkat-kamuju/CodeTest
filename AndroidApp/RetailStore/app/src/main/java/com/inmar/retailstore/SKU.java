package com.inmar.retailstore;

import android.util.Log;

import java.util.Locale;

public class SKU {
    private static final String TAG = "SKU";
    public int skuId;
    public String skuDescription;
    public int locationId;
    public String locationName;
    public int deptId;
    public String deptName;
    public int categoryId;
    public String categoryName;
    public int subCategoryId;
    public String subCategoryName;

    public SKU(int skuId, String skuDescription, int locationId, String locationName,
               int deptId, String deptName, int categoryId, String categoryName,
               int subCategoryId, String subCategoryName) {
        this.skuId = skuId;
        this.skuDescription = skuDescription;
        this.locationId = locationId;
        this.locationName = locationName;
        this.deptId = deptId;
        this.deptName = deptName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.subCategoryId = subCategoryId;
        this.subCategoryName = subCategoryName;
    }

    public SKU(String skuId, String skuDescription, String locationId, String locationName,
               String deptName, String categoryName, String subCategoryName) {
        if (skuId != null && !skuId.isEmpty()) {
            this.skuId = Integer.parseInt(skuId);
        }

        this.skuDescription = skuDescription;

        if (locationId != null && !locationId.isEmpty()) {
            this.locationId = Integer.parseInt(locationId);
        }

        this.locationName = locationName;
        this.deptName = deptName;
        this.categoryName = categoryName;
        this.subCategoryName = subCategoryName;
    }

    @Override
    public String toString() {
        String str = String.format(Locale.ENGLISH, "SKU ID:%d, NAME:%s, " +
                "Location ID:%d, Location Name:%s, Dept ID:%d, Dept Name:%s, " +
                "Category Id: %d, Category Name:%s, Sub Category Id: %d, Sub Category Name:%s",
                skuId, skuDescription, locationId, locationName, deptId, deptName,
                categoryId, categoryName, subCategoryId, subCategoryName);
        //Log.i(TAG, str);
        return str;
    }
}