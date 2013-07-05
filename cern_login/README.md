##CERN authentication via mobile platform

Code to authenticate to CERNs login.cern.ch on mobile platforms. 
The main way of doing that is 
 1. opening a webview with the login page
 2. waiting till the user logs in and then 
 3. getting the resulting authentication cookie from the site and 
 4. using that on any following requests on pages that require authentication.

###Android implementation

####1) Open a webview with the login page 

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
        //Remove previous cookies
        CookieManager.getInstance().removeSessionCookie();
        webView.loadUrl(authURL); //where authURL is the string with the url to which you want to login

####2,3) Wait till the user logs in and get the cookie
Start an asynctask which runs in another thread and waits 
till it receives a cookie.
Here's an example

     /** An AsyncTask that waits until it gets a cookie for the authUrl
     * from the CookieManager. After it receives the cookie it removes
     * the webView created before and starts a RetrieveGroupsTask. */
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
                  //Return to the previous layout
                  setContentView(R.layout.main_page);
             } catch (Exception e) { e.printStackTrace(); }
        }
     }

####4) Use the cookie in a next request 
With something like this inside an asynctask:

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

                while ((inputLine = in.readLine()) != null) {
                        inputLine = inputLine.trim();
                        //handle input
                }
         }
     }
     } catch(Exception e) { e.printStackTrace(); }
     finally {
        if(urlConnection!=null)
             urlConnection.disconnect();
     }

Note: Make sure you have added the android.permission.INTERNET in your AndroidManifest.xml to be able to access the internet.

###iOS implementation

Open a webView with the login page

     //Called when login button is pressed, opens webview with given url
    NSURL *url = [NSURL URLWithString:@"https://e-groups.cern.ch/e-groups/EgroupsSearchMember.do"];
    WebViewController *webViewController =
    [[WebViewController alloc] initWithURL:url andTitle:@"CERN Login"];
    [self presentViewController:webViewController animated:YES completion:nil];

The WebViewController is a class created to handle our WebView. You can view it here: ![WebViewController.h](https://raw.github.com/uberspot/cern_auth/master/ios/WebViewController.h) and ![WebViewController.m](https://raw.github.com/uberspot/cern_auth/master/ios/WebViewController.m)
You can add them to the project just as they are. In ios there is no need to start a separate thread that will wait until the user logs in.Instead the delegate mechanism is used. When each webpage finishes loading the delegate function webViewDidFinishLoad is called. 
This function retrieves the cookies from the visited url (where the user logged in) and the html content of the page. The cookies retrieved can be then forwarded to the main thread and used in later requests. In the code sample the html content itself is forwarded to the ViewController page so that it's parsed immediately there. 

So in webViewDidFinishLoad we have: 

    //Retrieve cookies with session id for wanted url
    NSArray * availableCookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:[NSURL URLWithString:@"https://e-groups.cern.ch/e-groups/EgroupsSearchMember.do"]];
    NSLog(@"%@", availableCookies);
    if(availableCookies != nil && [availableCookies count] !=0) {
         NSString *content = [webView stringByEvaluatingJavaScriptFromString:@"document.body.outerHTML"];
        
        //Notify viewController and send the retrieved content to it by using NSNotificationCenter
        NSDictionary *site = [NSDictionary dictionaryWithObject:content forKey:@"contentKey"];
        [[NSNotificationCenter defaultCenter] postNotificationName:@"TestNotification" object:nil userInfo:site];
        //Close webview
        [webView stopLoading]; 
        [self dismissViewControllerAnimated:YES completion:nil];
    }

To receive the ,forwarded from webviewcontroller, NSNotification in the ViewContoller page you add a notification observer in viewDidLoad that will listen for notifications.

     //Add notification observer
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(receiveNotification:)
                                                 name:@"TestNotification" object:nil];

and you create a function to be called when a notification is received

    -(void) receiveNotification:(NSNotification *) notification {
        if([[notification name] isEqualToString:@"TestNotification"]) {
            //Retrieve included data from notification
            NSDictionary *contD = notification.userInfo;
            NSString *content = [contD objectForKey:@"contentKey"]; 
        }
     }


Finally assuming you created a .xib UI file with a webview in it you have to of course connect the UI elements delegate to the IBActions in WebViewController as you would connect a simple button to its function in the code. 
