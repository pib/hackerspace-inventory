package org.atxhackerspace.inventory;

import java.io.File;

import android.graphics.Bitmap;

public class InventoryItem {
	String wikiUrl;

	String username;
	String password;
	String item_code;
	File image_file;
	Bitmap item_image;
	String item_name;
	String item_description;

	public InventoryItem(String username, String password, String item_code,
			File image_file, Bitmap item_image, String item_name, String item_description) {
		this.username = username;
		this.password = password;
		this.item_code = item_code;
		this.image_file = image_file;
		this.item_image = item_image;
		this.item_name = item_name;
		this.item_description = item_description;
	}
}
