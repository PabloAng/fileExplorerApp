package com.android.explorer.interfaces;

import java.io.File;
import java.util.List;

import com.android.explorer.exceptions.CantCopyFileException;
import com.android.explorer.exceptions.CantCreateFileException;
import com.android.explorer.exceptions.CantMoveFileException;
import com.android.explorer.exceptions.CantRemoveFileException;
import com.android.explorer.exceptions.CantRenameFileException;

public interface FileManager {

	public List<File> openDirectory(File dir);

	public File getRootFile();

	/**
	 * Delete the File and its children recursively. Return the result of the
	 * operation.
	 * 
	 * @param file
	 *            file or directory to be removed recursively.
	 * @throws CantRemoveFileException
	 *             throws this exception if file/directory wasn't completely
	 *             removed
	 * 
	 */
	public void removeFileOrDirectory(File file) throws CantRemoveFileException;

	/**
	 * Rename the file or Directory
	 * 
	 * @param file
	 *            file to be renamed.
	 * @param newName
	 *            new name of the file
	 * @throws CantRenameFileException
	 */
	public void renameFileOrDirectory(File file, String newName)
			throws CantRenameFileException;

	/**
	 * Move the File and its children recursively.
	 * 
	 * @param from
	 *            origin.
	 * @param to
	 *            destiny.
	 * @throws CantMoveFileException
	 *             throws this exception if file/directory wasn't moved.
	 * 
	 */
	public void moveFileOrDirectory(File from, File to)
			throws CantMoveFileException;

	/**
	 * Copy the File and its children recursively.
	 * 
	 * @param from
	 *            origin.
	 * @param to
	 *            destiny.
	 * @throws CantMoveFileException
	 *             throws this exception if file/directory wasn't moved.
	 * 
	 */
	public void copyFileOrDirectory(File from, File to)
			throws CantCopyFileException;

	/**
	 * Create a new folder.
	 * 
	 * @param file
	 *            directory to be create.
	 * @throws CantCreateFileException
	 *             throws this exception if directory wasn't created
	 * 
	 */
	public void createDirectory(File newDir) throws CantCreateFileException;

}
