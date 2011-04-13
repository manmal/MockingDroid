package com.manuelmaly.mockingdroid;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.content.Context;

public class URIHelper {

	public static URI urlForLink(String originalUrl, String link) {
		try {
			 // originalUrl is HTTP or asset; link is either absolute or relative:
			if (isURLCompatible(originalUrl))
				// this can handle both absolute and relative HTTP or asset
				// links:
				return new URI(new URL(new URL(originalUrl), link).toString());
			// originalUrl is from sd-card; link is either absolute HTTP
			// or relative on sdcard:
			else {
				if (isURLCompatible(link))
					return new URI(link);
				// link seems to be relative and on sd-card
				else { 
					StringBuilder sb = new StringBuilder(originalUrl.substring(0, originalUrl.lastIndexOf("/")));
					String linkFileName = null;
					if (link.lastIndexOf("/") > -1) // link has at least one dir component
						linkFileName = link.substring(link.lastIndexOf("/")+1);
					else // link consists only of filename
						linkFileName = link;
					return new URI(sb.append("/").append(linkFileName).toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Is used to determine if the given url is HTTP/FTP/... or an android
	 * asset. Will return false for sd-card files.
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isURLCompatible(String url) {
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
	
	public static String getFileNameForURI(String uri) {
		URI parseduri;
		try {
			parseduri = new URI(uri);
			return parseduri.getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return uri;
		}
	}
	
	public static URI getMenuScreenURL(Context context, String originalUrl) {
		return getSpecialScreenURL(context, originalUrl, context.getResources().getString(R.string.filesuffix_menu));
	}

	public static URI getSearchScreenURL(Context context, String originalUrl) {
		return getSpecialScreenURL(context, originalUrl, context.getResources().getString(R.string.filesuffix_search));
	}
	
	/**
	 * Returns the URL for a special screen file (menu, search,...) belonging to
	 * the currently displayed file. (e.g. current is "start.html" returns
	 * "start.menu.html")
	 * 
	 * @return String
	 */
	public static URI getSpecialScreenURL(Context context, String originalUrl, String suffix) {
		String fileName = URIHelper.getFileNameForURI(originalUrl);
		int dotPos;
		String specScreenFileName = null;

		// Is this already a special screen? If yes, return to the original
		// screen (remove suffixes); if there is a suffix, but it's another
		// one
		// (e.g. start.menu.html and suffix ".search" is requested), drop
		// the old suffix:
		String suffixMenu = context.getResources().getString(R.string.filesuffix_menu);
		String suffixSearch = context.getResources().getString(R.string.filesuffix_search);
		String[] suffixesToLookFor = new String[] { suffixMenu, suffixSearch };
		for (String curSuffix : suffixesToLookFor) {
			if (fileName.indexOf(curSuffix) > -1) {
				if (suffix.equals(curSuffix))
					return URIHelper.urlForLink(originalUrl, fileName.replace(curSuffix, ""));
				else
					fileName = fileName.replace(curSuffix, "");
			}
		}

		if ((dotPos = fileName.indexOf(".")) > 0) { // filename.ext
			String extension = fileName.substring(dotPos);
			String nameWithoutExt = fileName.substring(0, dotPos);
			specScreenFileName = nameWithoutExt + suffix + extension;

		} else { // filename OR .filename
			specScreenFileName = fileName + suffix;
		}
		URI specScreenURL = URIHelper.urlForLink(originalUrl, specScreenFileName);
		if (resourceExists(context, specScreenURL))
			return specScreenURL;
		return null;
	}

	public static boolean resourceExists(Context context, URI path) {
		// File is on SD-card:
		if (path.toString().indexOf("com.manuelmaly.localfile") > -1) {
			return true;
		}

		// File is in asset folder:
		else if (path.toString().indexOf("android_asset/") > -1) {
			try {
				URL url = path.toURL();
				context.getAssets().open(url.getFile().replace("/android_asset/", "")).close();
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

	public static boolean onlineResourceExists(String URLName) {
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
	
}
