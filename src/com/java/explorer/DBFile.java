package com.java.explorer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.android.explorer.ExternalStorage;
import com.dropbox.client2.DropboxAPI.Entry;

public class DBFile extends ExplorerEntity {

	private static final String DROPBOX_CACHE = "Dropbox";

	Entry mEntry = null;
	File mLocalFile = null;
	DropboxClient dbClient = null;

	public DBFile() {
		this.dbClient = DropboxClient.getInstance();
		this.mEntry = dbClient.getEntry(DropboxClient.ROOT);
		this.mLocalFile = ExternalStorage.getInstance().createNewDirectory(
				DBFile.DROPBOX_CACHE, ".");
		if (mEntry != null)
			ExternalStorage.getInstance().saveMetadata(DBFile.DROPBOX_CACHE,
					mEntry);
		else {
			mEntry = ExternalStorage.getInstance().getMetadata(
					DBFile.DROPBOX_CACHE, ".");
		}
	}

	private DBFile(Entry dbEntry, DropboxClient dbClient) {
		this.mEntry = dbEntry;
		this.dbClient = dbClient;
		if (mEntry.isDir) {
			this.mLocalFile = ExternalStorage.getInstance().createNewDirectory(
					DBFile.DROPBOX_CACHE, "." + mEntry.path);
			ExternalStorage.getInstance().saveMetadata(DBFile.DROPBOX_CACHE,
					mEntry);
		} else
			this.mLocalFile = ExternalStorage.getInstance().getFile(
					DBFile.DROPBOX_CACHE, "." + mEntry.path);
	}

	private void searchInExternalStorage() {
		this.mLocalFile = ExternalStorage.getInstance().getFile(
				DBFile.DROPBOX_CACHE, "." + mEntry.path);
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
		if (dbClient.deleteEntity(mEntry.path)) {
			return true;
		}
		return false;
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
		return mEntry.fileName();
	}

	@Override
	public String getParent() {
		return mEntry.parentPath();
	}

	@Override
	public ExplorerEntity getParentEntity() {
		return (ExplorerEntity) new DBFile(dbClient.getEntry(mEntry
				.parentPath()), dbClient);
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
		if (mEntry.contents == null)
			refresh();
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
		if (mEntry.contents == null)
			refresh();
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
		if (mEntry.contents == null)
			refresh();
		ExplorerEntity[] list = new ExplorerEntity[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = new DBFile(ent, dbClient);
			}
		}
		return list;
	}

	@Override
	public ExplorerEntity[] listEntities(FileFilter filter) {
		if (mEntry.contents == null)
			refresh();
		ExplorerEntity[] list = new ExplorerEntity[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = new DBFile(ent, dbClient);
			}
		}
		return list;
	}

	@Override
	public ExplorerEntity[] listEntities(FilenameFilter filter) {
		if (mEntry.contents == null)
			refresh();
		ExplorerEntity[] list = new ExplorerEntity[mEntry.contents.size()];
		int i = 0;
		for (Entry ent : mEntry.contents) {
			if (!ent.isDeleted) {
				list[i++] = new DBFile(ent, dbClient);
			}
		}
		return list;
	}

	@Override
	public ExplorerEntity mksubdir(String dirName) {
		String path = mEntry.path;
		if (path.endsWith("/"))
			path += dirName;
		else
			path += "/" + dirName;
		Entry newDir = dbClient.createDirectory(path);
		if (newDir != null)
			return new DBFile(newDir, dbClient);
		return null;
	}

	@Override
	public boolean renameTo(ExplorerEntity newDir, String newName) {
		String path = newDir.getPath();
		if (path.endsWith("/"))
			path += newName;
		else
			path += "/" + newName;
		Entry e = dbClient.moveEntity(mEntry.path, path);
		if (e == null)
			return false;
		mEntry = e;
		return true;
	}

	@Override
	public String toString() {
		return mEntry.toString();
	}

	@Override
	public ExplorerEntity copy(ExplorerEntity newDir, String newName)
			throws FileNotFoundException, IOException {
		if (mEntry.contents == null)
			refresh();
		String path = newDir.getPath();
		if (path.endsWith("/"))
			path += newName;
		else
			path += "/" + newName;
		return new DBFile(dbClient.copyEntity(mEntry.path, path), dbClient);

	}

	@Override
	public void refresh() {
		String path = mEntry.path;
		Entry entry = dbClient.getEntry(path);
		if (entry != null) {
			mEntry = entry;
			if (mEntry.isDir)
				ExternalStorage.getInstance().saveMetadata(
						DBFile.DROPBOX_CACHE, mEntry);
		}
		// if (mEntry != null)
		// ExternalStorage.getInstance().saveMetadata(DBFile.DROPBOX_CACHE,
		// mEntry);
		// else
		// mEntry = ExternalStorage.getInstance().getMetadata(
		// DBFile.DROPBOX_CACHE, path);
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
}
