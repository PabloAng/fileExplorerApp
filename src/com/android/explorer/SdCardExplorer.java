package com.android.explorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.android.explorer.exceptions.CantCopyFileException;
import com.android.explorer.exceptions.CantCreateFileException;
import com.android.explorer.exceptions.CantMoveFileException;
import com.android.explorer.exceptions.CantRemoveFileException;
import com.android.explorer.exceptions.CantRenameFileException;
import com.android.explorer.interfaces.FileManager;

public class SdCardExplorer implements FileManager {

	private File root = null;
	private boolean showHidden = false;
	private FilenameFilter fileFilter = null;

	public SdCardExplorer() {
		if (isSdPresent())
			root = android.os.Environment.getExternalStorageDirectory();

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

	/*********** FileManager Interface Implementation **************/

	@Override
	public List<File> openDirectory(File dir) {
		ArrayList<File> fList = new ArrayList<File>();

		if (root != null && dir.isDirectory() && dir.exists()) {
			File[] files = dir.listFiles(fileFilter);
			for (File f : files) {
				fList.add(f);
			}
		}
		return fList;

	}

	@Override
	public File getRootFile() {
		return root;
	}

	@Override
	public void removeFileOrDirectory(File file) throws CantRemoveFileException {
		if (file.isDirectory())
			for (File child : file.listFiles())
				removeFileOrDirectory(child);

		if (!file.delete())
			throw new CantRemoveFileException("Can't remove file: "
					+ file.getName());
	}

	@Override
	public void renameFileOrDirectory(File file, String newName)
			throws CantRenameFileException {
		File newFile = new File(file.getParentFile().getPath(), newName);

		if (newName.equals(""))
			return;

		if (!newFile.exists()) {
			if (!file.renameTo(newFile))
				throw new CantRenameFileException();
		} else
			throw new CantRenameFileException(
					"There is a file/directory with that name in the same folder.");
	}

	@Override
	public void moveFileOrDirectory(File from, File to)
			throws CantMoveFileException {
		if (from.renameTo(to))
			throw new CantMoveFileException("Can't move file: "
					+ from.getName());
	}

	@Override
	public void createDirectory(File newDir) throws CantCreateFileException {
		if (!newDir.exists()) {
			if (!newDir.mkdir())
				throw new CantCreateFileException();
		} else
			throw new CantCreateFileException(
					"File or Directory already exists");
	}

	@Override
	public void copyFileOrDirectory(File from, File to)
			throws CantCopyFileException {
		if (from.isDirectory()) {
			if (!to.exists()) {
				to.mkdirs();
			}

			String[] children = from.list();
			for (int i = 0; i < children.length; i++) {
				copyFileOrDirectory(new File(from, children[i]), new File(to,
						children[i]));
			}
		} else {

			try {
				copyFile(from, to);
			} catch (Exception e) {
				throw new CantCopyFileException();
			}
		}
	}

	/******************************************************************/

	private void copyFile(File sourceLocation, File targetLocation)
			throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(sourceLocation);
		OutputStream out = new FileOutputStream(targetLocation);

		// Copy the bits from instream to outstream
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

}
