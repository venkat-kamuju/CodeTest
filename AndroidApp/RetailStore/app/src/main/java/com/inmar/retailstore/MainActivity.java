package com.inmar.retailstore;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inmar.retailstore.adapter.SkuListViewAdapter;
import com.inmar.retailstore.common.Constants;
import com.inmar.retailstore.common.Util;
import com.inmar.retailstore.model.SKU;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity class to show list of SKU.
 */
public class MainActivity extends AppCompatActivity implements SkuResultsFilterFragment.OnSkuFilterListener {

    private static final String TAG = "RestActivity";

    private static final String FRAGMENT_FILTER = "filter_fragment";

    //Listview for SKU items
    private ListView mLvSku;

    //List to maintain SKU info
    private ArrayList<SKU> mSkuList;

    //Adapter for SKU list
    private SkuListViewAdapter mSkuListAdapter;

    private TextView mTvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTvStatus = findViewById(R.id.tv_content);
        mLvSku = (ListView) findViewById(R.id.lv_sku);
        mLvSku.setOnItemLongClickListener(mOnSkuItemLongClickListener);
        mSkuList = new ArrayList<SKU>();

        //Floating Plus (+) button. Add SKU activity is launched on tapping it.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InsertSkuActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager manager = getFragmentManager();
        Fragment frag = manager.findFragmentByTag(FRAGMENT_FILTER);
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        int id = item.getItemId();
        if (id == R.id.action_filter_results) {
            SkuResultsFilterFragment skuResultFragment = new SkuResultsFilterFragment();
            skuResultFragment.show(manager, FRAGMENT_FILTER);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Listener for long click of SKU item.
     * An alert is populated to delete the selected item.
     */
    private AdapterView.OnItemLongClickListener mOnSkuItemLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mSkuList != null && mSkuList.size() >= position) {
                deleteSKU(mSkuList.get(position).skuId, mSkuList.get(position).skuDescription);
            } else {
                Log.e(TAG, "SKU info not found, internal error");
            }
            return false;
        }
    };

    /**
     * Show alert dialog to confirm deleting SKU
     * @param skuId
     * @param skuDesc
     */
    private void deleteSKU(final int skuId, final String skuDesc) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.delete_sku);
        alert.setMessage(getString(R.string.delete_sku_message, skuDesc));
        alert.setIcon(android.R.drawable.stat_sys_warning);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                deleteSKU(skuId);
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();

    }

    /**
     * Execute asynchronous task to fetch SKU list from Server
     */
    private void getContent() {
        /*
        if (!isConnected()) {
            return;
        }
        */
        String params = null;
        new FetchSkuTask().execute(params);
    }

    /**
     * Execute asynchronous task to delete selected SKU in Server
     * @param skuId
     */
    private void deleteSKU(int skuId) {
        new DeleteSkuTask().execute(skuId);
    }

    /**
     * Check network connection state
     * @return true if connected, popup a dialog if not.
     */
    private boolean isConnected() {

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Not connected");
            builder.setMessage("Please check your network connection and relauch the app");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //finish();
                        }
                    });
            builder.create().show();
            return false;
        }
    };

    /**
     * Asynchronous task to fetch SKU list from Server
     */
    class FetchSkuTask extends AsyncTask<String, Void, List<SKU>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateStatus(getString(R.string.loading));
        }

        @Override
        protected List<SKU> doInBackground(String... params) {

            String url_locations = "http://192.168.43.45/retail_store/v1/get_sku";
            if (params[0] != null) {
                url_locations += params[0];
            }
            String responseData = Util.getServerData(url_locations);

            List<SKU> skuList = parseSKUData(responseData);
            Log.i(TAG, "SKU count:" + skuList.size());

            return skuList;
        }

        @Override
        protected void onPostExecute(List<SKU> skuList) {
            super.onPostExecute(skuList);
            Log.i(TAG, "Task:onPostExecute");

            if (skuList != null && !skuList.isEmpty()) {
                updateStatus("");
                for (SKU sku : skuList) {
                    Log.i(TAG, sku.toString());
                }
            } else {
                updateStatus(getString(R.string.no_sku_found));
            }

            mSkuList.clear();
            mSkuList.addAll(skuList);
            mSkuListAdapter = new SkuListViewAdapter(MainActivity.this, mSkuList);
            mLvSku.setAdapter(mSkuListAdapter);

            mSkuListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Asynchronous task to delete selected SKU in Server
     */
    class DeleteSkuTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... sku_id) {

            String deleteUrl = "http://192.168.43.45/retail_store/v1/delete_sku/" + sku_id[0] ;

            String responseData = Util.deleteServerData(deleteUrl);

            return parseDeleteSkuResponse(responseData);

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.i(TAG, "Task:onPostExecute");
            if (result) {
                Toast.makeText(MainActivity.this, R.string.sku_deleted, Toast.LENGTH_SHORT).show();
                getContent();
            } else {
                Log.e(TAG, "Failed to delete SKU");
            }
        }

    }

    /**
     * Parse SKU list from JSON to Java list.
     * @param jString
     * @return
     */
    private List<SKU> parseSKUData(String jString){

        Log.i(TAG, "parseSKUData");

        if(jString == null || jString.isEmpty()) {
            Log.e(TAG, "Invalid input");
            return null;
        }

        List<SKU> skuList = new ArrayList<SKU>();
        try {
            JSONObject jObj = new JSONObject(jString);
            if (jObj == null || jObj.length() == 0) {
                return null;
            }

            String resultCode = jObj.getString(Constants.JSON_KEY_RESULT);
            if (resultCode != null && !resultCode.isEmpty()) {
                Log.i(TAG, "ResultCode:" + resultCode);
            }

            JSONArray items = jObj.getJSONArray(Constants.JSON_KEY_SKU);
            if (items == null && items.length() == 0) {
                Log.i(TAG, "SKU list is empty");
                return null;
            }

            Log.i(TAG, "SKU items:" + items.length());

            for (int i = 0; i < items.length(); i++) {

                String skuId = items.getJSONObject(i).getString(Constants.JSON_KEY_SKU_ID);
                String skuName = items.getJSONObject(i).getString(Constants.JSON_KEY_SKU_DESC);

                String locationId = items.getJSONObject(i).getString(Constants.JSON_KEY_LOCATION_ID);
                String locationName = items.getJSONObject(i).getString(Constants.JSON_KEY_LOCATION_NAME);

                String deptId = items.getJSONObject(i).getString(Constants.JSON_KEY_DEPT_ID);
                String deptName = items.getJSONObject(i).getString(Constants.JSON_KEY_DEPT_NAME);

                String categoryId = items.getJSONObject(i).getString(Constants.JSON_KEY_CATEGORY_ID);
                String categoryName = items.getJSONObject(i).getString(Constants.JSON_KEY_CATEGORY_NAME);

                String subCategoryId = items.getJSONObject(i).getString(Constants.JSON_KEY_SUB_CATEGORY_ID);
                String subCategoryName = items.getJSONObject(i).getString(Constants.JSON_KEY_SUB_CATEGORY_NAME);

                SKU sku = new SKU(skuId, skuName, locationId, locationName, deptName, categoryName, subCategoryName);
                skuList.add(sku);
            }


        } catch (JSONException e) {
            Log.e("CatalogClient", "unexpected JSON exception", e);
        }

        Log.i(TAG, "parseSKUData: sku count:" + skuList.size());

        return skuList;
    }

    /**
     * Parse JSON response received on deleting SKU and check for the result.
     * @param responseData
     * @return
     */
    private boolean parseDeleteSkuResponse(String responseData) {
        Log.i(TAG, "parseDeleteSkuResponse");

        boolean result = false;

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
                result = (Integer.parseInt(resultCode) == Constants.RESULT_CODE_SUCCESS);
            }
        } catch (JSONException e) {
            Log.e("DeleteSku", "unexpected JSON exception", e);
        }

        return result;
    }

    @Override
    public void onSkuFilter(String urlParam) {
        Log.d(TAG, "onSkuFilter, urlParam:" + urlParam);
        new FetchSkuTask().execute(urlParam);
    }

    private void updateStatus(String message) {
        if (mTvStatus != null) {
            mTvStatus.setText(message);
        }

    }
}
