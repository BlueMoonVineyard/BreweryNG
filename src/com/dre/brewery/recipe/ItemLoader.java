package com.dre.brewery.recipe;

import java.io.DataInputStream;

public class ItemLoader {
	private final DataInputStream in;
	private final String saveID;

	public ItemLoader(DataInputStream in, String saveID) {
		this.in = in;
		this.saveID = saveID;
	}

	public DataInputStream getInputStream() {
		return in;
	}

	public String getSaveID() {
		return saveID;
	}
}
