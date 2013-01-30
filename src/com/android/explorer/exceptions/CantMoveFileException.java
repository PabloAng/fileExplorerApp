package com.android.explorer.exceptions;

public class CantMoveFileException extends Exception {

	private static final long serialVersionUID = -7740604129214769078L;

	public CantMoveFileException() {
		super();
	}

	public CantMoveFileException(String message) {
		super(message);
	}
}
