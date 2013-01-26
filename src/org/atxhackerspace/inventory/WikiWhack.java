package org.atxhackerspace.inventory;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.graphics.Bitmap;
import android.net.http.AndroidHttpClient;

public class WikiWhack {
	private static final String ENC = "application/x-www-form-urlencoded";

	private NameValuePair p(String name, String val) {
		return new BasicNameValuePair(name, val);
	}

	private URL root_url;

	public WikiWhack(String base_url) throws MalformedURLException {
		root_url = new URL(new URL(base_url), "/api.php");
	}

	public String create(String username, String password, String item_code,
			Bitmap item_image, String item_name, String item_description) {
		URL url;
		try {
			url = api_url(new NameValuePair[] { p("action", "login"),
					p("lgname", username), p("lgpassword", password) });
		} catch (MalformedURLException e) {
			return "URL was invalid :(";
		}
		return "Wiki Page Created";
	}
	
	private URL api_url(NameValuePair... args) throws MalformedURLException {
		String query = "?" + URLEncodedUtils.format(Arrays.asList(args), ENC);
		return new URL(root_url, query);
	}
}
