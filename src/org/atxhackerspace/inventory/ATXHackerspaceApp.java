package org.atxhackerspace.inventory;

import org.wikipedia.Wiki;

import android.app.Application;
import android.graphics.Bitmap;

public class ATXHackerspaceApp extends Application {

	private Wiki wiki; 
	private Bitmap bitmap; 
	
	//return our wiki object
	public Wiki GetLoggedInWiki()
	{
		return wiki; 
	}
	
	//Set our wiki object 
	public void SetLoggedInWiki(Wiki wiki)
	{
		this.wiki = wiki; 
	} 
	
	public Bitmap GetLastPhoto()
	{
		return this.bitmap;
	}
	
	public void SetLastPhoto(Bitmap bitmap)
	{
		if(this.bitmap!=null)
		{
			this.bitmap.recycle();
		}
		this.bitmap = bitmap; 
	}
	
}
