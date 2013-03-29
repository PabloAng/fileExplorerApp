package com.android.explorer;

import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

public abstract class ExplorerEntity {

	public abstract boolean canRead();

	public abstract boolean canWrite();

	public abstract boolean createNewFile() throws IOException;

	public abstract boolean exists();

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

	public abstract boolean mkdir();

	public abstract boolean mkdirs();

	public abstract boolean renameTo(String absolutePath);

	public abstract String toString();
}
