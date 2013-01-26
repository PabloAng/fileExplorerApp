package com.android.explorer;

import java.io.File;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class UrlButton extends Button {

	private String path;

	public UrlButton(Context context, AttributeSet attrs, int defStyle,
			String path) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		this.path = path;
	}

	public File getFile() {
		return new File(path);
	}

}
