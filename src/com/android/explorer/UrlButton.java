package com.android.explorer;

import java.io.File;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;

public class UrlButton extends Button {

	private File file;

	public UrlButton(Context context) {
		super(context);
	}

	public UrlButton(Context context, int background, String msg, File file) {
		super(context);
		this.file = file;

		setBackgroundResource(background);
		setTypeface(null, Typeface.BOLD);
		setTextColor(getResources().getColor(android.R.color.white));
		setText(msg);

	}

	public UrlButton(Context context, int background, int drawableLeft,
			String msg, File file) {
		super(context);
		this.file = file;

		setBackgroundResource(background);
		setTypeface(null, Typeface.BOLD);
		setTextColor(getResources().getColor(android.R.color.white));
		setText(msg);
		/*
		 * Drawable img = getResources().getDrawable(drawableLeft); int w =
		 * getWidth(); int h = getHeight(); img.setBounds(0, 0, w, h);
		 * setCompoundDrawables(img, null, null, null);
		 */
	}

	public File getFile() {
		return file;
	}

}
