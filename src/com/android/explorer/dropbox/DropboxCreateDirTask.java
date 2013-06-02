package com.android.explorer.dropbox;

import android.content.Context;

import com.android.explorer.ExplorerActivity;
import com.android.explorer.ExplorerTabActivity;
import com.android.explorer.ExternalStorage;
import com.android.explorer.R;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

public class DropboxCreateDirTask extends DropboxTask {

	public DropboxCreateDirTask(Context context, DropboxClient dbClient,
			DropboxFile file) {
		super(context, dbClient, file);
		mDialog.setCancelable(false);
	}

	@Override
	protected boolean doSpecificTask() throws DropboxException {
		// Get the metadata for a directory
		Entry entry = mApi.createFolder(mFile.getPath());
		if (entry != null) {
			ExternalStorage.getInstance().saveMetadata(DropboxFile.DROPBOX_CACHE,
					entry);
			return true;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mDialog.dismiss();
		if (result) {
			mFile.checkEntryUpdate();
			((ExplorerTabActivity) ((ExplorerActivity) mContext)
					.getCurrentActivity()).refreshDirectory(null);
		} else
			showToast(mContext.getString(R.string.could_not_create_dir_msg));
	}

	@Override
	protected String getDialogMessage() {
		return mContext.getString(R.string.create_dir_task_msg) + " "
				+ mFile.getName();
	}

}
