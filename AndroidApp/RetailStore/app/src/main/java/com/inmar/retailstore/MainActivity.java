package com.inmar.retailstore;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RestActivity";

    ProgressDialog progress;

    ConnectivityManager mConnMgr;

    private ListView mLvSku;
    private ArrayList<SKU> mSkuList;
    private SkuListViewAdapter mSkuListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        mSkuList = new ArrayList<SKU>();
        mLvSku = (ListView) findViewById(R.id.lv_sku);
        mLvSku.setOnItemLongClickListener(mOnSkuItemLongClickListener);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getContent();
    }

    private AdapterView.OnItemLongClickListener mOnSkuItemLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(MainActivity.this,
                    "SKU: " + mSkuList.get(position).skuDescription, Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
    };

    private void getContent() {

        new RestTask().execute();
        //progress = ProgressDialog.show(this, "Getting Data ...", "Waiting For Results...", true);
/*
        NetworkInfo networkInfo = mConnMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RestTask().execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alert!");
            builder.setMessage("Please check your network connection");
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            builder.create().show();
        }
*/

    }

    class RestTask extends AsyncTask<Void, Void, List<SKU>> {

        @Override
        protected List<SKU> doInBackground(Void... voids) {

            List<SKU> skuList = null;

            try {
                URL url = new URL("http://192.168.43.45/retail_store/v1/sku");

                // Create connection
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                //httpURLConnection.setRequestMethod("GET");

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    InputStream responseBody = httpURLConnection.getInputStream();

                    String responseData = getStringFromInputStream(responseBody);
                    //Log.i(TAG, "Server Response:" + responseData);

                    skuList = parseSKUData(responseData);

                    /*
                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");

                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginObject(); // Start processing the JSON object
                    while (jsonReader.hasNext()) { // Loop through all keys
                        //String key = jsonReader.nextName(); // Fetch the next key
                        //String value = jsonReader.nextString();
                        Log.i(TAG, "JSON");
                    }
                    jsonReader.close();
                    */
                } else {
                    Log.e(TAG, "Failed establishing HTTP connection");
                }

                httpURLConnection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return skuList;
        }

        @Override
        protected void onPostExecute(List<SKU> skuList) {
            super.onPostExecute(skuList);
            Log.i(TAG, "Task:onPostExecute");
            if (skuList != null && !skuList.isEmpty()) {
                for (SKU sku : skuList) {
                    Log.i(TAG, sku.toString());
                }
            }

            mSkuList.clear();
            mSkuList.addAll(skuList);
            mSkuListAdapter = new SkuListViewAdapter(MainActivity.this, mSkuList);
            mLvSku.setAdapter(mSkuListAdapter);

            //mSkuListAdapter.updateList(mSkuList);
            mSkuListAdapter.notifyDataSetChanged();
        }

    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private List<SKU> parseSKUData(String jString){

        Log.i(TAG, "parseSKUData");

        List<SKU> skuList = new ArrayList<SKU>();
        try {
            JSONObject jObj = new JSONObject(jString);
            if (jObj == null || jObj.length() == 0) {
                return null;
            }

            String error = jObj.getString(Constants.JSON_KEY_RESULT);
            if (error != null && !error.isEmpty()) {
                Log.i(TAG, "Error:" + Boolean.parseBoolean(error));
            }

            JSONArray items = jObj.getJSONArray(Constants.JSON_KEY_SKU);
            if (items == null && items.length() == 0) {
                Log.i(TAG, "SKU list is empty");
                return null;
            }

            Log.i(TAG, "SKU items:" + items.length());

            for (int i = 0; i < items.length(); i++) {

                //String title = items.getJSONObject(i).getJSONObject("volumeInfo").getString("title");

                String skuId = items.getJSONObject(i).getString(Constants.JSON_KEY_SKU_ID);
                String skuName = items.getJSONObject(i).getString(Constants.JSON_KEY_SKU_DESC);

                //String locationId = items.getJSONObject(i).getString(Constants.JSON_KEY_LOCATION_ID);
                String locationName = items.getJSONObject(i).getString(Constants.JSON_KEY_LOCATION_NAME);

                //String deptId = items.getJSONObject(i).getString(Constants.JSON_KEY_DEPT_ID);
                String deptName = items.getJSONObject(i).getString(Constants.JSON_KEY_DEPT_NAME);

                //String categoryId = items.getJSONObject(i).getString(Constants.JSON_KEY_CATEGORY_ID);
                String categoryName = items.getJSONObject(i).getString(Constants.JSON_KEY_CATEGORY_NAME);

                //String subCategoryId = items.getJSONObject(i).getString(Constants.JSON_KEY_SUB_CATEGORY_ID);
                String subCategoryName = items.getJSONObject(i).getString(Constants.JSON_KEY_SUB_CATEGORY_NAME);

                SKU sku = new SKU(skuId, skuName, "0", locationName, deptName, categoryName, subCategoryName);
                skuList.add(sku);
            }


        } catch (JSONException e) {
            Log.e("CatalogClient", "unexpected JSON exception", e);
        }

        Log.i(TAG, "parseSKUData: sku count:" + skuList.size());

        return skuList;
    }
}
