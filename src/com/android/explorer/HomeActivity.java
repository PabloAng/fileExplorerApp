package com.android.explorer;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.explorer.utils.Data;
import com.android.explorer.utils.Data.DataTypeEnum;
import com.demo.explorer.R;

public class HomeActivity extends Activity {

	// constants
	private static final String DATE_FORMAT = "dd/MM/yy HH:mm";
	private static final String TAG = "HomeActivity";

	// Views
	private ListView dirList;
	private LinearLayout urlBar;
	private Button goHome;

	private List<Button> urlBtns;
	private ArrayList<Data> homeList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		setViews();
		initHomeList();
		goHomeView();

	}

	private void setViews() {

		urlBar = (LinearLayout) this.findViewById(R.id.url_bar);
		dirList = (ListView) this.findViewById(R.id.dir_list);
		goHome = (Button) findViewById(R.id.btn_home); // go Home button

		urlBtns = new LinkedList<Button>();

		goHome.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goHomeView();
			}
		});

		dirList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				openData((Data) dirList.getItemAtPosition(position));
			}

		});

		this.registerForContextMenu(dirList);
	}

	private void initHomeList() {
		homeList = new ArrayList<Data>();
		if (isSdPresent()) {
			File sdcard = new File(Environment.getExternalStorageDirectory()
					+ "");
			homeList.add(new Data("mnt/sdcard", DataTypeEnum.FOLDER, new Date(
					sdcard.lastModified())));
		}
	}

	private void goHomeView() {

		if (!HomeActivity.this.homeList.isEmpty()) {

			dirList.setVisibility(View.VISIBLE);
			this.findViewById(R.id.empty).setVisibility(View.GONE);

			// Populate ListView

			dirList.setAdapter(new DirItemAdapter(this, R.layout.activity_home,
					R.id.dir_list, HomeActivity.this.homeList));
			urlBtns.clear();
			urlBar.removeAllViews();
			urlBar.addView(goHome);
		} else {
			this.findViewById(R.id.empty).setVisibility(View.VISIBLE);
			dirList.setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		if (!urlBtns.isEmpty()) {
			urlBtns.remove(urlBtns.size() - 1);
			urlBar.removeViewAt(urlBar.getChildCount() - 1);
			loadFileList();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);

		return true;
	}

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
			Data item = (Data) adapter.getItem(info.position);

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
		Data d = (Data) dirList.getAdapter().getItem(info.position);

		switch (mItem.getItemId()) {
		case R.id.menu_open:
			openData(d);
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

	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.config:

			break;
		case R.id.new_folder:

			break;
		}
		return true;
	}

	/**
	 * Adapter used to populate listItems with folders and files data
	 */
	public class DirItemAdapter extends ArrayAdapter<Data> {

		private final ArrayList<Data> data;

		public DirItemAdapter(Context context, int layoutResourceId,
				int textViewResourceId, ArrayList<Data> values) {

			super(context, layoutResourceId, textViewResourceId, values);
			this.data = values;
		}

		@Override
		// TODO Add attachment handling
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.dir_list_item, null);
			}

			Data item = data.get(position);

			if (item != null) {

				// id
				TextView id = (TextView) v.findViewById(R.id.item_id);

				// last modification
				TextView lmod = (TextView) v.findViewById(R.id.item_last_mod);

				// icon
				ImageView icon = (ImageView) v.findViewById(R.id.item_ic);

				if (id != null)
					id.setText(item.getId());

				if (lmod != null) {
					SimpleDateFormat dateformat = new SimpleDateFormat(
							HomeActivity.DATE_FORMAT);
					StringBuilder strDate = new StringBuilder(
							dateformat.format(item.getLastModification()));
					lmod.setText(strDate);
				}

				if (icon != null) {
					switch (item.getType()) {
					case FOLDER:
						icon.setImageResource(R.drawable.ic_folder);
						break;
					case FILE:
						icon.setImageResource(R.drawable.ic_document);
						break;
					default:
						break;
					}
				}

			}

			return v;

		}
	}

	private void openData(Data d) {
		switch (d.getType()) {
		case FOLDER:
			openFolder(d);
			break;
		case FILE:
			openFile(d);
			break;
		default:
			break;
		}
	}

	private void removeData(Data d) {
		switch (d.getType()) {
		case FOLDER:
			openFolder(d);
			break;
		case FILE:
			openFile(d);
			break;
		default:
			break;
		}
	}

	private void renameData(Data d) {
		switch (d.getType()) {
		case FOLDER:
			openFolder(d);
			break;
		case FILE:
			openFile(d);
			break;
		default:
			break;
		}
	}

	private void openFolder(Data d) {
		Button btn = new Button(this, null, android.R.attr.buttonStyleSmall);
		btn.setTypeface(null, Typeface.BOLD);
		btn.setText(d.getId());

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int pos = urlBtns.indexOf((Button) v);
				Iterator<Button> it = urlBtns.iterator();
				while (it.hasNext()) {
					Button btn = it.next();
					if (pos < urlBtns.indexOf((Button) btn)) {
						it.remove();
						urlBar.removeView(btn);
					}
				}
				loadFileList();
			}
		});

		urlBar.addView(btn, lp);
		urlBtns.add(btn);
		loadFileList();
	}

	private void openFile(Data d) {

	}

	private static boolean isSdPresent() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	private void loadFileList() {
		ArrayList<Data> fileList = new ArrayList<Data>();
		String strPath = "";

		if (this.urlBtns.isEmpty()) {
			goHomeView();
			return;
		}
		this.dirList.setVisibility(View.VISIBLE);
		this.findViewById(R.id.empty).setVisibility(View.GONE);
		// Get current path
		for (Button btn : this.urlBtns) {
			strPath += "/" + btn.getText().toString();
		}

		File path = new File(strPath);

		try {
			path.mkdirs();
		} catch (SecurityException e) {
			Log.e(TAG, "unable to write on the sd card ");
		}

		// Checks whether path exists
		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					// Filters based on whether the file is hidden or not
					return (sel.isFile() || sel.isDirectory())
							&& !sel.isHidden();

				}
			};

			String[] fList = path.list(filter);
			fileList = new ArrayList<Data>(fList.length);
			for (int i = 0; i < fList.length; i++) {
				// Convert into file path
				File sel = new File(path, fList[i]);

				if (sel.isDirectory()) {
					fileList.add(new Data(fList[i], DataTypeEnum.FOLDER,
							new Date(sel.lastModified())));
					Log.d("DIRECTORY", fileList.get(i).toString());
				} else {
					fileList.add(new Data(fList[i], DataTypeEnum.FILE,
							new Date(sel.lastModified())));
					Log.d("FILE", fileList.get(i).toString());
				}
			}

		} else {
			Log.e(TAG, "path does not exist");
		}

		if (!fileList.isEmpty())
			dirList.setAdapter(new DirItemAdapter(this, R.layout.activity_home,
					R.id.dir_list, fileList));
		else {
			findViewById(R.id.empty).setVisibility(View.VISIBLE);
			dirList.setVisibility(View.GONE);
		}

	}
}
