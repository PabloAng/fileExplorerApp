package com.java.explorer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;


public class SdCardExplorer extends Explorer {

	private boolean showHidden = true;
	private FilenameFilter fileFilter = null;

	public SdCardExplorer() {
		super();

		if (isSdPresent())
			root = new LocalFile();

		fileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				File sel = new File(dir, filename);
				// Filters based on whether the file is hidden or not
				return (sel.isFile() || sel.isDirectory())
						&& ((sel.isHidden() == showHidden) || showHidden);

			}
		};
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

	/******************************************************************/

	@Override
	public List<ExplorerEntity> openDirectory(ExplorerEntity dir) {
		ArrayList<ExplorerEntity> fList = new ArrayList<ExplorerEntity>();

		if (root != null && dir.exists() && dir.isDirectory()) {
			ExplorerEntity[] files = dir.listEntities(fileFilter);
			for (ExplorerEntity f : files) {
				fList.add(f);
			}
		}
		return fList;

	}

	@Override
	public void refreshDirectory(ExplorerEntity dir) {
		// TODO Auto-generated method stub
		
	}
}
