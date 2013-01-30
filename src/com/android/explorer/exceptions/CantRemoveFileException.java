package com.android.explorer.exceptions;

public class CantRemoveFileException extends Exception {

	private static final long serialVersionUID = -6662978595308403451L;

	public CantRemoveFileException() {
		super();
	}

	public CantRemoveFileException(String message) {
		super(message);
	}
}
