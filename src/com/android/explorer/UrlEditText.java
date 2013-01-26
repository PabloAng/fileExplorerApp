package com.android.explorer;

import java.io.File;

import android.content.Context;
import android.widget.EditText;

public class UrlEditText extends EditText {

	File file;

	public UrlEditText(Context context, File file) {
		super(context);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

}
