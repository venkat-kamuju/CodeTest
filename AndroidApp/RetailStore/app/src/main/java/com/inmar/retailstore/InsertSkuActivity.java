package com.inmar.retailstore;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
 * Activity class to add new SKU
 */
public class InsertSkuActivity extends AppCompatActivity {

    private static final String TAG = InsertSkuActivity.class.getSimpleName();

    //Private members to hold UI elements
    private Spinner mSpLocations, mSpDept, mSpCategory, mSpSubCategory;
    private EditText mEtSku;
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

    private String mApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_insert_sku);

        Intent intent = getIntent();
        if (intent != null) {
            mApiKey = intent.getStringExtra(Constants.INTENT_EXTRA_API_KEY);
            Log.i(TAG, "Api Key:" + mApiKey);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Hold the references of UI controls
        mEtSku = findViewById(R.id.et_sku);
        mSpLocations = findViewById(R.id.sp_location);
        mSpDept = findViewById(R.id.sp_dept);
        mSpCategory = findViewById(R.id.sp_category);
        mSpSubCategory = findViewById(R.id.sp_sub_category);
        mBtnSave = findViewById(R.id.btn_save);

        //Setup adapters
        mLocationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mLocationList);
        mSpLocations.setAdapter(mLocationAdapter);

        mDeptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mDeptList);
        mSpDept.setAdapter(mDeptAdapter);
        mSpDept.setOnItemSelectedListener(onDeptSelectedListener);

        mCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mCategoryList);
        mSpCategory.setAdapter(mCategoryAdapter);
        mSpCategory.setOnItemSelectedListener(onCategorySelectedListener);

        mSubCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                mSubCategoryList);
        mSpSubCategory.setAdapter(mSubCategoryAdapter);

        mBtnSave.setOnClickListener(onSaveListener);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        new RestTask().execute();
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
     * Listener for Save button.
     * SKU info is posted to Server through REST API on tapping Save button
     */
    private View.OnClickListener onSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String skuDesc = mEtSku.getText().toString();

            //Null check on SKU description
            if (skuDesc == null || skuDesc.isEmpty()) {
                Toast.makeText(InsertSkuActivity.this, R.string.no_sku_desc,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            //Read selected location, department, category and sub-category
            String location = mSpLocations.getSelectedItem().toString();
            String dept = mSpDept.getSelectedItem().toString();
            String category = mSpCategory.getSelectedItem().toString();
            String subCategory = mSpSubCategory.getSelectedItem().toString();

            //Get Location ID and Sub category ID for the selected ones
            int locationId = getLocationIdByName(location);
            int subCategoryId = getSubCategoryIdByName(dept, category, subCategory);

            //Prepare an URL encoded string for params
            String urlParamString = getParamString(skuDesc, locationId, subCategoryId);

            //Run an asynchronous task to post SKU to server
            new PostSkuDataTask().execute(urlParamString);
        }
    };

    /**
     * Extended class to run Asynchronous task to fetch locations and metadata
     */
    class RestTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {

            //Get locations from REST API
            String url_locations = Constants.BASE_URL + "/get_locations";

            String responseLocations = Util.getServerData(url_locations);

            mLocationsMap = parseLocations(responseLocations);
            Log.i(TAG, "Locations count:" + mLocationsMap.size());

            //Get metadata from REST API
            String url_metadata = Constants.BASE_URL + "/get_metadata";

            String responseMetadata = Util.getServerData(url_metadata);

            mMetadataList = parseMetadata(responseMetadata);

            Log.i(TAG, "Metadata count:" + mMetadataList.size());

            return true;
        };

        @Override
        protected void onPostExecute(Boolean value) {
            super.onPostExecute(value);

            for (String location : mLocationsMap.values()) {
                if (location != null && !mLocationList.contains(location)) {
                    mLocationList.add(location);
                    Log.i(TAG, "Location:" + location);
                }
            }
            mLocationAdapter.notifyDataSetChanged();

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
     * Extended class to run Asynchronous task to post sku info onto server
     */
    class PostSkuDataTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... jsonStrings) {

            //Get locations from REST API
            String url_locations = Constants.BASE_URL + "/insert_sku";

            String responseData = Util.postServerData(url_locations, jsonStrings[0], mApiKey);

            int result = parseInsertSkuResponse(responseData);

            return result;
        };

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == Constants.RESULT_CODE_SUCCESS) {
                Toast.makeText(InsertSkuActivity.this, R.string.sku_added,
                        Toast.LENGTH_SHORT).show();
                finish();
            } else if (result == Constants.RESULT_CODE_SKU_ALREADY_EXIST) {
                Toast.makeText(InsertSkuActivity.this, R.string.sku_duplicate,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(InsertSkuActivity.this, R.string.sku_adding_failed,
                        Toast.LENGTH_SHORT).show();
            }
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
     * Parse JSON response received on posting the SKU and check for the result codes.
     * @param responseData
     * @return
     */
    private int parseInsertSkuResponse(String responseData) {
        Log.i(TAG, "parseLocationData");

        int result = Constants.RESULT_CODE_FAIL;

        if(responseData == null || responseData.isEmpty()) {
            Log.e(TAG, "Invalid response");
            return result;
        }

        try {
            JSONObject jObj = new JSONObject(responseData);
            if (jObj == null || jObj.length() == 0) {
                return result;
            }

            String resultCode = jObj.getString(Constants.JSON_KEY_RESULT);
            if (resultCode != null && !resultCode.isEmpty()) {
                Log.i(TAG, "ResultCode:" + resultCode);
                result = Integer.parseInt(resultCode);
            }
        } catch (JSONException e) {
            Log.e("InsertSku", "unexpected JSON exception", e);
        }

        return result;
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
        for (int i=0; i < mMetadataList.size(); i++) {
            Metadata metadata = mMetadataList.get(i);
            if (metadata.deptName.equals(dept) && !mCategoryList.contains(metadata.categoryName)) {
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
        for (int i=0; i < mMetadataList.size(); i++) {
            Metadata metadata = mMetadataList.get(i);
            if (metadata.categoryName.equals(category) && !mSubCategoryList.contains(metadata.subCategoryName)) {
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
    private int getSubCategoryIdByName(String dept, String category, String subCategory) {
        int key = -1;
        for (int i = 0; i < mMetadataList.size(); i++) {
            Metadata metadata = mMetadataList.get(i);
            if (metadata.deptName.equals(dept) && metadata.categoryName.equals(category)
                    && metadata.subCategoryName.equals(subCategory)) {
                key = metadata.subCategoryId;
            }
        }
        return key;
    }

    /**
     * Return URL encoded parameter string for the supplied arguments.
     * @param skuDesc
     * @param locationId
     * @param subCategoryId
     * @return
     */
    private String getParamString(String skuDesc, int locationId, int subCategoryId) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(Constants.JSON_KEY_SKU_DESC, skuDesc);
        paramMap.put(Constants.JSON_KEY_LOCATION_ID, Integer.toString(locationId));
        paramMap.put(Constants.JSON_KEY_META_INFO_ID, Integer.toString(subCategoryId));
        String paramString = Util.getQueryStringForParameters(paramMap);
        return paramString;
    }
}
