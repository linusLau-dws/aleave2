## Coding guide
### iOS
The app is written in Swift 4, and requires iOS 10.0+.

To add a string, always use `NSLocalizedString(key, comment)` for translation. To add a formatted string, wrap the `NSLocalizedString` inside `String(format: <NSLocalizedString>, CVarArg...)`.

### Android
The app is written in Java, and requires Android 5.0+ (API level 21+).

To add a string, always use `getString(R.string.some_string, arg0, arg1, arg2...)`.

## Dependencies
### iOS
The iOS app employs [CocoaPods](https://cocoapods.org/), a popular package manager for iOS. A new machine should have it installed first:
```bash
$ sudo gem install cocoapods
```
It is very important to open the **.workspace file on XCode, NOT .xcodeproj** .

The project dependent on the following libraries, as listed in the Podfile:
-	[AlamoFire](https://github.com/Alamofire/Alamofire), analogous to Volley on Android, for sending network requests
-	[DropDown](https://github.com/AssistoLab/DropDown), since UIKit does not have built-in drop downs
-	[FMDB](https://github.com/ccgus/fmdb), implementation of SQLite on iOS
-	[NMessenger](https://github.com/eBay/NMessenger), which provides the GUI for messaging
-	[SwiftyJSON](https://github.com/SwiftyJSON/SwiftyJSON), for creating and accessing JSON objects using subscript syntax

### Android

The project dependent on the following libraries, as listed in the Podfile:

-	[Volley](https://developer.android.com/training/volley/index.html), analogous to AlamoFire on iOS
-	[Android-Bootstrap](https://github.com/Bearded-Hen/Android-Bootstrap), UI components

I maintain two libraries myself on GitHub:

-	[TableView for Android](https://github.com/SoftFeta/TableView)
-	[ChatView for Android](https://github.com/SoftFeta/ChatView)

## Building guide
Before building, update the version number in AndroidManifest.xml / .xcodeproj AND the SQL Server table t_Metadata using 
```sql
UPDATE t_Metadata SET [Value] = '<new version number>' WHERE [Key] = 'VERSION_NUMBER'
```
If local (SQLite) schema is changed, use the onUpdate function of SQLHelper / FMDB on iOS to patch it.

### iOS
[App Store Connect guide here](https://instabug.com/blog/how-to-submit-app-to-app-store/)

### Android

1.	Open Android Studio, select `Build` > `Generate Signed Bundle / APK…`
![1](https://drive.google.com/uc?export=view&id=1t3L8_G-ABmgkUp3VjEXYhjvePeeoXqgb)

2.	Choose Android App Bundle if you wish to upload the app to Play Store, otherwise select APK.
![2](https://drive.google.com/uc?export=view&id=13qzP2FdfH7dpR7EHE8Kex97N-mtjVT6Z)

3.	The .jks key store file is inside the same project. Locate it and put its path into the dialog. The key store password and key password are both `dw@4ndr01d`.
![3](https://drive.google.com/uc?export=view&id=1T4tmBrv9GJOdciVsvncYiVOTWGnDMnIr)

4.	Choose release for Build Type and click Finish.
![4](https://drive.google.com/uc?export=view&id=1xz3UOOKehB9IgrjRWxpfE5LWH6gQQShb)

5.	A dialog will appear at the bottom right corner of the screen. Click locate to open the container folder.
![5](https://drive.google.com/uc?export=view&id=11EJkYDkQncdL2s6J2gnlrC_v26ein_UX)
