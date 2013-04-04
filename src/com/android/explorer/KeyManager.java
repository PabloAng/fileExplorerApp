package com.android.explorer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class KeyManager {

	// You don't need to change these, leave them alone.
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	Context context;

	public KeyManager(Context context) {
		this.context = context;
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a
	 * local store, rather than storing user name & password, and
	 * re-authenticating each time (which is not to be done, ever).
	 * 
	 * @return Array of [access_key, access_secret], or null if none stored
	 */
	public String[] getKeys(String preferences) {
		SharedPreferences prefs = context.getSharedPreferences(preferences, 0);
		String key = prefs.getString(ACCESS_KEY_NAME, null);
		String secret = prefs.getString(ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	/**
	 * Shows keeping the access keys returned from Trusted Authenticator in a
	 * local store, rather than storing user name & password, and
	 * re-authenticating each time (which is not to be done, ever).
	 */
	public void storeKeys(String preferences, String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = context.getSharedPreferences(preferences, 0);
		Editor edit = prefs.edit();
		edit.putString(ACCESS_KEY_NAME, key);
		edit.putString(ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	public void clearKeys(String preferences) {
		SharedPreferences prefs = context.getSharedPreferences(preferences, 0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}
}
