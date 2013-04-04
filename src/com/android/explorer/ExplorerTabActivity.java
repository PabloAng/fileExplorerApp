package com.android.explorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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
import com.java.explorer.DateFormatter;
import com.java.explorer.EntityManager;
import com.java.explorer.ExplorerEntity;
import com.java.explorer.FileSizeFormatter;
import com.java.explorer.UrlButton;
import com.java.explorer.UrlEditText;

public abstract class ExplorerTabActivity extends Activity {

	// constants
	public static final String HOME = "INTERNAL_HOME_PATH_FOLDER";
	// private static final String TAG = "ExplorerTabActivity";
	private String homeName = null;

	// Views
	private ListView dirList = null;
	private LinearLayout urlBar = null;
	private Button goHome = null;

	// Rename textedit
	private UrlEditText nameField = null;

	private List<UrlButton> urlBtns = null;
	// private List<ExplorerEntity> homeList = null;
	private List<ExplorerEntity> pendingRemovals = null;

	// Map<String, EntityManager> fManagers = null;
	EntityManager fManager = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		homeName = "Home";
		pendingRemovals = new ArrayList<ExplorerEntity>();
		urlBtns = new LinkedList<UrlButton>();

	}

	@Override
	public void onBackPressed() {
		if (!urlBtns.isEmpty()) {
			urlBtns.remove(urlBtns.size() - 1);
			urlBar.removeViewAt(urlBar.getChildCount() - 1);
			if (!urlBtns.isEmpty()) {
				urlBtns.get(urlBtns.size() - 1).setBackgroundResource(
						R.drawable.black_button_right);
				loadFileList(urlBtns.get(urlBtns.size() - 1).getEntity());
			} else
				goHomeView();
		} else {
			super.onBackPressed();
		}
	}

	public abstract void downloadFile(ExplorerEntity entity);

	// /***************** OPTION MENU SETUP *******************/
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.local_tab_menu, menu);
	//
	// return true;
	// }
	//
	// // This method is called once the menu is selected
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (!getCurrentDirectory().exists())
	// return false;
	// switch (item.getItemId()) {
	//
	// case R.id.config:
	//
	// break;
	// case R.id.new_folder:
	// createNewFolder();
	// break;
	// }
	// return true;
	// }

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
			ExplorerEntity item = (ExplorerEntity) adapter
					.getItem(info.position);
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
		ExplorerEntity d = (ExplorerEntity) dirList.getAdapter().getItem(
				info.position);

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
	public class DirItemAdapter extends ArrayAdapter<ExplorerEntity> {

		private final List<ExplorerEntity> data;

		public DirItemAdapter(Context context, int layoutResourceId,
				int textViewResourceId, List<ExplorerEntity> values) {

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

			ExplorerEntity item = data.get(position);

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

					Bitmap bitmap = null;
					try {
						bitmap = getThumbnail(getContentResolver(),
								item.getAbsolutePath());
					} catch (Exception e) {

					}
					if (bitmap != null)
						icon.setImageBitmap(bitmap);
					else {
						icon.setImageResource(R.drawable.ic_document);
					}
					String length = FileSizeFormatter.formatFileSize(item
							.length());
					weight.setText(length);

				}

			}

			return v;

		}

		public Bitmap getThumbnail(ContentResolver cr, String path)
				throws Exception {

			Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					new String[] { ImageColumns._ID,
							MediaStore.Images.Media.DATA },
					MediaStore.MediaColumns.DATA + "=?", new String[] { path },
					null);
			if (ca != null && ca.moveToFirst()) {
				int id = ca.getInt(ca
						.getColumnIndex(MediaStore.MediaColumns._ID));
				ca.close();
				Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
						MediaStore.Images.Thumbnails.MICRO_KIND, null);
				Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, (int) 32,
						(int) 32, true);
				bm.recycle();
				bm = scaledBitmap;
				return bm;
			}

			ca.close();
			return null;

		}
	}

	/******************* PRIVATE METHODS ********************/

	/**
	 * Initialize the homeList
	 */
	/*
	 * private void initHomeList() { homeList = new ArrayList<ExplorerEntity>();
	 * homeList.add(fManager.getRootDirectory()); for (String key :
	 * fManagers.keySet()) {
	 * homeList.add(fManagers.get(key).getRootDirectory()); } }
	 */
	/**
	 * Initialize all views of the activity
	 */
	protected void setViews() {

		urlBar = (LinearLayout) this.findViewById(R.id.url_bar);
		dirList = (ListView) this.findViewById(R.id.dir_list);
		dirList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		goHome = new UrlButton(this, R.drawable.black_button_full, homeName,
				fManager.getRootDirectory()); // go Home button

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
				openData((ExplorerEntity) dirList.getItemAtPosition(position));
			}

		});

		this.registerForContextMenu(dirList);
	}

	/**
	 * Reset listview to Home
	 */
	protected void goHomeView() {
		loadFileList(fManager.getRootDirectory());

		urlBtns.clear();
		urlBar.removeAllViews();

		goHome.setBackgroundResource(R.drawable.black_button_full);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		urlBar.addView(goHome, lp);

	}

	/**
	 * Open the File, and show the result.
	 * 
	 * @param d
	 *            File object. In case of directory, opens the directory and
	 *            update the listview. Nothing if it is a file.
	 */
	protected void openData(ExplorerEntity d) {
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
	protected void removeData(ExplorerEntity d) {
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
		for (ExplorerEntity d : pendingRemovals) {
			ExplorerEntity parent = d.getParentEntity();
			try {
				this.fManager.removeEntity(d);
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
	protected void renameData(ExplorerEntity d) {
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
							ExplorerTabActivity.this.fManager.renameEntity(
									nameField.getEntity(), nameField.getText()
											.toString());
							loadFileList(nameField.getEntity()
									.getParentEntity());
						} catch (CantRenameFileException e) {
							Toast.makeText(ExplorerTabActivity.this,
									R.string.not_renamed_msg,
									Toast.LENGTH_SHORT).show();

						}
					}
				});
		editalert.setNegativeButton(R.string.btn_cancel, null);

		editalert.show();
	}

	private void openFolder(ExplorerEntity d) {
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
				loadFileList(((UrlButton) v).getEntity());
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

	protected void openFile(ExplorerEntity entity) {
		/**
		 * TODO: Find out who to open files.
		 */
		Uri data = entity.toUri();
		String type = entity.getMimeType();

		if (data != null)
			try {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(data, type);
				startActivity(intent);
			} catch (Exception e) {
			}
		else {
			this.downloadFile(entity);
		}
	}

	public void refreshDirectory() {
		fManager.refreshDirectory(getCurrentDirectory());
		loadFileList(getCurrentDirectory());
	}

	/**
	 * Create a new folder in the last directory open.
	 */
	public void createNewDirectory() {
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
								openFolder(ExplorerTabActivity.this.fManager
										.createDirectory(nameField.getEntity(),
												nameField.getText().toString()));
							} catch (CantCreateFileException e) {
								if (e.getMessage().contains("exists"))
									Toast.makeText(ExplorerTabActivity.this,
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
	private void loadFileList(ExplorerEntity file) {
		List<ExplorerEntity> fList;

		fList = fManager.openDirectory(file);

		if (!fList.isEmpty()) {
			Collections.sort(fList, new Comparator<ExplorerEntity>() {
				@Override
				public int compare(ExplorerEntity f1, ExplorerEntity f2) {
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
			int textViewResourceId, List<ExplorerEntity> list) {

		view.setVisibility(View.VISIBLE);
		view.setAdapter(new DirItemAdapter(this, layoutResourceId,
				textViewResourceId, list));

	}

	private ExplorerEntity getCurrentDirectory() {
		if (!urlBtns.isEmpty())
			return urlBtns.get(urlBtns.size() - 1).getEntity();
		return fManager.getRootDirectory();
	}

	private void cutData(ExplorerEntity d) {
		// TODO Auto-generated method stub

	}

	private void copyData(ExplorerEntity d) {
		// TODO Auto-generated method stub

	}

}
