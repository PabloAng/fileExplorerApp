package com.java.explorer;

import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import android.net.Uri;

public abstract class ExplorerEntity {

	public abstract boolean canRead();

	public abstract boolean canWrite();

	public abstract boolean createNewFile() throws IOException;

	public abstract boolean delete();

	public abstract boolean exists();

	public abstract void refresh();

	public abstract String getAbsolutePath();

	public abstract String getName();

	public abstract String getParent();

	public abstract ExplorerEntity getParentEntity();

	public abstract String getPath();

	public abstract boolean isDirectory();

	public abstract boolean isFile();

	public abstract boolean isHidden();

	public abstract long lastModified();

	public abstract long length();

	public abstract String[] list();

	public abstract String[] list(FilenameFilter filter);

	public abstract ExplorerEntity[] listEntities();

	public abstract ExplorerEntity[] listEntities(FileFilter filter);

	public abstract ExplorerEntity[] listEntities(FilenameFilter filter);

	public abstract ExplorerEntity mksubdir(String subDirName);

	public abstract boolean renameTo(ExplorerEntity newDir, String newName);

	public abstract ExplorerEntity copy(ExplorerEntity newDir, String newName)
			throws FileNotFoundException, IOException;

	public abstract String toString();

	public abstract String getMimeType();

	public abstract Uri toUri();

	public abstract int getImageResource();
}
