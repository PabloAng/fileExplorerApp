package com.android.explorer.dropbox;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.explorer.interfaces.VirtualStorageClient;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class DropboxClient implements VirtualStorageClient {

	// private static final String TAG = "DropboxClient";

	// /////////////////////////////////////////////////////////////////////////
	// Your app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	// Replace this with your app key and secret assigned by Dropbox.
	// Note that this is a really insecure way to do this, and you shouldn't
	// ship code which contains your key & secret in such an obvious way.
	// Obfuscation is good.
	final static private String APP_KEY = "6nflv40o10mji9b";
	final static private String APP_SECRET = "dbjnpnrfipt92dt";

	// If you'd like to change the access type to the full Dropbox instead of
	// an app folder, change this value.
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

	// /////////////////////////////////////////////////////////////////////////
	// End app-specific settings. //
	// /////////////////////////////////////////////////////////////////////////

	private DropboxAPI<AndroidAuthSession> mApi;
	private Context mContext;

	// private boolean mLoggedIn = false;
	private static DropboxClient instance = null;

	final static public String ROOT = "/";
	public static final String DROPBOX_PREFERENCES = "Dropbox_prefs";

	public static DropboxClient getInstance() {
		if (instance == null)
			instance = new DropboxClient();
		return instance;
	}

	private DropboxClient() {
		// mLoggedIn = false;
	}

	// ************* VirtualStorageClient Interface *****************//

	public void startAuthentication(Context context) {
		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession(context);
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		mApi.getSession().startAuthentication(context);
		mContext = context;
	}

	public void startAuthentication(Context context, String key, String secret) {
		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession(context, key, secret);
		mApi = new DropboxAPI<AndroidAuthSession>(session);
		// mApi.getSession().startAuthentication(context);
		mContext = context;
	}

	public void finishAuthentication() {
		if (mApi != null)
			mApi.getSession().finishAuthentication();
	}

	public boolean authenticationSuccessful() {
		return mApi != null && mApi.getSession().authenticationSuccessful();
	}

	public boolean isLinked() {
		boolean conectado = false;
		ConnectivityManager conn = (ConnectivityManager) this
				.getCurrentContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] redes = conn.getAllNetworkInfo();
		for (int i = 0; i < 2; i++) {
			if (redes[i].getState() == NetworkInfo.State.CONNECTED)
				conectado = true;
		}

		return conectado && mApi != null && mApi.getSession().isLinked();
	}

	public void logOut() {
		// Remove credentials from the session
		if (mApi != null)
			mApi.getSession().unlink();
	}

	public String[] getAccessPair() {
		String[] pair = new String[2];
		TokenPair tokens = mApi.getSession().getAccessTokenPair();
		pair[0] = tokens.key;
		pair[1] = tokens.secret;
		return pair;
	}

	// ************* ************************* *****************//

	public DropboxAPI<AndroidAuthSession> getDropboxAPI() {
		return mApi;
	}

	private AndroidAuthSession buildSession(Context context) {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;
		session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
		mContext = context;
		return session;
	}

	private AndroidAuthSession buildSession(Context context, String key,
			String secret) {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		AccessTokenPair accessToken = new AccessTokenPair(key, secret);
		session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
		mContext = context;
		return session;
	}

	public Entry moveEntity(String fromPath, String toPath) {
		try {
			if (isLinked())
				return mApi.move(fromPath, toPath);
		} catch (DropboxException e) {

		}
		return null;
	}

	public Entry copyEntity(String fromPath, String toPath) {
		try {
			if (isLinked())
				return mApi.copy(fromPath, toPath);
		} catch (DropboxException e) {
		}
		return null;
	}

	public Context getCurrentContext() {
		return mContext;
	}

	// NEW INTERFACE
	public void createDirectory(DropboxFile file) {
		if (isLinked())
			(new DropboxCreateDirTask(this.getCurrentContext(), this, file))
					.execute();
		return;
	}

	public void deleteEntity(DropboxFile file) {
		if (isLinked())
			(new DropboxDeleteTask(this.getCurrentContext(), this, file))
					.execute();
		return;
	}

	public void getEntry(DropboxFile file) {

		if (isLinked())
			(new DropboxUpdateTask(this.getCurrentContext(), this, file))
					.execute();
		return;
	}

	public void moveEntity(DropboxFile from, DropboxFile to) {
		if (isLinked())
			(new DropboxRenameTask(this.getCurrentContext(), this, from, to))
					.execute();
		return;
	}

	public void copyEntity(DropboxFile from, DropboxFile to) {
		if (isLinked())
			(new DropboxCopyTask(this.getCurrentContext(), this, from, to))
					.execute();
		return;
	}

}
