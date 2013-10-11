package com.kienle.famous.people.baeyongjin;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.kienle.famous.people.baeyongjin.adapter.GridViewImageAdapter;
import com.kienle.famous.people.baeyongjin.helper.AppConstant;
import com.kienle.famous.people.baeyongjin.helper.Config;
import com.kienle.famous.people.baeyongjin.helper.DialogUtil;
import com.kienle.famous.people.baeyongjin.helper.FileUtils;
import com.kienle.famous.people.baeyongjin.helper.StringUtil;
import com.kienle.famous.people.baeyongjin.helper.Utils;

public class GridViewActivity extends Activity {

	private Utils utils;
	private ArrayList<String> imagePaths = new ArrayList<String>();
	private GridViewImageAdapter mAdapter;
	private GridView mGridView;
	private int columnWidth;
	private SharedPreferences prefs;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grid_view);

		mGridView = (GridView) findViewById(R.id.grid_view);

		prefs = getSharedPreferences("info.androidhive.imageslider", MODE_PRIVATE);
		mProgressDialog = DialogUtil.createProgressDialog(this, StringUtil.getString(R.string.copying_data));
		
		if (prefs.getBoolean("first_run", true)) {
            
            // copy data to sdcard
            String appFolder = FileUtils.getSdcardDir();
            CopyDataTask copyTask = new CopyDataTask();
            copyTask.execute(appFolder);

            prefs.edit().putBoolean("first_run", false).commit();
        }
		
		utils = new Utils(this);

		// Initilizing Grid View
		InitilizeGridLayout();

		// loading all image paths from SD card
		imagePaths = utils.getFilePaths();

		// Gridview adapter
		mAdapter = new GridViewImageAdapter(GridViewActivity.this, imagePaths, columnWidth);

		// setting grid view adapter
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// on selecting grid view image
				// launch full screen activity
				Intent i = new Intent(GridViewActivity.this, FullScreenViewActivity.class);
				i.putExtra("position", position);
				startActivity(i);
			}
		});
	}

	private void InitilizeGridLayout() {
		Resources r = getResources();
		float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				AppConstant.GRID_PADDING, r.getDisplayMetrics());

		columnWidth = (int) ((utils.getScreenWidth() - ((AppConstant.NUM_OF_COLUMNS + 1) * padding)) / AppConstant.NUM_OF_COLUMNS);

		mGridView.setNumColumns(AppConstant.NUM_OF_COLUMNS);
		mGridView.setColumnWidth(columnWidth);
		mGridView.setStretchMode(GridView.NO_STRETCH);
		mGridView.setPadding((int) padding, (int) padding, (int) padding,
				(int) padding);
		mGridView.setHorizontalSpacing((int) padding);
		mGridView.setVerticalSpacing((int) padding);
	}
	
	private class CopyDataTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String appFolder = params[0];
            boolean copyComplete = false;
            try {
                copyComplete = FileUtils.copyDataFromAssetToSd(GridViewActivity.this, Config.DATA_ZIP_FILE, appFolder);
            } catch (IOException e) {
                Log.d("KienLT", "[MainActivity] copy data to sdcard error: " + e.getMessage());
                copyComplete = false;
            }
            return copyComplete;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            hideDialog();
        }

        @Override
        protected void onCancelled() {
            hideDialog();
        }
    };

    private void showDialog() {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    private void hideDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showDialogConfirmExit();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showDialogConfirmExit() {
        DialogUtil.createConfirmExistDialog(this, confirmExitListenner, R.string.confirm_exit);
    }
    
    DialogInterface.OnClickListener confirmExitListenner = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            moveTaskToBack(true);
            finish();
        }
    };

}
