package com.android.explorer.interfaces;

import java.util.List;

import com.android.explorer.ExplorerEntity;
import com.android.explorer.exceptions.CantCopyFileException;
import com.android.explorer.exceptions.CantCreateFileException;
import com.android.explorer.exceptions.CantMoveFileException;
import com.android.explorer.exceptions.CantRemoveFileException;
import com.android.explorer.exceptions.CantRenameFileException;

public interface EntityManager {

	public List<ExplorerEntity> openDirectory(ExplorerEntity dir);

	public ExplorerEntity getRootDirectory();

	/**
	 * Delete the Entity and its children recursively. Return the result of the
	 * operation.
	 * 
	 * @param file
	 *            file or directory to be removed recursively.
	 * @throws CantRemoveFileException
	 *             throws this exception if entity wasn't completely removed
	 * 
	 */
	public void removeEntity(ExplorerEntity file)
			throws CantRemoveFileException;

	/**
	 * Rename the Entity
	 * 
	 * @param file
	 *            file to be renamed.
	 * @param newName
	 *            new name of the file
	 * @throws CantRenameFileException
	 */
	public void renameEntity(ExplorerEntity file, String newName)
			throws CantRenameFileException;

	/**
	 * Move the File and its children recursively.
	 * 
	 * @param origin
	 *            the original Entity.
	 * @param to
	 *            the new path.
	 * @throws CantMoveFileException
	 *             throws this exception if entity wasn't moved.
	 * 
	 */
	public void moveEntity(ExplorerEntity origin, String newPath)
			throws CantMoveFileException;

	/**
	 * Copy the Entity and its children recursively.
	 * 
	 * @param entity
	 *            entity to be copied.
	 * @param dirDestiny
	 *            folder destiny.
	 * @throws CantMoveFileException
	 *             throws this exception if entity wasn't moved.
	 * 
	 */
	public void copyEntity(ExplorerEntity entity, ExplorerEntity dirDestiny)
			throws CantCopyFileException;

	/**
	 * Create a new folder.
	 * 
	 * @param newDir
	 *            directory to be create.
	 * @throws CantCreateFileException
	 *             throws this exception if directory wasn't created
	 * 
	 */
	public ExplorerEntity createDirectory(ExplorerEntity parent, String name)
			throws CantCreateFileException;

}
