package com.duckduckgo.mobile.android.views.webview;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import com.duckduckgo.mobile.android.activity.DuckDuckGo;
import com.duckduckgo.mobile.android.util.DDGConstants;
import com.duckduckgo.mobile.android.util.DDGUtils;
import com.duckduckgo.mobile.android.util.SESSIONTYPE;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class DDGWebViewClient extends WebViewClient {
	boolean loadingFinished = true;
	boolean redirect = false;
		
	DuckDuckGo activity;
	
	public DDGWebViewClient(DuckDuckGo activity) {
		this.activity = activity;
	}
	 	
	private void clickedAnchorAction(DDGWebView view) {
		view.allowInHistory = true; 
		activity.mDuckDuckGoContainer.sessionType = SESSIONTYPE.SESSION_BROWSE;
		view.resetReadabilityState();
	}
	        	        	        	
	public boolean shouldOverrideUrlLoading(WebView view, String url) { 
		// Log.i(TAG, "shouldOverrideUrl  " + url);
		
		if(!activity.savedState) {			
			if (!loadingFinished) {
				redirect = true;
			}
			else {
				clickedAnchorAction((DDGWebView) view);
			}
			loadingFinished = false;
			
			// handle mailto: and tel: links with native apps
			if(url.startsWith("mailto:")){
                MailTo mt = MailTo.parse(url);
                Intent i = DDGUtils.newEmailIntent(mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                activity.startActivity(i);
                return true;
            }
			else if(url.startsWith("tel:")){
                Intent i = DDGUtils.newTelIntent(url);
                activity.startActivity(i);
                return true;
			}
			else if(url.startsWith("file:///android_asset/webkit/")){
				return false;
			}
			else if(!(url.startsWith("http:") || url.startsWith("https:"))) {
				// custom handling, there can be a related app
				try {
					Intent customIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					activity.startActivity(customIntent);
					return true;
				}
				catch(ActivityNotFoundException ae) {
					// no related app, inform and still try to load in browser
					Toast.makeText(activity, "No related app found!", Toast.LENGTH_LONG).show();
//					view.loadUrl(url);
				}
			}
		}
		return false;
	}
	
	@SuppressLint("NewApi")
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		
		loadingFinished = false;
		        		
		if(url.equals(activity.mDuckDuckGoContainer.lastFeedUrl)) {
			activity.mDuckDuckGoContainer.sessionType = SESSIONTYPE.SESSION_FEED;
		}
		        		
		// Omnibar like behavior.
		if (url.contains("duckduckgo.com")) {
			view.getSettings().setUserAgentString(DDGConstants.USER_AGENT);
			
	        view.getSettings().setSupportZoom(true);
	        view.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
	        view.getSettings().setBuiltInZoomControls(false);
	        view.getSettings().setUseWideViewPort(false);
	        view.getSettings().setLoadWithOverviewMode(false);
	        
	        view.getSettings().setPluginState(WebSettings.PluginState.ON);
	        view.getSettings().setPluginsEnabled(true); 

	        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
    	        view.getSettings().setEnableSmoothTransition(false);
    	        view.getSettings().setDisplayZoomControls(false);
	        }
	        
			URL fullURL = null;
			try {
				fullURL = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			if (fullURL != null) {
				//Okay, it's a valid url, which we already knew...
				
				// disambiguations appear directly as path string
				String path = fullURL.getPath();
				
				String query = fullURL.getQuery();
				if (query != null) {
					//Get the actual query string now...
					int index = query.indexOf("q=");
					if (index != -1) {
    					String text = query.substring(query.indexOf("q=") + 2);
    					if (text.contains("&")) {
    						text = text.substring(0, text.indexOf("&"));
    					}
    					String realText = URLDecoder.decode(text);
    					activity.setSearchBarText(realText);
					}
					else if(path != null && !path.equals("/")){
    					String text = path.substring(path.lastIndexOf("/") + 1).replace("_", " ");
    					activity.setSearchBarText(text);
    				}
					else {
						activity.setSearchBarText(url);
					}
				}
				else {
					activity.setSearchBarText(url);
				}
			} else {
				//Just in case...
				activity.setSearchBarText(url);
			}
		} else {
			//This isn't duckduck go...
			view.getSettings().setUserAgentString(activity.mWebViewDefaultUA);
			// This is a bit strange, but necessary to load Twitter in the app
			//TODO: Figure out a better way, it has something to do with JS with errors
			if (url.contains("twitter.com")) {
				view.getSettings().setUserAgentString(DDGConstants.USER_AGENT);
			}
			
	        view.getSettings().setSupportZoom(true);
	        view.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
	        view.getSettings().setBuiltInZoomControls(true);
	        view.getSettings().setUseWideViewPort(true);
	        view.getSettings().setLoadWithOverviewMode(true);
	        view.getSettings().setPluginState(WebSettings.PluginState.ON);
	        view.getSettings().setPluginsEnabled(true); 
	        
	        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
    	        view.getSettings().setEnableSmoothTransition(true);
    	        view.getSettings().setDisplayZoomControls(false);
	        }
	        
	        activity.setSearchBarText(url);
		}
	}
	
	public void onPageFinished (WebView view, String url) {
		super.onPageFinished(view, url);
		
		if(!redirect){
			loadingFinished = true;
		}

		if(!loadingFinished || redirect){
			redirect = false; 
		}
		
		activity.mCleanSearchBar = false;
		
		if(!((DDGWebView) view).allowInHistory) {
			view.clearHistory();
		}
		
		if(view.getVisibility() != View.VISIBLE) {
			return;
		}
		
		activity.searchField.setBackgroundDrawable(activity.mDuckDuckGoContainer.searchFieldDrawable);
		
//		// This makes a little (X) to clear the search bar.
//		mDuckDuckGoContainer.reloadDrawable.setBounds(0, 0, (int)Math.floor(mDuckDuckGoContainer.reloadDrawable.getIntrinsicWidth()/1.5), (int)Math.floor(mDuckDuckGoContainer.reloadDrawable.getIntrinsicHeight()/1.5));
//        searchField.setCompoundDrawables(null, null, mDuckDuckGoContainer.reloadDrawable, null);
	}
}
