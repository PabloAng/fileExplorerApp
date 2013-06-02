package com.android.explorer.dropbox;

import android.content.Context;

import com.android.explorer.ExplorerActivity;
import com.android.explorer.ExplorerTabActivity;
import com.android.explorer.R;
import com.dropbox.client2.exception.DropboxException;

public class DropboxDeleteTask extends DropboxTask {

	public DropboxDeleteTask(Context context, DropboxClient dbClient,
			DropboxFile file) {
		super(context, dbClient, file);
		mDialog.setCancelable(false);
	}

	@Override
	protected boolean doSpecificTask() throws DropboxException {
		// Get the metadata for a directory
		mApi.delete(mFile.getPath());
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mDialog.dismiss();
		if (result) {
			((ExplorerTabActivity) ((ExplorerActivity) mContext)
					.getCurrentActivity()).refreshDirectory(null);
		} else
			showToast(mContext.getString(R.string.could_not_delete_msg) + " "
					+ mFile.getName());
	}

	@Override
	protected String getDialogMessage() {
		return mContext.getString(R.string.delete_task_msg) + " "
				+ mFile.getName();
	}

}
