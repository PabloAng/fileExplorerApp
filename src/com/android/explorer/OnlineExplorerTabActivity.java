package com.android.explorer;

import java.io.File;

import android.os.Bundle;
import android.view.ContextMenu;

import com.android.explorer.dropbox.DropboxExplorer;
import com.android.explorer.dropbox.DropboxFile;
import com.android.explorer.dropbox.DropboxClient;
import com.android.explorer.dropbox.DropboxDownloadTask;
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

		if (mExplorer == null) {
			switch (mType) {
			case DROPBOX:
				mExplorer = new DropboxExplorer();
				break;
			case DRIVE:
				// fManager = new DropboxExplorer();
				break;
			default:
				mExplorer = new DropboxExplorer();
				break;
			}
			setViews();
			// initHomeList();
			goHomeView();
		}
		

	}

	@Override
	public void onItemSelected(ExplorerEntity entity, int itemId) {

		switch (itemId) {
		case R.id.menu_open:
			openData(entity);
			break;
		case R.id.menu_update_cache:
			updateLocalCopyData(entity);
			break;
		case R.id.menu_remove_cache:
			removeLocalCopyData(entity);
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

	protected void openFile(ExplorerEntity entity) {
		if (entity.toUri() != null)
			super.openFile(entity);
		else
			this.downloadFile(entity);
	}

	public void downloadFile(ExplorerEntity entity) {
		DropboxDownloadTask db = new DropboxDownloadTask(
				OnlineExplorerTabActivity.this.getParent(),
				DropboxClient.getInstance(), (DropboxFile) entity);
		db.execute();
	}

	protected void removeLocalCopyData(ExplorerEntity d) {
		File f = new File(d.toUri().getPath());
		f.delete();
		updateListItems();
	}

	protected void updateLocalCopyData(ExplorerEntity d) {
		downloadFile(d);
	}

	@Override
	public void inflateContextMenu(ContextMenu menu, ExplorerEntity entity) {
		if (entity.isFile() && entity.toUri() != null)
			getMenuInflater().inflate(R.menu.online_dir_item_menu, menu);
		else
			getMenuInflater().inflate(R.menu.dir_item_menu, menu);
	}

}
