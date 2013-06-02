package com.android.explorer.exceptions;

public class CantOpenFileException extends Exception {

	private static final long serialVersionUID = 2847966193623194505L;

	public CantOpenFileException() {
		super();
	}

	public CantOpenFileException(String message) {
		super(message);
	}
}
