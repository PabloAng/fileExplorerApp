package com.android.explorer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.explorer.exceptions.CantOpenFileException;
import com.android.explorer.exceptions.NoExplorerEntityVincualtionException;
import com.java.explorer.DateFormatter;
import com.java.explorer.Explorer;
import com.java.explorer.ExplorerEntity;
import com.java.explorer.FileSizeFormatter;

public abstract class ExplorerView {

	private Explorer mExplorer = null;
	private Context mContext = null;
	private ExplorerEntity currentDir = null;

	private DirItemAdapter mExplorerAdapter = null;

	// Views
	private ListView mExplorerView = null;
	private TextView mEmptyList = null;
	private Map<View, ExplorerEntity> mViewUrl = null;

	public ExplorerView(Context context, ListView explorerListView,
			TextView emptyListView) {
		mContext = context;
		mExplorerView = explorerListView;
		mEmptyList = emptyListView;

		mViewUrl = new HashMap<View, ExplorerEntity>();
		mExplorerAdapter = new DirItemAdapter(mContext, R.layout.explorer,
				R.id.dir_list, null);
		mExplorerView.setAdapter(mExplorerAdapter);
	}

	/**
	 * Given a <code>View</code>, it returns the <code>ExplorerEntity</code>
	 * associated with that <code>View</code>.
	 * 
	 * @param view
	 *            <code>View</code>.
	 * @return <code>ExplorerEntity</code> that is associated with the
	 *         <code> View </code> argument.
	 * @throws NoExplorerEntityVincualtionException
	 */
	public ExplorerEntity getVinculatedExplorerEntity(View view)
			throws NoExplorerEntityVincualtionException {
		ExplorerEntity result = mViewUrl.get(view);
		if (result != null)
			return result;
		throw new NoExplorerEntityVincualtionException(
				"The view has not vinculated ExplorerEntity.");
	}

	/**
	 * Associates a <code>View</code> with a <code>ExplorerEntity</code>.
	 * 
	 * @param view
	 *            <code>View</code> to be associated with the
	 *            <code>ExplorerEntity</code>.
	 * @param entity
	 *            <code>ExplorerEntity</code> to be associated with the
	 *            <code>View</code>.
	 */
	public void registerView(View view, ExplorerEntity entity) {
		mViewUrl.put(view, entity);
	}

	/**
	 * Disassociates a <code>View</code>.
	 * 
	 * @param view
	 *            <code>View</code> to be disassociated.
	 */
	public void unregisterView(View view) {
		mViewUrl.remove(view);
	}

	/**
	 * Adapter used to populate listItems with folders and files data
	 */
	private class DirItemAdapter extends ArrayAdapter<ExplorerEntity> {

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
				LayoutInflater vi = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
				icon.setImageResource(item.getImageResource());
				if (item.isDirectory()) {
					weight.setVisibility(View.GONE);
				} else if (item.isFile()) {

					Bitmap bitmap = null;
					try {
						bitmap = getThumbnail(mContext.getContentResolver(),
								item.getAbsolutePath());
					} catch (Exception e) {

					}
					if (bitmap != null)
						icon.setImageBitmap(bitmap);

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

	/**
	 * Refresh the Explorer's view. Given an <code>ExplorerEntity</code>
	 * directory, it loads the files of that directory in the Explorer's view,
	 * if the directory is <code>null</code>, it automatically refresh the
	 * current directory. Otherwise, if the entity is a file it <code>throws
	 * CantOpenFileException</code>.
	 * 
	 * @param entity
	 *            new directory to display in the explorer's view.
	 * @throws CantOpenFileException
	 */
	public void refreshExplorerView(ExplorerEntity entity)
			throws CantOpenFileException {
		List<ExplorerEntity> fList;
		currentDir = (entity == null) ? getCurrentDirectory() : entity;

		fList = mExplorer.openDirectory(currentDir);
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
		populateListView(fList);
	}

	/******************* PRIVATE METHODS ********************/
	private void populateListView(List<ExplorerEntity> entities) {

		mExplorerAdapter.clear();
		for (ExplorerEntity entity : entities)
			mExplorerAdapter.add(entity);

		if (!entities.isEmpty()) {
			mExplorerView.setVisibility(View.VISIBLE);
			mEmptyList.setVisibility(View.GONE);
		} else {
			mEmptyList.setVisibility(View.VISIBLE);
			mExplorerView.setVisibility(View.GONE);
		}

	}

	private ExplorerEntity getCurrentDirectory() {
		return this.currentDir;
	}

}
