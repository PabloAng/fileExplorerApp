package com.android.explorer.dropbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;

import com.android.explorer.ExplorerActivity;
import com.android.explorer.ExplorerTabActivity;
import com.android.explorer.ExternalStorage;
import com.android.explorer.R;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

public class DropboxDownloadTask extends DropboxTask {

	private FileOutputStream mFos;
	protected Long mFileLen;

	// Note that, since we use a single file name here for simplicity, you
	// won't be able to use this code for two simultaneous downloads.
	// private final static String IMAGE_FILE_NAME = "dbroulette.png";

	public DropboxDownloadTask(Context context, DropboxClient dbClient,
			DropboxFile file) {
		super(context, dbClient, file);
	}

	@Override
	protected boolean doSpecificTask() throws DropboxException {
		// Get the metadata for a directory
		Entry entry = mApi.metadata(mFile.getPath(), 1000, null, true, null);
		if (mCanceled)
			return false;

		try {
			mFos = new FileOutputStream(ExternalStorage.getInstance()
					.getAbsolutePath("Dropbox", "." + entry.path));
		} catch (FileNotFoundException e) {
			mErrorMsg = "Couldn't create a local file to store the image";
			return false;
		}

		// This download the file.
		mApi.getFile(mFile.getPath(), null, mFos, null);
		publishProgress(mFile.length());
		if (mCanceled)
			return false;

		return true;
	}

	@Override
	protected void onProgressUpdate(Long... progress) {
		int percent = (int) (100.0 * (double) progress[0] / mFile.length() + 0.5);
		mDialog.setProgress(percent);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (!mCanceled && result) {
			mDialog.dismiss();
			showToast(mContext.getString(R.string.download_compleated_msg));
			((ExplorerTabActivity) ((ExplorerActivity) mContext)
					.getCurrentActivity()).updateListItems();
			return;
		}
		if (mCanceled) {
			(new File(ExternalStorage.getInstance().getAbsolutePath("Dropbox",
					"." + mFile.getPath()))).delete();
		}
	}

	@Override
	protected String getDialogMessage() {
		return mContext.getString(R.string.download_task_msg) + " "
				+ mFile.getName();
	}
}
