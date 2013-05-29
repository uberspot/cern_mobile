/**
MIT/X Consortium License 

uberspot <Paul Sarbinowski>

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.

 */
package com.cern.cernauth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.TextView;

public class MainPage extends Activity {

	/* Views */
	private WebView webView;
	private ListView listView;
	private TextView output;
	
	/* The authentication cookie received after the user
	 * logs in the cern site. Used in each request after 
	 * that to authenticate again automatically. */
	private String authCookie = null;
	
	/* The url from which we fetch info after the authentication. 
	 * If on initial load you are not authenticated you are redirected to
	 * login.cern.ch . */
	private String authURL = "https://e-groups.cern.ch/e-groups/EgroupsSearchMember.do";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return false;
    }
    
    /** Called when the Login button is clicked. 
     *  It starts a webview that displays the cern login page
     *  and starts a WaitForCookieTask.
     * @param v
     */
    public void onLoginClick(View v) {
    	webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

        });
        setContentView(webView);

        CookieManager.getInstance().removeSessionCookie();
        webView.loadUrl(authURL);
        
        new WaitForCookieTask().execute();
    }
    
    /** An AsyncTask that waits until it gets a cookie for the authUrl
     *  from the CookieManager. After it receives the cookie it removes
     *  the webView created before and starts a RetrieveGroupsTask. */
   class WaitForCookieTask extends AsyncTask<String, Void, String> {
	   
       @Override protected void onPreExecute() { }

       protected String doInBackground(String... strings) {
    	   CookieManager cookieManager = CookieManager.getInstance();
           while (authCookie == null) {
	           	try {
	   				Thread.sleep(5000);
	   			} catch (InterruptedException e) { }
                authCookie = cookieManager.getCookie(authURL);
           }
           
           return "";
       }
       protected void onPostExecute(String result) { 
    	   try {
        	   webView.stopLoading();
        	   Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(webView, (Object[]) null);
        	   setContentView(R.layout.main_page);
        	   //DO NEW REQUEST FOR GROUPS, START ACTIVITY
        	   output = (TextView) findViewById(R.id.outputTextView);    	  		
    		   output.setText("Retrieving groups...");
        	   new RetrieveGroupsTask().execute();
        	   
           } catch (Exception e) { e.printStackTrace(); }
       }
   }
   
   /** An AsyncTask that connects to the URL that needs authentication 
    * using the authCookie, retrieves the page in that url, parses it 
    * and searches for group names. It's mostly to test if the 
    * authentication cookie works properly. */
   class RetrieveGroupsTask extends AsyncTask<String, Void, String> {
	   
	   ArrayList<String> groups = null;
	   
       @Override protected void onPreExecute() { }

       protected String doInBackground(String... strings) {
    	   
    	   if(authCookie==null)
    		   return null;
    	   HttpsURLConnection urlConnection = null;
    	   try { 
    		     URL url = new URL(authURL);
    		     urlConnection = (HttpsURLConnection) url.openConnection();
    		     // Add Cookie to request
    		     urlConnection.setRequestProperty("Cookie", authCookie);
    		     urlConnection.connect();
    		     
    		     if(urlConnection.getResponseCode() == 200) {
		    		     // Read page in url
		    			 BufferedReader in = new BufferedReader( new InputStreamReader( urlConnection.getInputStream() ) );
		    			 String inputLine;
		    			
		    			 groups = new ArrayList<String>();
		    			 while ((inputLine = in.readLine()) != null) {
		    				inputLine = inputLine.trim();
		    				if(inputLine.startsWith("<a href=\"mailto:")) {
		    					groups.add(inputLine.replace("<a href=\"mailto:", "").replaceAll("@cern.ch.*", ""));
		    				}
		    			 }
    			 }
    		 } catch(Exception e) { e.printStackTrace(); } 
    	     finally {
    	    	   if(urlConnection!=null)
    	    		   urlConnection.disconnect();
    	     }
    	   return "";
       }
       protected void onPostExecute(String result) { 
    	   if(groups!=null) {
    		   output = (TextView) findViewById(R.id.outputTextView);
    	  	   listView = (ListView) findViewById(R.id.groupList);
    	  		
    		   output.setText("Groups (" + groups.size() + "):\n");
	    	
    		   // Update Listview with group names
	   	       ListAdapter adapter = new ListAdapter(getApplicationContext(), groups.toArray(new String[groups.size()]));
	   	       listView.setAdapter(adapter);
    	   }
       }
   }
   
}
