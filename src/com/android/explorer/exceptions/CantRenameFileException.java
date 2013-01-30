package com.android.explorer.exceptions;

public class CantRenameFileException extends Exception {

	private static final long serialVersionUID = -3035369304259056954L;

	public CantRenameFileException() {
		super();
	}

	public CantRenameFileException(String message) {
		super(message);
	}
}
