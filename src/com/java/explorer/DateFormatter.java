package com.java.explorer;

import java.text.DateFormat;
import java.util.Date;

//Created by plusminus on 22:42:55 - 12.11.2008
//package org.andnav2.util;

public class DateFormatter {

	public static String formatDate(long date) {
		return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.SHORT).format(new Date(date));
	}
}