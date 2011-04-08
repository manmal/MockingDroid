package com.manuelmaly.mockingdroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.petpet.mockingdroid.monitor.WebViewMonitor;

public class HTML extends Activity {
	
	WebView webview;
	
	WebViewMonitor monitor;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		String startUrl = getResources().getString(R.string.start_page);
		webview = new WebView(this);
		webview.loadUrl(startUrl);
		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);
		webview.setPadding(0, 0, 0, 0);
		setContentView(webview);
		webview.setInitialScale(getScale());
		monitor = new WebViewMonitor(startUrl);
		webview.setWebViewClient(monitor);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			this.monitor.backButtonPressed(webview.getUrl());
			return true;
		}
		System.out.println("com.petpet.mockingdroid.monitor: " + this.monitor.getMetrics());
		return super.onKeyDown(keyCode, event);
	}

	private int getScale() {
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		//set int resource to image width of mockups (check also if size is set in HTML!):
		Double val = new Double(width) / new Double(getResources().getInteger(R.integer.mockups_width)); 
		val = val * 100d;
		return val.intValue();
	}
}