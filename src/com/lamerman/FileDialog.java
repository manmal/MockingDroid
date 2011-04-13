package com.lamerman;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.manuelmaly.mockingdroid.R;

public class FileDialog extends ListActivity {

	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";

	public static final String START_PATH = "START_PATH";
	public static final String RESULT_PATH = "RESULT_PATH";

	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";
	private TextView myPath;
	private ArrayList<HashMap<String, Object>> mList;

	private String parentPath;
	private String currentPath = root;

	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Choose the Start Page and tap onto it");
		setResult(RESULT_CANCELED, getIntent());
		setContentView(R.layout.file_dialog_main);
		myPath = (TextView) findViewById(R.id.path);

		String startPath = getIntent().getStringExtra(START_PATH);
		if (startPath != null)
			getDir(startPath);
		else
			getDir(root);
	}

	private void getDir(String dirPath) {
		boolean useAutoSelection = dirPath.length() < currentPath.length();
		Integer position = lastPositions.get(parentPath);
		getDirImpl(dirPath);
		if (position != null && useAutoSelection)
			getListView().setSelection(position);
	}

	private void getDirImpl(String dirPath) {
		myPath.setText(getText(R.string.location) + ": " + dirPath);
		currentPath = dirPath;

		item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(dirPath);
		File[] files = f.listFiles();

		if (!dirPath.equals(root)) {
			item.add(root);
			addItem(root, R.drawable.folder);
			path.add(root);
			item.add("../");
			addItem("../", R.drawable.folder);
			path.add(f.getParent());
			parentPath = f.getParent();
		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();

		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
			} else {
				filesMap.put(file.getName(), file.getName());
				filesPathMap.put(file.getName(), file.getPath());
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(this, mList, R.layout.file_dialog_row, new String[] { ITEM_KEY,
				ITEM_IMAGE }, new int[] { R.id.fdrowtext, R.id.fdrowimage });
		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder);
		}
		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.file);
		}
		fileList.notifyDataSetChanged();
		setListAdapter(fileList);
	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		if (file.isDirectory()) {
			if (file.canRead()) {
				lastPositions.put(currentPath, position);
				getDir(path.get(position));
			} else {
				new AlertDialog.Builder(this).setIcon(R.drawable.icon)
						.setTitle("[" + file.getName() + "] " + getText(R.string.cant_read_folder))
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}).show();
			}
		} else {
			String fileName = file.getName();
			if (fileName.lastIndexOf(".") >= 0) {
				String fileExt = fileName.substring(fileName.lastIndexOf("."));
				if (fileExt.equals(".html") || fileExt.equals(".htm")) {
					getIntent().putExtra(RESULT_PATH, file.getPath());
					setResult(RESULT_OK, getIntent());
					finish();
					v.setSelected(true);
				} else
					wrongFileTypeError();
			} else
				wrongFileTypeError();
		}
	}

	public void wrongFileTypeError() {
		Toast.makeText(getApplicationContext(), "You have to select a .htm or .html file for your Start Page!",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (!currentPath.equals(root)) {
				getDir(parentPath);
			} else {
				return super.onKeyDown(keyCode, event);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}