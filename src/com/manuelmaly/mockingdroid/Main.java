package com.manuelmaly.mockingdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.lamerman.FileDialog;

public class Main extends Activity {
	private final Integer REQUEST_LOAD_START_HTML_FILE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button startFileChooseBtn = (Button) findViewById(R.id.selectStartFile);
		startFileChooseBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startFileChooser();
			}
		});
	}

	private void startFileChooser() {
		Intent intent = new Intent(getBaseContext(), FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory());
		startActivityForResult(intent, REQUEST_LOAD_START_HTML_FILE);
	}

	public synchronized void onActivityResult(final int requestCode, int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_LOAD_START_HTML_FILE) {
				String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
				if (filePath.length() > 0) {
					Intent intent = new Intent(getBaseContext(), HTML.class);
					intent.putExtra(HTML.START_PATH, filePath);
					startActivity(intent);
				} else
					showFileLoadError();
			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			showFileLoadError();
		}
	}

	private void showFileLoadError() {
		Toast toast = Toast.makeText(getApplicationContext(),
				"You have to select a starting HTML file (.htm or .html) for your mockup!", Toast.LENGTH_LONG);
		toast.show();
	}
}
