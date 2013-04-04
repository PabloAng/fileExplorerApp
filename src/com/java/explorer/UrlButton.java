package com.java.explorer;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;

public class UrlButton extends Button {

	private ExplorerEntity entity;

	public UrlButton(Context context) {
		super(context);
	}

	public UrlButton(Context context, int background, String msg,
			ExplorerEntity entity) {
		super(context);
		this.entity = entity;

		setBackgroundResource(background);
		setTypeface(null, Typeface.BOLD);
		setTextColor(getResources().getColor(android.R.color.white));
		setText(msg);

	}

	public UrlButton(Context context, int background, int drawableLeft,
			String msg, ExplorerEntity entity) {
		super(context);
		this.entity = entity;

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

	public ExplorerEntity getEntity() {
		return entity;
	}

}
