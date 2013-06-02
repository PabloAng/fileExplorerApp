package com.android.explorer;

import android.os.Bundle;
import android.view.ContextMenu;

import com.java.explorer.ExplorerEntity;
import com.java.explorer.SdCardExplorer;

public class LocalExplorerTabActivity extends ExplorerTabActivity {

	// constants
	public static final String HOME = "INTERNAL_HOME_PATH_FOLDER";

	// private static final String TAG = "ExplorerTabActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mExplorer = new SdCardExplorer();
		setViews();
		goHomeView();

	}

	/***************** CONTEXT MENU SETUP *******************/
	@Override
	public void onItemSelected(ExplorerEntity entity, int itemId) {

		switch (itemId) {
		case R.id.menu_open:
			openData(entity);
			break;
		case R.id.menu_copy:
			copyData(entity);
			break;
		case R.id.menu_move:
			moveData(entity);
			break;
		case R.id.menu_remove:
			removeData(entity);
			break;
		case R.id.menu_rename:
			renameData(entity);
			break;
		default:
			break;
		}

	}

	@Override
	public void inflateContextMenu(ContextMenu menu, ExplorerEntity entity) {
		getMenuInflater().inflate(R.menu.dir_item_menu, menu);
	}
}
