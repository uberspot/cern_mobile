##CERN CA public certificate installer on android and ios devices 

Just an app with a button that when pressed installs cern CA certificates on the devices keyring. 
To add/update the certificates copy them to android/res/raw/ (will be found automatically) or ios/ and in XCode add them as a resource to your project. 
After that edit in android: src/ch/cern/cerncertinstaller/MainActivity.java and add a call in the OnClick method for the extra certificates 
or in ios: ios/CERNCertInstaller/ViewController.m and add a call in the doInstallation method for each extra certificate.


