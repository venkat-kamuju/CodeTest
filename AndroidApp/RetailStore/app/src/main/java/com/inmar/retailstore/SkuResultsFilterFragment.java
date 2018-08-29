package com.inmar.retailstore;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.inmar.retailstore.common.Constants;
import com.inmar.retailstore.common.Util;
import com.inmar.retailstore.model.Metadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Show filters for locations, departments, categories and sub-categories.
 * And filter the SKU results for the selected options.
 */
public class SkuResultsFilterFragment extends DialogFragment{

    public static final String TAG = SkuResultsFilterFragment.class.getSimpleName();

    private static final Integer FILTER_ALL_KEY = -1;
    private static final String FILTER_ALL_VALUE = "All";

    private Spinner mSpLocations, mSpDept, mSpCategory, mSpSubCategory;
    private Button mBtnSave;

    //Adapters to help in populating the lists and to shortlist items on selection
    private ArrayAdapter<String> mLocationAdapter, mDeptAdapter,
            mCategoryAdapter, mSubCategoryAdapter;

    //Container member to maintain location, departments, category and sub-category lists.
    private Map<Integer, String> mLocationsMap = null;
    private List<Metadata> mMetadataList = null;
    private List<String> mLocationList = new ArrayList<>();
    private List<String> mDeptList = new ArrayList<>();
    private List<String> mCategoryList = new ArrayList<>();
    private List<String> mSubCategoryList = new ArrayList<>();

    //Listener to return the selected filters to main activity
    private OnSkuFilterListener mListener;

    //Empty constructor
    public SkuResultsFilterFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.i(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.dialog_filter_by_metada, container);

        //Hold the references of UI controls
        mSpLocations = view.findViewById(R.id.sp_location);
        mSpDept = view.findViewById(R.id.sp_dept);
        mSpCategory = view.findViewById(R.id.sp_category);
        mSpSubCategory = view.findViewById(R.id.sp_sub_category);
        mBtnSave = view.findViewById(R.id.btn_save);

        //Setup adapters
        mLocationAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                mLocationList);
        mSpLocations.setAdapter(mLocationAdapter);

        mDeptAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                mDeptList);
        mSpDept.setAdapter(mDeptAdapter);
        mSpDept.setOnItemSelectedListener(onDeptSelectedListener);

        mCategoryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                mCategoryList);
        mSpCategory.setAdapter(mCategoryAdapter);
        mSpCategory.setOnItemSelectedListener(onCategorySelectedListener);

        mSubCategoryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,
                mSubCategoryList);
        mSpSubCategory.setAdapter(mSubCategoryAdapter);

        mBtnSave.setOnClickListener(onSaveListener);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
        if (context instanceof OnSkuFilterListener) {
            mListener = (OnSkuFilterListener) context;
        }
        new RestTask().execute();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSkuFilterListener {
        void onSkuFilter(String url);
    }

    /**
     * Listener for Department selection.
     * Different list of categories and sub-categories populated for selected department.
     */
    private AdapterView.OnItemSelectedListener onDeptSelectedListener
            = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItem = parent.getItemAtPosition(position).toString();
            populateCategoryForDept(selectedItem);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            //do nothing
        }

    };

    /**
     * Listener for Category selection.
     * Different list of Sub-categories populated for selected category.
     */
    private AdapterView.OnItemSelectedListener onCategorySelectedListener
            = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItem = parent.getItemAtPosition(position).toString();
            populateSubCategoryForCategory(selectedItem);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            //do nothing
        }

    };

    /**
     * Extended class to run Asynchronous task to fetch locations and metadata
     */
    class RestTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            //Get locations from REST API
            String url_locations = "http://192.168.43.45/retail_store/v1/get_locations";

            String responseLocations = Util.getServerData(url_locations);

            mLocationsMap = parseLocations(responseLocations);
            Log.i(TAG, "Locations count:" + mLocationsMap.size());

            //Get metadata from REST API
            String url_metadata = "http://192.168.43.45/retail_store/v1/get_metadata";

            String responseMetadata = Util.getServerData(url_metadata);

            mMetadataList = parseMetadata(responseMetadata);

            Log.i(TAG, "Metadata count:" + mMetadataList.size());

            return true;
        };

        @Override
        protected void onPostExecute(Boolean value) {
            super.onPostExecute(value);

            mLocationList.add(FILTER_ALL_VALUE);
            for (String location : mLocationsMap.values()) {
                if (location != null && !mLocationList.contains(location)) {
                    mLocationList.add(location);
                    Log.i(TAG, "Location:" + location);
                }
            }
            mLocationAdapter.notifyDataSetChanged();

            mDeptList.add(FILTER_ALL_VALUE);
            for (int i=0; i < mMetadataList.size(); i++) {
                String deptName = mMetadataList.get(i).deptName;
                if (deptName != null && !mDeptList.contains(deptName)) {
                    mDeptList.add(deptName);
                    Log.i(TAG, "Dept:" + deptName);
                }
            }
            mDeptAdapter.notifyDataSetChanged();
            mSpDept.setSelection(0);
        }
    }

    /**
     * Parse Metadata from JSON to Java members.
     * @param jString
     * @return
     */
    private List<Metadata> parseMetadata(String jString){

        Log.i(TAG, "parseMetaData");

        if(jString == null || jString.isEmpty()) {
            Log.e(TAG, "Invalid input");
            return null;
        }

        List<Metadata> metadataList = new ArrayList<Metadata>();
        try {
            JSONObject jObj = new JSONObject(jString);
            if (jObj == null || jObj.length() == 0) {
                return null;
            }

            String resultCode = jObj.getString(Constants.JSON_KEY_RESULT);
            if (resultCode != null && !resultCode.isEmpty()) {
                Log.i(TAG, "ResultCode:" + resultCode);
            }

            JSONArray items = jObj.getJSONArray(Constants.JSON_KEY_METADATA);
            if (items == null && items.length() == 0) {
                Log.i(TAG, "Metadata list is empty");
                return null;
            }

            Log.i(TAG, "Metdata items:" + items.length());

            for (int i = 0; i < items.length(); i++) {

                String deptId = items.getJSONObject(i).getString(Constants.JSON_KEY_DEPT_ID);
                String deptName = items.getJSONObject(i).getString(Constants.JSON_KEY_DEPT_NAME);

                String categoryId = items.getJSONObject(i).getString(Constants.JSON_KEY_CATEGORY_ID);
                String categoryName = items.getJSONObject(i).getString(Constants.JSON_KEY_CATEGORY_NAME);

                String subCategoryId = items.getJSONObject(i).getString(Constants.JSON_KEY_SUB_CATEGORY_ID);
                String subCategoryName = items.getJSONObject(i).getString(Constants.JSON_KEY_SUB_CATEGORY_NAME);

                Metadata metadata = new Metadata(deptId, deptName, categoryId, categoryName,
                        subCategoryId, subCategoryName);
                metadataList.add(metadata);
            }

        } catch (JSONException e) {
            Log.e("CatalogClient", "unexpected JSON exception", e);
        }

        Log.i(TAG, "parseMetaData: Metadata count:" + metadataList.size());

        return metadataList ;
    }

    /**
     * Parse Location list from JSON to Java Map.
     * @param jString
     * @return
     */
    private Map<Integer, String> parseLocations(String jString){

        Log.i(TAG, "parseLocationData");

        if(jString == null || jString.isEmpty()) {
            Log.e(TAG, "Invalid input");
            return null;
        }

        Map<Integer, String> mapLocations = new HashMap<>();
        mapLocations.put(FILTER_ALL_KEY, FILTER_ALL_VALUE);

        try {
            JSONObject jObj = new JSONObject(jString);
            if (jObj == null || jObj.length() == 0) {
                return null;
            }

            String resultCode = jObj.getString(Constants.JSON_KEY_RESULT);
            if (resultCode != null && !resultCode.isEmpty()) {
                Log.i(TAG, "ResultCode:" + resultCode);
            }

            JSONArray items = jObj.getJSONArray(Constants.JSON_KEY_LOCATION);
            if (items == null && items.length() == 0) {
                Log.i(TAG, "Location list is empty");
                return null;
            }

            Log.i(TAG, "Location items:" + items.length());

            for (int i = 0; i < items.length(); i++) {

                String locationId = items.getJSONObject(i).getString(Constants.JSON_KEY_LOCATION_ID);
                String locationName = items.getJSONObject(i).getString(Constants.JSON_KEY_LOCATION_NAME);

                Log.i(TAG, "Location adding to map:" + "id:" + locationId + ", name: " + locationName);

                mapLocations.put(Integer.parseInt(locationId), locationName);
            }

        } catch (JSONException e) {
            Log.e("CatalogClient", "unexpected JSON exception", e);
        }

        Log.i(TAG, "parseLocationData: location count:" + mapLocations.size());

        return mapLocations;
    }

    /**
     * Populate category list for selected department
     * @param dept
     */
    private void populateCategoryForDept(String dept) {

        if (dept == null) {
            dept = mSpDept.getSelectedItem().toString();
        }

        mCategoryList.clear();
        mCategoryList.add(FILTER_ALL_VALUE);//FILTER_ALL_KEY,

        for (int i=0; i < mMetadataList.size(); i++) {
            Metadata metadata = mMetadataList.get(i);
            if (dept.equals(FILTER_ALL_VALUE) ||
                    (metadata.deptName.equals(dept)
                            && !mCategoryList.contains(metadata.categoryName))) {
                mCategoryList.add(metadata.categoryName);
                Log.i(TAG, "CategoryName:" + metadata.categoryName);
            }
        }
        mCategoryAdapter.notifyDataSetChanged();
        mSpCategory.setSelection(0);

        populateSubCategoryForCategory(null);
    }

    /**
     * Populate sub-category list for selected category
     * @param category
     */
    private void populateSubCategoryForCategory(String category) {

        if (category == null) {
            category = mSpCategory.getSelectedItem().toString();
        }

        mSubCategoryList.clear();
        mSubCategoryList.add(FILTER_ALL_VALUE);
        for (int i=0; i < mMetadataList.size(); i++) {
            Metadata metadata = mMetadataList.get(i);
            if (category.equals(FILTER_ALL_VALUE) ||
                    (metadata.categoryName.equals(category)
                            && !mSubCategoryList.contains(metadata.subCategoryName))) {
                mSubCategoryList.add(metadata.subCategoryName);
                Log.i(TAG, "SubCategoryName:" + metadata.subCategoryName);
            }
        }
        mSubCategoryAdapter.notifyDataSetChanged();

    }

    /**
     * Return Location Id for the given Location name
     * @param name
     * @return
     */
    private int getLocationIdByName(String name) {

        int key = -1;

        if (name == null || name.equals(FILTER_ALL_VALUE)) {
            return key;
        }

        if (mLocationsMap == null || mLocationsMap.isEmpty() || name == null || name.isEmpty()) {
            Log.e(TAG, "Invalid input");
            return key;
        }

        for(Map.Entry entry: mLocationsMap.entrySet()){
            if(name.equals(entry.getValue())){
                key = (int) entry.getKey();
                break;
            }
        }
        return key;
    }

    /**
     * Return Sub-category Id for the given dept, category and sub-category
     * @param dept
     * @param category
     * @param subCategory
     * @return
     */
    private Metadata getMetadata(String dept, String category, String subCategory) {

        Metadata filteredMetadata = new Metadata(FILTER_ALL_KEY, FILTER_ALL_KEY, FILTER_ALL_KEY);

        for (int i = 0; i < mMetadataList.size(); i++) {

            Metadata metadata = mMetadataList.get(i);

            if (!dept.equals(FILTER_ALL_VALUE) && metadata.deptName.equals(dept)) {
                filteredMetadata.deptId = metadata.deptId;
            }

            if (!category.equals(FILTER_ALL_VALUE) && metadata.categoryName.equals(category)) {
                filteredMetadata.categoryId = metadata.categoryId;
            }

            if (!subCategory.equals(FILTER_ALL_VALUE) && metadata.subCategoryName.equals(subCategory)) {
                filteredMetadata.subCategoryId = metadata.subCategoryId;
            }

        }
        return filteredMetadata;
    }

    /**
     * Return URL encoded parameter string for the supplied arguments.
     * @param locationId
     * @param metadata
     * @return
     */
    private String getParamString(int locationId, Metadata metadata) {
        Map<String, String> paramMap = new HashMap<>();

        if (locationId != FILTER_ALL_KEY) {
            paramMap.put(Constants.JSON_KEY_LOCATION_ID, Integer.toString(locationId));
        }

        if (metadata != null) {
            if (metadata.deptId != FILTER_ALL_KEY) {
                paramMap.put(Constants.JSON_KEY_DEPT_ID, Integer.toString(metadata.deptId));
            }
            if (metadata.categoryId != FILTER_ALL_KEY) {
                paramMap.put(Constants.JSON_KEY_CATEGORY_ID, Integer.toString(metadata.categoryId));
            }
            if (metadata.subCategoryId != FILTER_ALL_KEY) {
                paramMap.put(Constants.JSON_KEY_SUB_CATEGORY_ID, Integer.toString(metadata.subCategoryId));
            }
        }

        String paramString = Util.getQueryStringForParameters(paramMap);
        if (paramString != null && !paramString.isEmpty()) {
            paramString = "?" + paramString;
        }
        return paramString;
    }

    /**
     * Listener for Save button.
     * SKU info is posted to Server through REST API on tapping Save button
     */
    private View.OnClickListener onSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //Read selected location, department, category and sub-category
            String location = mSpLocations.getSelectedItem().toString();
            String dept = mSpDept.getSelectedItem().toString();
            String category = mSpCategory.getSelectedItem().toString();
            String subCategory = mSpSubCategory.getSelectedItem().toString();

            //Get Location ID and Sub category ID for the selected ones
            int locationId = getLocationIdByName(location);
            Metadata metadata = getMetadata(dept, category, subCategory);

            //Prepare an URL encoded string for params
            String urlParamString = getParamString(locationId, metadata);

            if (mListener != null) {
                mListener.onSkuFilter(urlParamString);
            }
            dismiss();
        }
    };
}
