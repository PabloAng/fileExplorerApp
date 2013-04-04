package com.java.explorer;

import java.util.ArrayList;
import java.util.List;

import com.android.explorer.exceptions.CantCopyFileException;
import com.android.explorer.exceptions.CantCreateFileException;
import com.android.explorer.exceptions.CantMoveFileException;
import com.android.explorer.exceptions.CantRemoveFileException;
import com.android.explorer.exceptions.CantRenameFileException;

public abstract class EntityManager {

	protected ExplorerEntity root;

	protected EntityManager() {
		this.root = null;
	}

	public List<ExplorerEntity> openDirectory(ExplorerEntity dir) {
		ArrayList<ExplorerEntity> fList = new ArrayList<ExplorerEntity>();

		if (root != null && dir.exists() && dir.isDirectory()) {
			ExplorerEntity[] files = dir.listEntities();
			for (ExplorerEntity f : files) {
				fList.add(f);
			}
		}
		return fList;

	}

	public ExplorerEntity getRootDirectory() {
		return root;
	}

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
			throws CantRemoveFileException {
		if (file.isDirectory())
			for (ExplorerEntity child : file.listEntities())
				removeEntity(child);

		else if (!file.delete())
			throw new CantRemoveFileException("Can't remove file: "
					+ file.getName());
	}

	/**
	 * Rename the Entity
	 * 
	 * @param file
	 *            file to be renamed.
	 * @param newName
	 *            new name of the file
	 * @throws CantRenameFileException
	 */
	public void renameEntity(ExplorerEntity entity, String newName)
			throws CantRenameFileException {

		if (!entity.renameTo(entity.getParentEntity(), newName))
			throw new CantRenameFileException(
					"There is a file/directory with that name in the same folder.");
	}

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
	public void moveEntity(ExplorerEntity entity, ExplorerEntity dirTo)
			throws CantMoveFileException {
		if (dirTo.exists() && dirTo.isDirectory()
				&& entity.renameTo(dirTo, entity.getName()))
			throw new CantMoveFileException("Can't move file: "
					+ entity.getName());
	}

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
			throws CantCopyFileException {
		if (!dirDestiny.exists() || !dirDestiny.isDirectory())
			throw new CantCopyFileException();

		if (entity.isDirectory()) {
			ExplorerEntity copiedDir = dirDestiny.mksubdir(entity.getName());
			ExplorerEntity[] children = entity.listEntities();
			for (ExplorerEntity child : children) {
				try {
					copyEntity(child, copiedDir);
				} catch (Exception e) {
					throw new CantCopyFileException();
				}

			}
		} else {
			try {
				entity.copy(dirDestiny, entity.getName());
			} catch (Exception e) {
				throw new CantCopyFileException();
			}
		}
	}

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
			throws CantCreateFileException {
		ExplorerEntity newDir = null;
		if (parent.exists()) {
			newDir = parent.mksubdir(name);
			if (!newDir.exists())
				throw new CantCreateFileException();
		} else
			throw new CantCreateFileException(
					"File or Directory already exists");
		return newDir;
	}

	public void refreshDirectory(ExplorerEntity dir) {
		dir.refresh();
	}

}
