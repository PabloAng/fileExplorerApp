package com.android.explorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.android.explorer.exceptions.CantCopyFileException;
import com.android.explorer.exceptions.CantCreateFileException;
import com.android.explorer.exceptions.CantMoveFileException;
import com.android.explorer.exceptions.CantRemoveFileException;
import com.android.explorer.exceptions.CantRenameFileException;
import com.android.explorer.interfaces.FileManager;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class DropboxExplorer implements FileManager {

	private File root = null;
	private boolean showHidden = false;

	private static final String DROPBOX = "Dropbox";

	// /////////////////////////////////////////////////////////////////////////
	// Your app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// Replace this with your app key and secret assigned by Dropbox.
	// Note that this is a really insecure way to do this, and you shouldn't
	// ship code which contains your key & secret in such an obvious way.
	// Obfuscation is good.
	final static private String APP_KEY = "o9ogmd839ci03bz";
	final static private String APP_SECRET = "wqxxvm5ghtccq9y";

	// If you'd like to change the access type to the full Dropbox instead of
	// an app folder, change this value.
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

	// /////////////////////////////////////////////////////////////////////////
	// End app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// You don't need to change these, leave them alone.
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	DropboxAPI<AndroidAuthSession> mApi;

	private boolean mLoggedIn;

	public DropboxExplorer() {
		root = new File(DROPBOX);

		// We create a new AuthSession so that we can use the Dropbox API.
//		AndroidAuthSession session = buildSession();
	
//		mApi = new DropboxAPI<AndroidAuthSession>(session);

//		checkAppKeySetup();

	}

	public void showHidden(boolean show) {
		this.showHidden = show;
	}

	/**
	 * Check if there is a sd card mounted in the current device.
	 * 
	 * @return <code>true</code> if there is one; <code>false</code> otherwise.
	 */
	public static boolean isSdPresent() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/*********** FileManager Interface Implementation **************/

	@Override
	public List<File> openDirectory(File dir) {
		ArrayList<File> fList = new ArrayList<File>();

		/*
		 * if (root != null && dir.isDirectory() && dir.exists()) { File[] files
		 * = dir.listFiles(fileFilter); for (File f : files) { fList.add(f); } }
		 */
		return fList;

	}

	@Override
	public File getRootFile() {
		return root;
	}

	@Override
	public void removeFileOrDirectory(File file) throws CantRemoveFileException {
		if (file.isDirectory())
			for (File child : file.listFiles())
				removeFileOrDirectory(child);

		if (!file.delete())
			throw new CantRemoveFileException("Can't remove file: "
					+ file.getName());
	}

	@Override
	public void renameFileOrDirectory(File file, String newName)
			throws CantRenameFileException {
		File newFile = new File(file.getParentFile().getPath(), newName);

		if (newName.equals(""))
			return;

		if (!newFile.exists()) {
			if (!file.renameTo(newFile))
				throw new CantRenameFileException();
		} else
			throw new CantRenameFileException(
					"There is a file/directory with that name in the same folder.");
	}

	@Override
	public void moveFileOrDirectory(File from, File to)
			throws CantMoveFileException {
		if (from.renameTo(to))
			throw new CantMoveFileException("Can't move file: "
					+ from.getName());
	}

	@Override
	public void createDirectory(File newDir) throws CantCreateFileException {
		if (!newDir.exists()) {
			if (!newDir.mkdir())
				throw new CantCreateFileException();
		} else
			throw new CantCreateFileException(
					"File or Directory already exists");
	}

	@Override
	public void copyFileOrDirectory(File from, File to)
			throws CantCopyFileException {
		if (from.isDirectory()) {
			if (!to.exists()) {
				to.mkdirs();
			}

			String[] children = from.list();
			for (int i = 0; i < children.length; i++) {
				copyFileOrDirectory(new File(from, children[i]), new File(to,
						children[i]));
			}
		} else {

			try {
				copyFile(from, to);
			} catch (Exception e) {
				throw new CantCopyFileException();
			}
		}
	}

	/******************************************************************/

	private void copyFile(File sourceLocation, File targetLocation)
			throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(sourceLocation);
		OutputStream out = new FileOutputStream(targetLocation);

		// Copy the bits from instream to outstream
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

//	private void checkAppKeySetup() {
//		// Check if the app has set up its manifest properly.
//		Intent testIntent = new Intent(Intent.ACTION_VIEW);
//		String scheme = "db-" + APP_KEY;
//		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
//		testIntent.setData(Uri.parse(uri));
//		PackageManager pm = getPackageManager();
//		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
//			/*
//			 * showToast("URL scheme in your app's " +
//			 * "manifest is not set up correctly. You should have a " +
//			 * "com.dropbox.client2.android.AuthActivity with the " + "scheme: "
//			 * + scheme); finish();
//			 */
//
//		}
//	}
//
//	private AndroidAuthSession buildSession() {
//		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
//		AndroidAuthSession session;
//
//		String[] stored = getKeys();
//		if (stored != null) {
//			AccessTokenPair accessToken = new AccessTokenPair(stored[0],
//					stored[1]);
//			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,
//					accessToken);
//		} else {
//			session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
//		}
//
//		return session;
//	}
//
//	/**
//	 * Shows keeping the access keys returned from Trusted Authenticator in a
//	 * local store, rather than storing user name & password, and
//	 * re-authenticating each time (which is not to be done, ever).
//	 * 
//	 * @return Array of [access_key, access_secret], or null if none stored
//	 */
//	private String[] getKeys() {
//		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
//		String key = prefs.getString(ACCESS_KEY_NAME, null);
//		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
//		if (key != null && secret != null) {
//			String[] ret = new String[2];
//			ret[0] = key;
//			ret[1] = secret;
//			return ret;
//		} else {
//			return null;
//		}
//	}
//
//	/**
//	 * Shows keeping the access keys returned from Trusted Authenticator in a
//	 * local store, rather than storing user name & password, and
//	 * re-authenticating each time (which is not to be done, ever).
//	 */
//	private void storeKeys(String key, String secret) {
//		// Save the access key for later
//		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
//		Editor edit = prefs.edit();
//		edit.putString(ACCESS_KEY_NAME, key);
//		edit.putString(ACCESS_SECRET_NAME, secret);
//		edit.commit();
//	}
//
//	private void clearKeys() {
//		SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
//		Editor edit = prefs.edit();
//		edit.clear();
//		edit.commit();
//	}
//
//	private void logOut() {
//		// Remove credentials from the session
//		mApi.getSession().unlink();
//
//		// Clear our stored keys
//		clearKeys();
//		// Change UI state to display logged out version
//		setLoggedIn(false);
//	}
//
//	/**
//	 * Convenience function to change UI state based on being logged in
//	 */
//	private void setLoggedIn(boolean loggedIn) {
//		mLoggedIn = loggedIn;
//		if (loggedIn) {
//			mSubmit.setText("Unlink from Dropbox");
//			mDisplay.setVisibility(View.VISIBLE);
//		} else {
//			mSubmit.setText("Link with Dropbox");
//			mDisplay.setVisibility(View.GONE);
//			mImage.setImageDrawable(null);
//		}
//	}
}
