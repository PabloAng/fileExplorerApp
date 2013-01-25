package com.android.explorer.utils;

import java.io.File;
import java.util.Date;

public class Data {

	public enum DataTypeEnum {
		FOLDER, FILE
	};

	public String id;
	public DataTypeEnum type;
	public Date lastMod;

	public Data(File f) {
		this.id = f.getName();
		this.type = type;
		this.lastMod = lastMod;
	}
	
	public Data(String id, DataTypeEnum type, Date lastMod) {
		this.id = id;
		this.type = type;
		this.lastMod = lastMod;
	}

	public String getId() {
		return id;
	}

	public DataTypeEnum getType() {
		return type;
	}

	public Date getLastModification() {
		return lastMod;
	}

	@Override
	public String toString() {
		return id;
	}
}