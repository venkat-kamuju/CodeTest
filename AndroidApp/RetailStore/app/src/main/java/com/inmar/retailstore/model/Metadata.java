package com.inmar.retailstore.model;

import java.util.Locale;

/**
 * Container class for holding Metadata - Department, Category and SubCategory
 */
public class Metadata {

    private static final String TAG = Metadata.class.getSimpleName();

    public int deptId;

    public String deptName;

    public int categoryId;

    public String categoryName;

    public int subCategoryId;

    public String subCategoryName;

    public Metadata(String deptId, String deptName, String categoryId, String categoryName,
                    String subCategoryId, String subCategoryName) {

        if (deptId != null && !deptId.isEmpty()) {
            this.deptId = Integer.parseInt(deptId);
        }

        this.deptName = deptName;

        if (categoryId != null && !categoryId.isEmpty()) {
            this.categoryId = Integer.parseInt(categoryId);
        }

        this.categoryName = categoryName;

        if (subCategoryId != null && !subCategoryId.isEmpty()) {
            this.subCategoryId = Integer.parseInt(subCategoryId);
        }

        this.subCategoryName = subCategoryName;
    }

    @Override
    public String toString() {
        String str = String.format(Locale.ENGLISH, "Dept ID:%d, Dept Name:%s, " +
                "Category Id: %d, Category Name:%s, Sub Category Id: %d, Sub Category Name:%s",
                deptId, deptName, categoryId, categoryName, subCategoryId, subCategoryName);
        //Log.i(TAG, str);
        return str;
    }
}