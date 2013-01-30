package com.android.explorer.exceptions;

public class CantCreateFileException extends Exception {

	private static final long serialVersionUID = -528581176506779250L;

	public CantCreateFileException() {
		super();
	}

	public CantCreateFileException(String message) {
		super(message);
	}
}
