package com.android.explorer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;

import com.dropbox.client2.DropboxAPI.Entry;

public class ExternalStorage {

	Context context;
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	private static ExternalStorage instance = null;

	public static ExternalStorage initInstance(Context context) {
		if (instance == null)
			instance = new ExternalStorage(context);
		return instance;
	}

	public static ExternalStorage getInstance() {
		return instance;
	}

	private ExternalStorage(Context context) {
		this.context = context;

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	public File createNewDirectory(String internalRootName, String relativePath) {
		if (!mExternalStorageWriteable)
			return null;
		File root = context.getExternalFilesDir(null);
		File internalFile = new File(root, internalRootName + "/"
				+ relativePath);
		internalFile.mkdirs();
		if (internalFile.exists())
			return internalFile;
		return null;
	}

	public File getFile(String internalRootName, String relativePath) {
		if (!mExternalStorageAvailable)
			return null;
		File internalFile = new File(context.getExternalFilesDir(null),
				internalRootName + "/" + relativePath);
		if (internalFile.exists())
			return internalFile;
		return null;
	}

	public String getAbsolutePath(String internalRootName, String relativePath) {
		return context.getExternalFilesDir(null).getAbsolutePath() + "/"
				+ internalRootName + "/" + relativePath;

	}

	public void saveMetadata(String internalRootName, Entry entry) {
		if (!entry.isDir)
			return;
		File file = new File(context.getExternalFilesDir(null),
				internalRootName + "/" + entry.path + "/.metadata");
		FileOutputStream outputStream;

		if (file.exists()) {
			Entry e = getMetadata(internalRootName, entry.path);
			if (entry.contents == null && e != null && e.contents != null)
				return;
		}
		try {
			outputStream = new FileOutputStream(file);

			writeEntry(outputStream, entry);
			if (entry.contents != null) {
				outputStream.write(("+++\n").getBytes());
				for (Entry e : entry.contents) {
					writeEntry(outputStream, e);
				}
			}
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeEntry(FileOutputStream fos, Entry entry)
			throws IOException {
		fos.write((entry.size + "\n").getBytes());
		fos.write((entry.rev + "\n").getBytes());
		fos.write((Boolean.toString(entry.thumbExists) + "\n").getBytes());
		fos.write((Long.toString(entry.bytes) + "\n").getBytes());
		fos.write((entry.modified + "\n").getBytes());
		fos.write((entry.clientMtime + "\n").getBytes());
		fos.write((entry.path + "\n").getBytes());
		fos.write((Boolean.toString(entry.isDir) + "\n").getBytes());
		fos.write((entry.root + "\n").getBytes());
	}

	public Entry getMetadata(String internalRootName, String entry) {
		Entry ent = null;
		List<Entry> listEnt = new ArrayList<Entry>();
		File file = new File(context.getExternalFilesDir(null),
				internalRootName + "/" + entry + "/.metadata");
		InputStream inputStream;
		if (!file.exists())
			return ent;
		try {

			inputStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String[] lines = new String[10];
			int i = 0;
			while ((lines[i++] = bufferedReader.readLine()) != null && i < 10)
				;
			ent = new Entry();
			initEntry(ent, lines);
			i = 0;
			while ((lines[i++] = bufferedReader.readLine()) != null && i < 10) {
				if (i == 9) {
					Entry e = new Entry();
					initEntry(e, lines);
					listEnt.add(e);
					i = 0;
				}
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		if (!listEnt.isEmpty())
			ent.contents = listEnt;
		return ent;
	}

	private void initEntry(Entry entry, String[] lines) {
		entry.size = lines[0];
		entry.rev = lines[1];
		entry.thumbExists = Boolean.parseBoolean(lines[2]);
		entry.bytes = Long.parseLong(lines[3]);
		entry.modified = lines[4];
		entry.clientMtime = lines[5];
		entry.path = lines[6];
		entry.isDir = Boolean.parseBoolean(lines[7]);
		entry.root = lines[8];
		entry.contents = null;
	}
}
