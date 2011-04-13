package com.manuelmaly.mockingdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;

public class Main extends Activity {
	private final Integer REQUEST_LOAD_START_HTML_FILE = 10;
	private String startPagePath = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Define Your Mockup");
		setContentView(R.layout.main);
		TextView startPageTxt = (TextView) findViewById(R.id.txtStartPage);
		startPageTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFileChooser();
			}
		});
		Button startBtn = (Button) findViewById(R.id.btnStartMockup);
		startBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startSession();
			}
		});
	}

	private void startFileChooser() {
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory());
		startActivityForResult(intent, REQUEST_LOAD_START_HTML_FILE);
	}
	
	private void startSession() {
		if (startPagePath != null && startPagePath.length() > 0) {
			Intent intent = new Intent(getBaseContext(), HTML.class);
			intent.putExtra(HTML.START_PATH, startPagePath);
			startActivity(intent);
		} else
			showFileLoadError();
	}

	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_LOAD_START_HTML_FILE) {
				startPagePath = data.getStringExtra(FileDialog.RESULT_PATH);
				TextView startPageTxt = (TextView) findViewById(R.id.txtStartPage);
				startPageTxt.setText(startPagePath);
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			//Handle
		}
	}

	private void showFileLoadError() {
		Toast toast = Toast.makeText(getApplicationContext(),
				"You have to select a Start Page (.htm or .html) for your mockup!", Toast.LENGTH_LONG);
		toast.show();
	}
}
