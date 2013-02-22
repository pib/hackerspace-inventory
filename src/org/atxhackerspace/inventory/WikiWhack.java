package org.atxhackerspace.inventory;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.wikipedia.Wiki;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class WikiWhack {
	public class CreateTask extends AsyncTask<InventoryItem, String, Integer> {
		private ProgressDialog progress;
		private Wiki wiki; 
		
		public CreateTask(Wiki loggedIn)
		{
			wiki = loggedIn; 
		}
		
		protected void onPreExecute() {
			progress = ProgressDialog.show(context, "Posting to Wiki", "Starting...");
		}

		protected Integer doInBackground(InventoryItem... wis) {
			InventoryItem wi = wis[0];
			
			try {
				
				String filename = String.format("%s_%s.jpg", wi.item_name.replace(' ', '-'), wi.item_code);
				String image_tag = "";
				if (wi.item_image != null) {
					publishProgress("Uploading image...");
					wiki.upload(wi.image_file, filename, wi.item_name, "Inventory App");
					publishProgress("Image uploaded!");
					image_tag = String.format("[[File:%s|300px|right]]\n\n", filename);
				}
				
				String tpl = "= %s =\n" + image_tag
						+ "== Description ==\n\n" + wi.item_description + "\n\n"
						+ "== Links and Information ==\n\n"
						+ "== Inventory ==\n\n1, I guess\n\n"
						+ "== Location ==\n\nThe Hackerspace, probably\n\n";
				String body = String.format(tpl, wi.item_name, wi.item_description);
				String title = String.format("Inventory/%s", wi.item_code);

				try {
					if((Boolean)wiki.getPageInfo(title).get("exists"))
					// Next line only runs if the page exists
					return R.string.page_exists;
				} catch (FileNotFoundException e) {
					// Expected if item isn't in inventory yet.
				}
				wiki.edit(title, body, "Inventory App Added");
			} catch (IOException e) {
				Log.d("WikiWhack", "IO Error", e);
				return R.string.io_error;
			} catch (LoginException e) {
				return R.string.login_failed;
			} finally {
				wiki.logout();
			}
			return R.string.page_created; // "Wiki Page Created";
		}
		
		protected void onProgressUpdate(String message) {
			progress.setMessage(message);
		}
		
		protected void onPostExecute(Integer result) {
			progress.dismiss();
            // TODO: Call different callback on errors so they can be handled differently
			listener.onCreateComplete(result);
		}
	}

	Context context;
	CreateListener listener;

	public WikiWhack(Context context, CreateListener listener) {
		
		this.listener = listener;
		this.context = context;
	}

	public void create(InventoryItem data, Wiki wiki) {
		
		CreateTask create = new CreateTask(wiki);
		create.execute(data);
	}
}
