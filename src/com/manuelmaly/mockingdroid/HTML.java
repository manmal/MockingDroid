package com.manuelmaly.mockingdroid;

import java.net.URI;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.manuelmaly.mockingdroid.monitor.WebViewMonitor;

public class HTML extends Activity {

	WebView webview;
	WebViewMonitor monitor;

	String startURI = null;
	boolean backButtonGloballyAllowed;
	boolean currentPageBackButtonAllowed = true;
	String currentPageBackButtonURL = null;
	private Handler mHandler = new Handler();

	// Only used for backbutton presses:
	long lastBackPressTime = 0;
	Toast backToast;

	private final String TAG = "HTML";
	public static final String START_PATH = "START_PATH";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		backButtonGloballyAllowed = getResources().getInteger(R.integer.backbutton_enabled) > 0;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		startURI = LocalFileContentProvider.constructUri(getIntent().getStringExtra(START_PATH));
		webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);
		webview.setPadding(0, 0, 0, 0);
		webview.addJavascriptInterface(new JavaScriptInterface(), "BACKBUTTONSTATUS");
		webview.loadUrl(startURI);
		this.monitor = new WebViewMonitor(this, startURI);
		webview.setWebViewClient(this.monitor);
		setContentView(webview);
		webview.setInitialScale(getScale());
	}

	public void setCurrentPageBackButtonAllowed(boolean allowed) {
		this.currentPageBackButtonAllowed = allowed;
	}

	public void setCurrentPageBackButtonURL(String url) {
		this.currentPageBackButtonURL = url;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && isStartPage()) {
			// on quit application..
			// getMetrics here...
			System.out.println("com.manuelmaly.mockingdroid.monitor: " + this.monitor.getMetrics(webview.getUrl()));

			if (this.lastBackPressTime < System.currentTimeMillis() - 2000) {
				backToast = Toast.makeText(this, "Press back again to end this session!", Toast.LENGTH_SHORT);
				backToast.show();
				this.lastBackPressTime = System.currentTimeMillis();
				return true;
			} else {
				if (backToast != null)
					backToast.cancel();
				return super.onKeyDown(keyCode, event); // let app handle back button (activity end)
			}
		} else if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			if (backButtonGloballyAllowed && currentPageBackButtonAllowed) {
				if (currentPageBackButtonURL != null) {
					String gotoURL = URIHelper.urlForLink(webview.getUrl(), currentPageBackButtonURL).toString();
					Log.i(TAG, "Backbutton-Advice says: go to: " + gotoURL);
					webview.loadUrl(gotoURL);
				} else {
					webview.goBack();
				}
				this.monitor.backButtonPressed(webview.getUrl());
			}
			return true;
		}
		if ((keyCode == KeyEvent.KEYCODE_MENU)) {
			URI menuURL = URIHelper.getMenuScreenURL(getApplicationContext(), webview.getUrl());
			if (menuURL != null)
				webview.loadUrl(menuURL.toString());
			return true;
		}
		if ((keyCode == KeyEvent.KEYCODE_SEARCH)) {
			URI searchURL = URIHelper.getSearchScreenURL(getApplicationContext(), webview.getUrl());
			if (searchURL != null)
				webview.loadUrl(searchURL.toString());
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private boolean isStartPage() {
		return webview.getUrl().equals(startURI);
	}

	private class JavaScriptInterface {
		/**
		 * Gets called by JS within the currently displayed HTML file, to enable
		 * files' control of backbutton-support.
		 * 
		 * @param status
		 */
		@SuppressWarnings("unused")
		public void setBackButtonStatus(final String status) {
			mHandler.post(new Runnable() {
				public void run() {
					HTML.this.currentPageBackButtonAllowed = status.length() == 0;
				}
			});
		}

		/**
		 * Gets called by JS within the currently displayed HTML file, to
		 * optionally set the target file of the next backbutton press.
		 * 
		 * @param status
		 */
		@SuppressWarnings("unused")
		public void setBackButtonAdviceURL(final String backButtonAdviceURL) {
			mHandler.post(new Runnable() {
				public void run() {
					HTML.this.currentPageBackButtonURL = backButtonAdviceURL.length() > 0 ? backButtonAdviceURL : null;
				}
			});
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