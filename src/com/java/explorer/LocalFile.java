package com.java.explorer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.android.explorer.R;

public class LocalFile extends ExplorerEntity {

	File mFile;

	public LocalFile() {
		mFile = android.os.Environment.getExternalStorageDirectory();
	}

	private LocalFile(File internalFile) {
		mFile = internalFile;
	}

	@Override
	public boolean canRead() {
		return mFile.canRead();
	}

	@Override
	public boolean canWrite() {
		return mFile.canWrite();
	}

	@Override
	public boolean createNewFile() throws IOException {
		return mFile.createNewFile();
	}

	@Override
	public boolean delete() {
		return mFile.delete();
	}

	@Override
	public boolean exists() {
		return mFile.exists();
	}

	@Override
	public String getAbsolutePath() {
		return mFile.getAbsolutePath();
	}

	@Override
	public String getName() {
		return mFile.getName();
	}

	@Override
	public String getParent() {
		return mFile.getParent();
	}

	@Override
	public ExplorerEntity getParentEntity() {
		return (ExplorerEntity) new LocalFile(mFile.getParentFile());
	}

	@Override
	public String getPath() {
		return mFile.getPath();
	}

	@Override
	public boolean isDirectory() {
		return mFile.isDirectory();
	}

	@Override
	public boolean isFile() {
		return mFile.isFile();
	}

	@Override
	public boolean isHidden() {
		return mFile.isHidden();
	}

	@Override
	public long lastModified() {
		return mFile.lastModified();
	}

	@Override
	public long length() {
		return mFile.length();
	}

	@Override
	public String[] list() {
		return mFile.list();
	}

	@Override
	public String[] list(FilenameFilter filter) {
		return mFile.list(filter);
	}

	@Override
	public ExplorerEntity[] listEntities() {
		File[] files = mFile.listFiles();
		ExplorerEntity[] entities;
		if (files != null && files.length > 0) {
			entities = new ExplorerEntity[files.length];
			int i = 0;
			for (File f : files) {
				entities[i++] = (ExplorerEntity) new LocalFile(f);
			}
			return entities;
		}
		return null;
	}

	@Override
	public ExplorerEntity[] listEntities(FileFilter filter) {
		File[] files = mFile.listFiles(filter);
		ExplorerEntity[] entities;
		if (files != null && files.length > 0) {
			entities = new ExplorerEntity[files.length];
			int i = 0;
			for (File f : files) {
				entities[i++] = (ExplorerEntity) new LocalFile(f);
			}
			return entities;
		}
		return null;
	}

	@Override
	public ExplorerEntity[] listEntities(FilenameFilter filter) {
		File[] files = mFile.listFiles(filter);
		ExplorerEntity[] entities = new ExplorerEntity[0];
		if (files != null && files.length > 0) {
			entities = new ExplorerEntity[files.length];
			int i = 0;
			for (File f : files) {
				entities[i++] = (ExplorerEntity) new LocalFile(f);
			}
		}
		return entities;
	}

	@Override
	public ExplorerEntity mksubdir(String subDirName) {
		String path = mFile.getAbsolutePath();
		if (path.endsWith("/"))
			path += subDirName;
		else
			path += "/" + subDirName;
		File newDir = new File(path);
		newDir.mkdir();
		return new LocalFile(newDir);
	}

	@Override
	public boolean renameTo(ExplorerEntity newDir, String newName) {
		String path = newDir.getPath();
		if (path.endsWith("/"))
			path += newName;
		else
			path += "/" + newName;
		return mFile.renameTo(new File(path));
	}

	@Override
	public String toString() {
		return mFile.toString();
	}

	@Override
	public ExplorerEntity copy(ExplorerEntity newDir, String newName)
			throws FileNotFoundException, IOException {
		String path = newDir.getPath();
		if (path.endsWith("/"))
			path += newName;
		else
			path += "/" + newName;
		File cloned = new File(path);
		if (cloned.exists())
			throw new IOException();
		InputStream in = new FileInputStream(mFile);
		OutputStream out = new FileOutputStream(cloned);
		// Copy the bits from instream to outstream
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		return new LocalFile(cloned);
	}

	@Override
	public void refresh() {
		this.mFile = new File(this.mFile.getAbsolutePath());
	}

	@Override
	public String getMimeType() {
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String ext = mFile.getName()
				.substring(mFile.getName().indexOf(".") + 1);
		String type = mime.getMimeTypeFromExtension(ext);
		return type;
	}

	@Override
	public Uri toUri() {
		return Uri.fromFile(mFile);
	}

	@Override
	public int getImageResource() {
		if (isDirectory())
			return R.drawable.ic_folder;
		return R.drawable.ic_document;
	}
}
