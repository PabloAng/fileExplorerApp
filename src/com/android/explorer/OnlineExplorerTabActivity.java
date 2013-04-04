package com.android.explorer;

import android.os.Bundle;

import com.java.explorer.DropboxClient;
import com.java.explorer.DropboxExplorer;
import com.java.explorer.ExplorerEntity;

public class OnlineExplorerTabActivity extends ExplorerTabActivity {

	private enum ExplorerType {
		DROPBOX, DRIVE;
	};

	// constants
	public static final String STORAGE_CLIENT = "STORAGE CLIENT";
	// private static final String TAG = "OnlineExplorerTabActivity";

	// STORAGE CLIENTS KEYS
	public static final int STORAGE_CLIENT_DROPBOX = 0x1;

	public static final int STORAGE_CLIENT_DRIVE = 0x2;

	private ExplorerType mType = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		int i = extras.getInt(OnlineExplorerTabActivity.STORAGE_CLIENT);
		switch (i) {
		case STORAGE_CLIENT_DROPBOX:
			mType = ExplorerType.DROPBOX;
			break;
		case STORAGE_CLIENT_DRIVE:
			mType = ExplorerType.DRIVE;
			break;
		default:
			mType = ExplorerType.DROPBOX;
			break;
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		if (fManager == null) {
			switch (mType) {
			case DROPBOX:
				fManager = new DropboxExplorer();
				break;
			case DRIVE:
				// fManager = new DropboxExplorer();
				break;
			default:
				fManager = new DropboxExplorer();
				break;
			}
			setViews();
			// initHomeList();
			goHomeView();
		}
	}

	@Override
	public void downloadFile(ExplorerEntity entity) {
		DownloadFile db = new DownloadFile(
				OnlineExplorerTabActivity.this.getParent(),
				DropboxClient.getInstance(), entity.getPath());
		db.execute();
	}
	//
	// @Override
	// public void openData(ExplorerEntity e){
	// super.openData(e);
	// DropboxClient.getInstance().logOut();
	// }
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
