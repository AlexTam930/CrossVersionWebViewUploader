# uploadimage
* A (Android Studio Module) demo that explores how an Android WebView can upload image files from the gallery or camera, helping to make data input more consistent across different devices and Android versions. I tested it on various devices (from 4.2.2 to 6.0.1) to address version-specific challenges. While there are still some compatibility issues on certain Android Kitkat (4.4.4) devices, this demo may be useful for understanding practical approaches to reliable file upload in Android WebViews.
Here are some alternative solutions to consider:

  * Use Cordova instead of the default System WebView.
  * Use JavaScript to trigger native file upload methods.

* **About**: A (Android Studio Module) demo that explores how Android WebView can upload image files from gallery or camera. This demo was tested on multiple Android devices (versions 4.2.2 to 6.0.1) to work around system-level differences and achieve consistent data upload performance—an important building block for any system that needs to reliably collect user-generated content.

And there are some resolvations you could consider: 
1. User [Cordova](http://cordova.apache.org/) instead of System WebView.
2. Use JS to invoke native method of uploading files.
