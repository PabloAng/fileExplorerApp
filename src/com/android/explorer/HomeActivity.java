package com.android.explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.explorer.exceptions.CantCreateFileException;
import com.android.explorer.exceptions.CantRemoveFileException;
import com.android.explorer.exceptions.CantRenameFileException;
import com.android.explorer.interfaces.FileManager;
import com.demo.explorer.R;

public class HomeActivity extends Activity {

	// constants
	private static final String HOME = "INTERNAL_HOME_PATH_FOLDER";
	// private static final String TAG = "HomeActivity";

	// Views
	private ListView dirList = null;
	private LinearLayout urlBar = null;
	private Button goHome = null;

	// Rename textedit
	private UrlEditText nameField = null;

	private List<UrlButton> urlBtns = null;
	private List<File> homeList = null;
	private List<File> pendingRemovals = null;

	Map<String, FileManager> fManagers = null;
	FileManager currentFM = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		pendingRemovals = new ArrayList<File>();
		fManagers = new HashMap<String, FileManager>();
		fManagers.put("sdcard", new SdCardExplorer());
		urlBtns = new LinkedList<UrlButton>();
		setViews();
		initHomeList();
		goHomeView();

	}

	@Override
	public void onBackPressed() {
		if (!urlBtns.isEmpty()) {
			urlBtns.remove(urlBtns.size() - 1);
			urlBar.removeViewAt(urlBar.getChildCount() - 1);
			if (!urlBtns.isEmpty())
				loadFileList(urlBtns.get(urlBtns.size() - 1).getFile());
			else
				goHomeView();
		} else {
			super.onBackPressed();
		}
	}

	/***************** OPTION MENU SETUP *******************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);

		return true;
	}

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!getCurrentDirectory().exists())
			return false;
		switch (item.getItemId()) {

		case R.id.config:

			break;
		case R.id.new_folder:
			createNewFolder();
			break;
		}
		return true;
	}

	/***************** CONTEXT MENU SETUP *******************/
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.equals(dirList)) {
			// Get the info on which item was selected
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

			// Get the Adapter behind your ListView
			Adapter adapter = ((ListView) v).getAdapter();

			// Retrieve the item that was clicked on
			File item = (File) adapter.getItem(info.position);
			menu.setHeaderTitle(item.toString());
			MenuInflater inflater = getMenuInflater();

			inflater.inflate(R.menu.dir_item_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem mItem) {
		// Get the correct item in onContextItemSelected()
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) mItem
				.getMenuInfo();
		// Retrieve the item that was clicked on
		File d = (File) dirList.getAdapter().getItem(info.position);

		switch (mItem.getItemId()) {
		case R.id.menu_open:
			openData(d);
			break;
		case R.id.menu_copy:
			copyData(d);
			break;
		case R.id.menu_cut:
			cutData(d);
			break;
		case R.id.menu_remove:
			removeData(d);
			break;
		case R.id.menu_rename:
			renameData(d);
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * Adapter used to populate listItems with folders and files data
	 */
	public class DirItemAdapter extends ArrayAdapter<File> {

		private final List<File> data;

		public DirItemAdapter(Context context, int layoutResourceId,
				int textViewResourceId, List<File> values) {

			super(context, layoutResourceId, textViewResourceId, values);
			this.data = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.dir_list_item, null);
			}

			File item = data.get(position);

			if (item != null) {

				// id
				TextView id = (TextView) v.findViewById(R.id.item_id);

				// weight
				TextView weight = (TextView) v.findViewById(R.id.item_weight);

				// last modification
				TextView lmod = (TextView) v.findViewById(R.id.item_last_mod);

				// icon
				ImageView icon = (ImageView) v.findViewById(R.id.item_ic);

				id.setText(item.getName());

				lmod.setText(DateFormatter.formatDate(item.lastModified()));
				if (item.isDirectory()) {
					icon.setImageResource(R.drawable.ic_folder);
					weight.setVisibility(View.GONE);
				} else if (item.isFile()) {
					icon.setImageResource(R.drawable.ic_document);
					weight.setText(FileSizeFormatter.formatFileSize(item
							.length()));
				}

			}

			return v;

		}
	}

	/******************* PRIVATE METHODS ********************/

	/**
	 * Initialize the homeList
	 */
	private void initHomeList() {
		homeList = new ArrayList<File>();
		for (String key : fManagers.keySet()) {
			homeList.add(fManagers.get(key).getRootFile());
		}
	}

	/**
	 * Initialize all views of the activity
	 */
	private void setViews() {

		urlBar = (LinearLayout) this.findViewById(R.id.url_bar);
		dirList = (ListView) this.findViewById(R.id.dir_list);
		dirList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		goHome = new UrlButton(this, R.drawable.black_button_full, getText(
				R.string.default_dir_url).toString()
		/* + "  /" */, new File(HomeActivity.HOME)); // go Home button

		// Set goHome listener: show home view on Click
		goHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				goHomeView();
			}
		});

		// Set dirList item listener: open File
		dirList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				openData((File) dirList.getItemAtPosition(position));
			}

		});

		this.registerForContextMenu(dirList);
	}

	/**
	 * Reset listview to Home
	 */
	private void goHomeView() {

		if (!homeList.isEmpty()) {
			populateListView(dirList, R.layout.activity_home, R.id.dir_list,
					homeList);
			findViewById(R.id.empty).setVisibility(View.GONE);
			urlBtns.clear();
			urlBar.removeAllViews();

			goHome.setBackgroundResource(R.drawable.black_button_full);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			urlBar.addView(goHome, lp);

		} else {
			findViewById(R.id.empty).setVisibility(View.VISIBLE);
			findViewById(R.id.dir_list).setVisibility(View.GONE);
		}
	}

	/**
	 * Open the File, and show the result.
	 * 
	 * @param d
	 *            File object. In case of directory, opens the directory and
	 *            update the listview. Nothing if it is a file.
	 */
	private void openData(File d) {
		if (d.isDirectory())
			openFolder(d);
		else if (d.isFile())
			openFile(d);
	}

	/**
	 * Confirm if the user want to delete file throw prompt
	 * 
	 * @param d
	 *            File to be removed.
	 */
	private void removeData(File d) {
		pendingRemovals.clear();
		pendingRemovals.add(d);
		AlertDialog.Builder removeAlert = new AlertDialog.Builder(this);

		removeAlert.setTitle(R.string.title_remove_alert);
		removeAlert.setMessage(R.string.remove_msg);

		removeAlert.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						deletePendingFiles();
					}
				});
		removeAlert.setNegativeButton(R.string.btn_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						pendingRemovals.clear();
					}
				});
		removeAlert.show();
	}

	/**
	 * Delete pending files that were confirmed to be removed by the user.
	 */
	private void deletePendingFiles() {
		for (File d : pendingRemovals) {
			File parent = d.getParentFile();
			try {
				this.currentFM.removeFileOrDirectory(d);
				Toast.makeText(this,
						d.getName() + " " + getText(R.string.removed_msg),
						Toast.LENGTH_SHORT).show();
				loadFileList(parent);
			} catch (CantRemoveFileException e) {
				Toast.makeText(this,
						d.getName() + " " + getText(R.string.not_removed_msg),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Ask the user to enter the new name, and try to change it.
	 * 
	 * @param d
	 *            File to be renamed.
	 */
	private void renameData(File d) {
		AlertDialog.Builder editalert = new AlertDialog.Builder(this);

		editalert.setTitle(R.string.title_rename_alert);
		editalert.setMessage(R.string.rename_msg);

		nameField = new UrlEditText(this, d);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		nameField.setLayoutParams(lp);
		nameField.setSingleLine();
		nameField.setText(d.getName());
		nameField.selectAll();
		editalert.setView(nameField);

		editalert.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							HomeActivity.this.currentFM.renameFileOrDirectory(
									nameField.getFile(), nameField.getText()
											.toString());
							loadFileList(nameField.getFile().getParentFile());
						} catch (CantRenameFileException e) {
							Toast.makeText(HomeActivity.this,
									R.string.not_renamed_msg,
									Toast.LENGTH_SHORT).show();

						}
					}
				});
		editalert.setNegativeButton(R.string.btn_cancel, null);

		editalert.show();
	}

	private void openFolder(File d) {
		UrlButton btn = new UrlButton(this, R.drawable.black_button_right,
				d.getName()/* + "  /" */, d);

		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((UrlButton) v)
						.setBackgroundResource(R.drawable.black_button_right);
				int pos = urlBtns.indexOf((UrlButton) v);
				Iterator<UrlButton> it = urlBtns.iterator();
				while (it.hasNext()) {
					Button btn = it.next();
					if (pos < urlBtns.indexOf((Button) btn)) {
						it.remove();
						urlBar.removeView(btn);
					}
				}
				loadFileList(((UrlButton) v).getFile());
			}
		});

		goHome.setBackgroundResource(R.drawable.black_button_left);
		
		loadFileList(d);
		if (!urlBtns.isEmpty())
			urlBtns.get(urlBtns.size() - 1).setBackgroundResource(
					R.drawable.black_button_middle);
		urlBar.addView(btn, lp);
		urlBtns.add(btn);

		new Handler().postDelayed(new Runnable() {
			public void run() {
				((HorizontalScrollView) findViewById(R.id.url_scroll_bar))
						.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		}, 100L);

	}

	private void openFile(File d) {
		/**
		 * TODO: Find out who to open files.
		 */
	}

	/**
	 * Create a new folder in the last directory open.
	 */
	private void createNewFolder() {
		AlertDialog.Builder newAlert = new AlertDialog.Builder(this);

		newAlert.setTitle(R.string.title_new_folder_alert);
		newAlert.setMessage(R.string.new_folder_name_msg);

		nameField = new UrlEditText(this, getCurrentDirectory());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		nameField.setLayoutParams(lp);
		nameField.setSingleLine();
		nameField.findFocus();
		newAlert.setView(nameField);

		newAlert.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (!nameField.getText().toString().equals("")) {
							try {
								File newFile = new File(nameField.getFile()
										.getPath(), nameField.getText()
										.toString());
								HomeActivity.this.currentFM
										.createDirectory(newFile);
								openFolder(newFile);
							} catch (CantCreateFileException e) {
								if (e.getMessage().contains("exists"))
									Toast.makeText(HomeActivity.this,
											R.string.new_folder_exists_msg,
											Toast.LENGTH_SHORT).show();
							}
						}

					}
				});
		newAlert.setNegativeButton(R.string.btn_cancel, null);

		newAlert.show();
	}

	/**
	 * Update the current listview of files.
	 * 
	 * @param file
	 *            file root of the new listview.
	 */
	private void loadFileList(File file) {
		List<File> fList;

		if (getCurrentDirectory().getName().equals(HomeActivity.HOME)) {
			currentFM = fManagers.get(file.getName());
		}

		fList = currentFM.openDirectory(file);

		if (!fList.isEmpty()) {
			Collections.sort(fList, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					if ((f1.isDirectory() && f2.isDirectory())
							|| (f1.isFile() && f2.isFile()))
						return f1.getName().compareToIgnoreCase(f2.getName());
					else if (f1.isDirectory() && f2.isFile())
						return -1;
					return 1;
				}
			});
			populateListView(dirList, R.layout.activity_home, R.id.dir_list,
					fList);
			findViewById(R.id.empty).setVisibility(View.GONE);
		} else {
			findViewById(R.id.empty).setVisibility(View.VISIBLE);
			dirList.setVisibility(View.GONE);
		}

	}

	private void populateListView(ListView view, int layoutResourceId,
			int textViewResourceId, List<File> list) {

		view.setVisibility(View.VISIBLE);
		view.setAdapter(new DirItemAdapter(this, layoutResourceId,
				textViewResourceId, list));

	}

	private File getCurrentDirectory() {
		if (!urlBtns.isEmpty())
			return urlBtns.get(urlBtns.size() - 1).getFile();
		return new File(HOME, HOME);
	}

	private void cutData(File d) {
		// TODO Auto-generated method stub

	}

	private void copyData(File d) {
		// TODO Auto-generated method stub

	}

}
