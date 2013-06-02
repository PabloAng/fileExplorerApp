package com.android.explorer.dropbox;

import android.content.Context;

import com.android.explorer.ExplorerActivity;
import com.android.explorer.ExplorerTabActivity;
import com.android.explorer.ExternalStorage;
import com.android.explorer.R;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

public class DropboxUpdateTask extends DropboxTask {

	public DropboxUpdateTask(Context context, DropboxClient dbClient,
			DropboxFile file) {
		super(context, dbClient, file);
	}

	@Override
	protected boolean doSpecificTask() throws DropboxException {
		// Get the metadata for a directory
		Entry entry = mApi.metadata(mFile.getPath(), 1000, null, true, null);
		if (mCanceled)
			return false;

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
		if (result && !mCanceled && mFile.checkEntryUpdate()) {
			((ExplorerTabActivity) ((ExplorerActivity) mContext)
					.getCurrentActivity()).refreshUI(null);
		}
	}

	@Override
	protected String getDialogMessage() {
		return mContext.getString(R.string.update_dir_task_msg) + " "
				+ mFile.getName();
	}

}
