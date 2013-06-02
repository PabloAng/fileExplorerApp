package com.android.explorer.dropbox;

import com.java.explorer.Explorer;

public class DropboxExplorer extends Explorer {

	public DropboxExplorer() {
		super();

		root = new DropboxFile();
	}

}
