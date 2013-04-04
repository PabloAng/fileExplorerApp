package com.android.explorer.interfaces;

import android.content.Context;

public interface VirtualStorageClient {

	public void startAuthentication(Context context);

	public void startAuthentication(Context context, String key, String secret);

	public void finishAuthentication();

	public boolean authenticationSuccessful();

	public boolean isLinked();

	public String[] getAccessPair();

	public void logOut();

}
