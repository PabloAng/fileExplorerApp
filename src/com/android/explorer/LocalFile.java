package com.android.explorer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

public class LocalFile extends ExplorerEntity {

	File mFile;

	public LocalFile(File internalFile) {
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
		if (files.length > 0) {
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
		if (files.length > 0) {
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
		ExplorerEntity[] entities;
		if (files.length > 0) {
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
	public boolean mkdir() {
		return mFile.mkdir();
	}

	@Override
	public boolean mkdirs() {
		return mFile.mkdirs();
	}

	@Override
	public boolean renameTo(String absolutePath) {
		return mFile.renameTo(new File(absolutePath));
	}

	@Override
	public String toString() {
		return mFile.toString();
	}
}
