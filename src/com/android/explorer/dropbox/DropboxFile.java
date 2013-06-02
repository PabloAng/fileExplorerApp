package com.android.explorer.dropbox;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.android.explorer.ExternalStorage;
import com.android.explorer.R;
import com.dropbox.client2.DropboxAPI.Entry;
import com.java.explorer.ExplorerEntity;

public class DropboxFile extends ExplorerEntity {

	public static final String DROPBOX_CACHE = "Dropbox";

	Entry mEntry = null;
	File mLocalFile = null;
	DropboxClient dbClient = null;

	public DropboxFile() {
		this.dbClient = DropboxClient.getInstance();
		// this.mEntry = dbClient.getEntry(DropboxClient.ROOT);

		mEntry = new Entry();
		mEntry.path = DropboxClient.ROOT;
		mEntry.isDir = true;
		mEntry.isDeleted = false;
		mEntry.modified = "";
		mEntry.bytes = 0;

		this.mLocalFile = ExternalStorage.getInstance().createNewDirectory(
				DropboxFile.DROPBOX_CACHE, ".");
		checkEntryUpdate();

	}

	private DropboxFile(Entry dbEntry, DropboxClient dbClient) {
		this.mEntry = dbEntry;
		this.dbClient = dbClient;
		if (mEntry.isDir) {
			this.mLocalFile = ExternalStorage.getInstance().createNewDirectory(
					DropboxFile.DROPBOX_CACHE, "." + mEntry.path);
			ExternalStorage.getInstance().saveMetadata(
					DropboxFile.DROPBOX_CACHE, mEntry);
		} else
			this.mLocalFile = ExternalStorage.getInstance().getFile(
					DropboxFile.DROPBOX_CACHE, "." + mEntry.path);
	}

	private void searchInExternalStorage() {
		this.mLocalFile = ExternalStorage.getInstance().getFile(
				DropboxFile.DROPBOX_CACHE, "." + mEntry.path);
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public boolean createNewFile() throws IOException {
		return false;
	}

	@Override
	public boolean delete() {
		dbClient.deleteEntity(this);
		return true;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getAbsolutePath() {
		return mEntry.path;
	}

	@Override
	public String getName() {
		return (mEntry.fileName().equals("")) ? "Home" : mEntry.fileName();
	}

	@Override
	public String getParent() {
		return mEntry.parentPath();
	}

	@Override
	public ExplorerEntity getParentEntity() {
		return (ExplorerEntity) new DropboxFile(ExternalStorage.getInstance()
				.getMetadata(DropboxFile.DROPBOX_CACHE, mEntry.parentPath()),
				dbClient);
	}

	@Override
	public String getPath() {
		return mEntry.path;
	}

	@Override
	public boolean isDirectory() {
		return mEntry.isDir;
	}

	@Override
	public boolean isFile() {
		return !mEntry.isDir;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public long lastModified() {
		return Date.parse(mEntry.modified);
	}

	@Override
	public long length() {
		return mEntry.bytes;
	}

	@Override
	public String[] list() {
		checkEntryUpdate();
		if (mEntry.contents == null)
			return new String[0];
		String[] list = new String[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = mEntry.path;
			}
		}
		return list;
	}

	@Override
	public String[] list(FilenameFilter filter) {
		checkEntryUpdate();
		if (mEntry.contents == null)
			return new String[0];
		String[] list = new String[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = mEntry.path;
			}
		}
		return list;
	}

	@Override
	public ExplorerEntity[] listEntities() {
		checkEntryUpdate();
		if (mEntry.contents == null)
			return new ExplorerEntity[0];
		ExplorerEntity[] list = new ExplorerEntity[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = new DropboxFile(ent, dbClient);
			}
		}
		return list;
	}

	@Override
	public ExplorerEntity[] listEntities(FileFilter filter) {
		checkEntryUpdate();
		if (mEntry.contents == null)
			return new ExplorerEntity[0];
		ExplorerEntity[] list = new ExplorerEntity[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = new DropboxFile(ent, dbClient);
			}
		}
		return list;
	}

	@Override
	public ExplorerEntity[] listEntities(FilenameFilter filter) {
		checkEntryUpdate();
		if (mEntry.contents == null)
			return new ExplorerEntity[0];
		ExplorerEntity[] list = new ExplorerEntity[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = new DropboxFile(ent, dbClient);
			}
		}
		return list;
	}

	@Override
	public ExplorerEntity mksubdir(String dirName) {

		checkEntryUpdate();
		Entry newDirEntry = new Entry();
		newDirEntry.path = buildPath(mEntry.path, dirName);
		DropboxFile newDir = new DropboxFile(newDirEntry, dbClient);
		dbClient.createDirectory(newDir);
		return newDir;

		/*
		 * checkEntryUpdate(); if (mEntry.contents == null) refresh(); Entry
		 * newDir = dbClient .createDirectory(buildPath(mEntry.path, dirName));
		 * if (newDir != null) return new DBFile(newDir, dbClient); return null;
		 */}

	@Override
	public boolean renameTo(ExplorerEntity newDir, String newName) {
		checkEntryUpdate();
		Entry entry = new Entry();
		entry.path = buildPath(newDir.getPath(), newName);
		dbClient.moveEntity(this, new DropboxFile(entry, dbClient));
		return true;
	}

	@Override
	public String toString() {
		return mEntry.toString();
	}

	@Override
	public ExplorerEntity copy(ExplorerEntity newDir, String newName)
			throws FileNotFoundException, IOException {
		// renameTo(newDir, newName);
		checkEntryUpdate();
		return new DropboxFile(dbClient.copyEntity(mEntry.path,
				buildPath(newName, newDir.getPath())), dbClient);

	}

	@Override
	public void refresh() {
		if (!mEntry.isDir) {
			this.mLocalFile = ExternalStorage.getInstance().getFile(
					DropboxFile.DROPBOX_CACHE, "." + mEntry.path);
		}

		dbClient.getEntry(this);

	}

	@Override
	public String getMimeType() {
		searchInExternalStorage();
		if (mLocalFile == null)
			return null;
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String ext = mLocalFile.getName().substring(
				mLocalFile.getName().indexOf(".") + 1);
		String type = mime.getMimeTypeFromExtension(ext);
		return type;
	}

	@Override
	public Uri toUri() {
		searchInExternalStorage();
		if (mLocalFile == null)
			return null;
		return Uri.fromFile(mLocalFile);
	}

	@Override
	public int getImageResource() {
		if (!isDirectory() && mLocalFile != null)
			return R.drawable.ic_document_updated;
		else if (!isDirectory())
			return R.drawable.ic_document;
		return R.drawable.ic_folder;
	}

	public boolean checkEntryUpdate() {
		Entry entry = ExternalStorage.getInstance().getMetadata(
				DropboxFile.DROPBOX_CACHE, mEntry.path);
		if (entry != null && hasChanged(entry)) {
			mEntry = entry;
			return true;
		}
		return false;
	}

	private boolean hasChanged(Entry entry) {
		if (entry.contents == null && mEntry.contents == null)
			return false;
		if ((entry.contents == null && mEntry.contents != null)
				|| (entry.contents != null && mEntry.contents == null)
				|| (entry.contents != null && mEntry.contents != null && entry.contents
						.size() != mEntry.contents.size()))
			return true;

		if (entry.contents != null && mEntry.contents != null
				&& entry.contents.size() == mEntry.contents.size()) {
			for (int i = 0; i < entry.contents.size(); i++) {
				if (!entry.contents.get(i).fileName()
						.equals(mEntry.contents.get(i).fileName()))
					return true;
			}
		}
		return false;
	}

	private String buildPath(String parentPath, String dirName) {
		if (parentPath.endsWith("/"))
			parentPath += dirName;
		else
			parentPath += "/" + dirName;
		return parentPath;
	}
}
