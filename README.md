##CERN authentication via mobile platform

Code to authenticate to CERNs login.cern.ch on mobile platforms. 
The main way of doing that is opening a webview with the login page,
waiting till the user logs in and then getting the resulting
authentication cookie from the site and using that on any following 
requests on pages that require authentication.
