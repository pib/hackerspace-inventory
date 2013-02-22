package org.atxhackerspace.inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.atxhackerspace.security.auth.login.FailedLoginException;
import org.wikipedia.Wiki;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.Directory;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class InventoryActivity extends Activity implements CreateListener {
	IntentIntegrator integrator;
	TextView item_code;
	EditText item_name, item_description;
	ImageView image_to_upload;
	private View mEntryView;
	private View mLoginStatusView;
	
	public static final int SHRINK_MAX = 1024;
	public static final int THUMBNAIL_MAX = 512;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
	
	private WikiWhack ww;
	Wiki wiki = null;
	


	@Override
	protected void onPause()
	{
		super.onPause();
		//remove the 
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inventory);
		mEntryView = findViewById(R.id.entry_screen);
		mLoginStatusView = findViewById(R.id.logging_on_status);
		SharedPreferences sp = getSharedPreferences(Constants.PREFS_NAME, 0);
		String mUsername = sp.getString(Constants.EXTRA_USERNAME,"");
		String mPassword = sp.getString(Constants.EXTRA_PASSWORD,"");
		
		File [] dir = getExternalCacheDir().listFiles(); 
		if(mUsername.equals("") || mPassword.equals(""))
		{
			Toast.makeText(this, "Please enter your ATX Wiki login", Toast.LENGTH_SHORT).show();
			//need to launch wiki credentials
			Intent i = new Intent(this, SetWikiCredentials.class);
	    	startActivityForResult(i, 0);
	    	finish(); 
			
		}
		else{
			 
			//Set our wiki object up in the 
			this.wiki = ((ATXHackerspaceApp)getApplicationContext()).GetLoggedInWiki();
			if(wiki==null)
			
				{
					showProgress(true);
					wiki = new Wiki("atxhackerspace.org","");
					wiki.setUsingCompressedRequests(false);
					LogInWiki task = new LogInWiki(mUsername, mPassword);
					task.execute(wiki);
				}
		}
	
		

		ww = new WikiWhack(this, this);
		
		integrator = new IntentIntegrator(this);

		item_code = (TextView) findViewById(R.id.item_code);
		image_to_upload = (ImageView) findViewById(R.id.image_to_upload);
		item_name = (EditText) findViewById(R.id.item_name);
		item_description = (EditText) findViewById(R.id.item_description);

		//Shouldn't need to reset this, should we? 
		//if so, let's persist the bitmap...
		setImagePreview(false);

		final Button scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				integrator.initiateScan();
			}
		});

		final Button take_picture = (Button) findViewById(R.id.take_picture);
		take_picture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Log.d("Onclick", Uri.fromFile(getTempFile(2)).toString());
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(2)));
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			}
		});
		
		final Button submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Bitmap image = GetImage(false); 
				if(image==null)
				{
					Toast.makeText(InventoryActivity.this, "Take a picture to upload first!", Toast.LENGTH_LONG).show();
				}
				else{
					InventoryItem toPost = new InventoryItem(
							item_code.getText().toString(), getTempFile(1), GetImage(false),
							item_name.getText().toString(), item_description.getText().toString());
					if (toPost.item_code.matches("") || toPost.item_name.matches("") || toPost.item_description.matches("")) {
						Toast.makeText(InventoryActivity.this, "All fields are required!", Toast.LENGTH_LONG).show();
						return;
					}
					ww.create(toPost,wiki);
				}
			}
		});
	}

	
	public void setImagePreview(boolean ignoreCache) {
		
	
		Bitmap thumbNail = ((ATXHackerspaceApp)getApplicationContext()).GetLastPhoto();
		//Hit our app cache and restore it.
		if(thumbNail!=null && !ignoreCache)
		{
			image_to_upload.setImageBitmap(thumbNail);
		}
		//if it's not in cache, restore it from file and add to app cache.
		else if(getTempFile(3).exists())
		{
			thumbNail = GetImage(true);
			((ATXHackerspaceApp)getApplicationContext()).SetLastPhoto(thumbNail); 
			image_to_upload.setImageBitmap(thumbNail);
		}
		// It's fine if there's no file-don't set it. 
	}
	
	public Bitmap GetImage(boolean getThumbnail)
	{
		FileInputStream fis = null; 
		try {
			fis = new FileInputStream(getTempFile(getThumbnail?3:1));
			BitmapFactory.Options options = new BitmapFactory.Options(); 
			options.inPurgeable=true;
			return BitmapFactory.decodeStream(fis);
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				fis.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void GenerateSmallerAndThumbnail() throws FileNotFoundException {
		
		FileInputStream fis = new FileInputStream(getTempFile(2));
		BitmapFactory.Options options = new BitmapFactory.Options(); 
		options.inPurgeable=true;
		
		Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
		
		Float width  = Float.valueOf(imageBitmap.getWidth());
		Float height = Float.valueOf(imageBitmap.getHeight());
		
		Integer new_width, new_height,thumb_width,thumb_height; 
		if (width > height) {
			Float ratio = height / width;
			new_width = SHRINK_MAX;
			thumb_width = THUMBNAIL_MAX; 
			new_height = (int)(ratio * SHRINK_MAX);
			thumb_height= (int)(ratio * THUMBNAIL_MAX);
		} else {
			//need to detect and autorotate
			Float ratio = width / height;
			new_width = (int)(ratio * SHRINK_MAX);
			new_height = SHRINK_MAX;
			thumb_width= (int)(ratio * THUMBNAIL_MAX);
			thumb_height = SHRINK_MAX;
		}
		Log.d("getPreviewImage", String.format("Shrinking from %d, %d to %d, %d", width.intValue(), height.intValue(), new_width, new_height));
		Bitmap scaledImage = Bitmap.createScaledBitmap(imageBitmap, new_width, new_height, false);
		Bitmap thumbImage = Bitmap.createScaledBitmap(imageBitmap, thumb_width,thumb_height , false);
		FileOutputStream fos = new FileOutputStream(getTempFile(1));		
		FileOutputStream fosThumb = new FileOutputStream(getTempFile(3));
		scaledImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
		thumbImage.compress(Bitmap.CompressFormat.JPEG, 90, fosThumb);
		
		try {
			fos.flush();
			fosThumb.flush();
			fis.close();
			fos.close();
			fosThumb.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private File getTempFile(int version) {
		File path = getExternalCacheDir();
		if (!path.exists()) path.mkdir();
		switch(version)
		{
		case 1:	return new File(path, "last-inventory-pic-small.jpg");
		case 2: return new File(path, "last-inventory-pic.jpg");
		case 3: return new File(path, "last-inventory-pic-thumb.jpg"); 
		default : return null; 
		}
	}
	

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				try {
					GenerateSmallerAndThumbnail();
					setImagePreview(true);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			IntentResult scanResult = IntentIntegrator.parseActivityResult(
					requestCode, resultCode, intent);
			if (scanResult != null) {
				String[] url_parts = scanResult.getContents().split("/");
				String code = url_parts[url_parts.length - 1];
				//Need to check if item already exists here....
				String parsedPage = String.format("Inventory/%s", code);
				showProgress(true);
				GetPageInfo pi = new GetPageInfo(wiki);
				pi.execute(parsedPage);
				item_code.setText(code);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_inventory, menu);
		return true;
	}

	@Override
	public void onCreateComplete(Integer result) {
        // TODO: Handle errors separately (don't clear, add a "Retry" button, etc.)
		new AlertDialog.Builder(this)
			.setMessage(getString(result))
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					InventoryActivity.this.clear();
					dialog.dismiss();
				}
			}).show();
	}

	protected void clear() {
		item_code.setText("");
		image_to_upload.setImageResource(0);
		item_name.setText("");
		item_description.setText("");
	}
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mEntryView.setVisibility(View.VISIBLE);
			mEntryView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mEntryView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mEntryView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	
	private class GetPageInfo extends AsyncTask<String, Void, HashMap<String, Object>>
	{

		private Wiki wiki;
		public HashMap<String,Object> pageInfo; 
		
		@Override
		protected HashMap<String, Object> doInBackground(String ... params) {
		try {
			HashMap<String,Object> results = wiki.getPageInfo(params[0]);
			if((Boolean)results.get("exists"))
			{
				results.put("Page",params[0]); 
			}
			return results; 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null; 
		}
		}

		@Override
		protected void onPostExecute(final HashMap<String, Object> success) {
			if(success == null)
			{
				//handle general I/o Failure
			}
			
			pageInfo = success;
			showProgress(false); 
			if((Boolean)pageInfo.get("exists"))
			{
				String url = String.format("http://atxhackerspace.org/wiki/%s",(String)pageInfo.get("Page"));
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		}
		
		public GetPageInfo(Wiki wiki)
		{
			this.wiki = wiki;
		}

		
	}
	
	
	protected class LogInWiki extends AsyncTask<Wiki, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(Wiki... params) {
			try{
				params[0].login(this.mUsername, this.mPassword);
				((ATXHackerspaceApp)getApplicationContext()).SetLoggedInWiki(params[0]);
				return true; 
			}
			catch(FailedLoginException ex)
			{
				return false; 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		private String mUsername;
		private String mPassword; 
		
		public LogInWiki(String username, String password)
		{
			this.mPassword = password; 
			this.mUsername = username;
		}
		
		@Override
		protected void onPostExecute(final Boolean success) {
			showProgress(false);
			if(success==null)
			{
				Toast.makeText(getApplicationContext(), "Error occured whilst trying to login. Sorry.", Toast.LENGTH_LONG).show();
				//do somethign here cause we got a non-response. 
			}
			else if (!success) {
				//need to launch wiki credentials
				Intent i = new Intent(getApplicationContext(), SetWikiCredentials.class);
		    	startActivity(i); 
		    	finish(); 
			}
		}
	}
	
	
	
}
