package com.manuelmaly.mockingdroid;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HTML extends Activity {

	WebView webview;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		webview = new WebView(this);
		webview.loadUrl(getResources().getString(R.string.start_page));
		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);
		webview.setPadding(0, 0, 0, 0);
		webview.setWebViewClient(new UserActionInterceptor());
		setContentView(webview);
		webview.setInitialScale(getScale());
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		if ((keyCode == KeyEvent.KEYCODE_MENU)) {
			URL menuURL = getMenuScreenURL();
			if (menuURL != null)
				webview.loadUrl(menuURL.toString());
			return true;
		}
		if ((keyCode == KeyEvent.KEYCODE_SEARCH)) {
			URL searchURL = getSearchScreenURL();
			if (searchURL != null)
				webview.loadUrl(searchURL.toString());
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private class UserActionInterceptor extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	private URL getMenuScreenURL() {
		return getSpecialScreenURL(getResources().getString(R.string.filesuffix_menu));
	}

	private URL getSearchScreenURL() {
		return getSpecialScreenURL(getResources().getString(R.string.filesuffix_search));
	}

	/**
	 * Returns the URL for a special screen file (menu, search,...) belonging to
	 * the currently displayed file. (e.g. current is "start.html" returns
	 * "start.menu.html")
	 * 
	 * @return String
	 */
	private URL getSpecialScreenURL(String suffix) {
		try {
			URL cURL = new URL(webview.getUrl());
			String fileName = cURL.getFile();
			int dotPos;
			String menuFileName = null;

			// Is this already a special screen? If yes, return to the original
			// screen (remove suffixes); if there is a suffix, but it's another one
			// (e.g. start.menu.html and suffix ".search" is requested), drop the old suffix:
			String suffixMenu = getResources().getString(R.string.filesuffix_menu);
			String suffixSearch = getResources().getString(R.string.filesuffix_search);
			String[] suffixesToLookFor = new String[]{suffixMenu, suffixSearch};
			for(String curSuffix : suffixesToLookFor) {
				if (fileName.indexOf(curSuffix) > -1) {
					if (suffix.equals(curSuffix)) 
						return createURLWithFilename(cURL, fileName.replace(curSuffix, ""));
					else
						fileName = fileName.replace(curSuffix, "");
				}
			}

			if ((dotPos = fileName.indexOf(".")) > 0) { // filename.ext
				String extension = fileName.substring(dotPos);
				String nameWithoutExt = fileName.substring(0, dotPos);
				menuFileName = nameWithoutExt + suffix + extension;

			} else { // filename OR .filename
				menuFileName = fileName + suffix;
			}
			URL menuURL = createURLWithFilename(cURL, menuFileName);
			if (resourceExists(menuURL))
				return menuURL;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private URL createURLWithFilename(URL url, String fileName) throws MalformedURLException {
		return new URL(url.getProtocol(), url.getHost(), url.getPort(), fileName);
	}

	private boolean resourceExists(URL path) {
		// File is in asset folder:
		if (path.toString().indexOf("android_asset/") > -1) {
			try {
				getAssets().open(path.getFile().replace("/android_asset/", "")).close();
				return true;
			} catch (IOException e1) {
				return false;
			}
		}
		// File is somewhere different - online? SDCard? - currently, handle
		// only online:
		else {
			return onlineResourceExists(path.toString());
		}
	}

	private boolean onlineResourceExists(String URLName) {
		try {
			HttpURLConnection.setFollowRedirects(false);
			// note : you may also need
			// HttpURLConnection.setInstanceFollowRedirects(false)
			HttpURLConnection con = (HttpURLConnection) new URL(URLName).openConnection();
			con.setRequestMethod("HEAD");
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private int getScale() {
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		// set int resource to image width of mockups (check also if size is set
		// in HTML!):
		Double val = new Double(width) / new Double(getResources().getInteger(R.integer.mockups_width));
		val = val * 100d;
		return val.intValue();
	}
}