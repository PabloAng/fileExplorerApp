package com.android.explorer;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.java.explorer.DropboxClient;
import com.java.explorer.EntityManager;

public class ExplorerActivity extends TabActivity {

	// constants
	private static final int MAX_TABS = 2;

	// Views
	private TabHost tabHost;
	private ImageButton addBtn;
	// List<ListView> tabsDirList;

	EntityManager currentFM = null;
	private int count;
	private KeyManager kManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_activity);
		ExternalStorage.initInstance(ExplorerActivity.this);
		count = 0;
		setViews();
		// initHomeList();
		// tabsDirList = new ArrayList<ListView>(HomeTabActivity.MAX_TABS);
		addTabMethod("SD", getResources().getDrawable(R.drawable.sdcard), 0);
		kManager = new KeyManager(ExplorerActivity.this);
		String[] keys = kManager.getKeys(DropboxClient.DROPBOX_PREFERENCES);
		if (keys != null) {
			DropboxClient.getInstance().startAuthentication(
					ExplorerActivity.this, keys[0], keys[1]);
			addTabMethod("Dropbox",
					getResources().getDrawable(R.drawable.dropbox), 1);
			ExplorerActivity.this.addBtn.setVisibility(View.GONE);
		}
		tabHost.setCurrentTab(0);
	}

	private void setViews() {
		this.tabHost = getTabHost(); // The activity TabHost

		// AddBtn
		addBtn = (ImageButton) findViewById(R.id.add_btn); // Add tab button
		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ExplorerActivity.this.openContextMenu(v);
			}
		});

		registerForContextMenu(addBtn);
	}

	/***************** CONTEXT MENU SETUP *******************/

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.equals(addBtn)) {
			// Get the info on which item was selected
			menu.setHeaderTitle("New tab");
			MenuInflater inflater = getMenuInflater();

			inflater.inflate(R.menu.add_tab_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem mItem) {
		// Retrieve the item that was clicked on
		switch (mItem.getItemId()) {
		case R.id.dropbox:
			String[] keys = kManager.getKeys(DropboxClient.DROPBOX_PREFERENCES);
			if (keys != null) {
				DropboxClient.getInstance().startAuthentication(
						ExplorerActivity.this, keys[0], keys[1]);
				addTabMethod("Dropbox",
						getResources().getDrawable(R.drawable.dropbox), 1);
				ExplorerActivity.this.addBtn.setVisibility(View.GONE);
			} else
				DropboxClient.getInstance().startAuthentication(
						ExplorerActivity.this);
			break;
		default:
			break;
		}
		return true;
	}

	/*************************************************************/

	@Override
	protected void onResume() {
		super.onResume();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (DropboxClient.getInstance().authenticationSuccessful() && count < 2) {
			try {
				// Mandatory call to complete the auth
				DropboxClient.getInstance().finishAuthentication();

				// Store it locally in our app for later use
				String[] tokens = DropboxClient.getInstance().getAccessPair();
				kManager.storeKeys(DropboxClient.DROPBOX_PREFERENCES,
						tokens[0], tokens[1]);
				addTabMethod("Dropbox",
						getResources().getDrawable(R.drawable.dropbox), 1);
			} catch (IllegalStateException e) {
				Toast.makeText(
						ExplorerActivity.this,
						"Couldn't authenticate with Dropbox:"
								+ e.getLocalizedMessage(), Toast.LENGTH_SHORT)
						.show();
				// Log.i(TAG, "Error authenticating", e);
			}
		}
	}

	/***************************************************************/

	private void addTabMethod(String indicator, Drawable icon, int val) {
		// Tab for Photos
		TabSpec tab = tabHost.newTabSpec(indicator);

		// setting Title and Icon for the Tab
		tab.setIndicator("", icon);
		Intent tabIntent;
		if (val == 0)
			tabIntent = new Intent(this, LocalExplorerTabActivity.class);
		else {
			tabIntent = new Intent(this, OnlineExplorerTabActivity.class);
			tabIntent.putExtra(OnlineExplorerTabActivity.STORAGE_CLIENT,
					OnlineExplorerTabActivity.STORAGE_CLIENT_DROPBOX);
		}
		tab.setContent(tabIntent);

		// Adding all TabSpec to TabHost
		tabHost.addTab(tab); // Adding photos tab
		tabHost.setCurrentTab(count++);

		if (count == ExplorerActivity.MAX_TABS)
			ExplorerActivity.this.addBtn.setVisibility(View.GONE);

	}

	private void deleteTabMethod() {

		if (tabHost.getTabWidget().getChildCount() <= 1)
			return;

		int position = tabHost.getCurrentTab();
		if (position == 0) {
			Toast.makeText(ExplorerActivity.this,
					"You can't close de SDCARD tab", Toast.LENGTH_SHORT).show();
			return;
		}
		ExplorerTabActivity childActivity = (ExplorerTabActivity) getCurrentActivity();
		childActivity.goHomeView();
		tabHost.getCurrentTabView().setVisibility(View.GONE);

		this.count--;
		if (position == 1)
			tabHost.setCurrentTab(0);
		else
			tabHost.setCurrentTab(1);

		ExplorerActivity.this.addBtn.setVisibility(View.VISIBLE);
		kManager.clearKeys(DropboxClient.DROPBOX_PREFERENCES);

	}

	/***************** OPTION MENU SETUP *******************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (tabHost.getCurrentTab() == 0)
			getMenuInflater().inflate(R.menu.local_tab_menu, menu);
		else
			getMenuInflater().inflate(R.menu.online_tab_menu, menu);
		return true;
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ExplorerTabActivity childActivity = (ExplorerTabActivity) getCurrentActivity();
		switch (item.getItemId()) {

		case R.id.config:

			break;
		case R.id.refresh:
			childActivity.refreshDirectory();
			break;
		case R.id.menu_logout:
			deleteTabMethod();
			break;
		case R.id.new_folder:
			childActivity.createNewDirectory();
			// createNewFolder();
			break;
		}
		return true;
	}
}
