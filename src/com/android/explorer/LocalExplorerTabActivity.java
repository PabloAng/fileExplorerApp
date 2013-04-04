package com.android.explorer;

import android.os.Bundle;

import com.java.explorer.ExplorerEntity;
import com.java.explorer.SdCardExplorer;

public class LocalExplorerTabActivity extends ExplorerTabActivity {

	// constants
	public static final String HOME = "INTERNAL_HOME_PATH_FOLDER";
	//private static final String TAG = "ExplorerTabActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		fManager = new SdCardExplorer();
		setViews();
		goHomeView();

	}

	@Override
	public void downloadFile(ExplorerEntity entity) {
		
	}

	// /***************** OPTION MENU SETUP *******************/
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.local_tab_menu, menu);
	//
	// return true;
	// }
	//
	// // This method is called once the menu is selected
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (!getCurrentDirectory().exists())
	// return false;
	// switch (item.getItemId()) {
	//
	// case R.id.config:
	//
	// break;
	// case R.id.new_folder:
	// createNewFolder();
	// break;
	// }
	// return true;
	// }

}
