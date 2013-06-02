package com.android.explorer.dropbox;

import android.content.Context;

import com.android.explorer.ExplorerActivity;
import com.android.explorer.ExplorerTabActivity;
import com.android.explorer.R;
import com.dropbox.client2.exception.DropboxException;

public class DropboxRenameTask extends DropboxTask {

	private DropboxFile mDestination;

	public DropboxRenameTask(Context context, DropboxClient dbClient,
			DropboxFile origin, DropboxFile destination) {
		super(context, dbClient, origin);
		this.mDestination = destination;
		mDialog.setCancelable(false);
	}

	@Override
	protected boolean doSpecificTask() throws DropboxException {
		// Get the metadata for a directory
		mApi.move(mFile.getPath(), mDestination.getPath());
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mDialog.dismiss();
		if (result) {
			((ExplorerTabActivity) ((ExplorerActivity) mContext)
					.getCurrentActivity()).refreshDirectory(null);
		} else
			showToast(mContext.getString(R.string.could_not_move_msg) + " "
					+ mFile.getName());
	}

	@Override
	protected String getDialogMessage() {
		return mContext.getString(R.string.move_task_msg) + " "
				+ mFile.getName();
	}

}
