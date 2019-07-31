<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Back end](#back-end)
  - [Trigger](#trigger)
  - [MySQL Version](#mysql-version)
    - [Set up SQL mode](#set-up-sql-mode)
    - [Set up ODBC drivers](#set-up-odbc-drivers)
    - [Connection string](#connection-string)
    - [Trigger](#trigger-1)
- [Front end](#front-end)
  - [Coding guide](#coding-guide)
    - [iOS](#ios)
    - [Android](#android)
  - [Testing](#testing)
  - [Dependencies](#dependencies)
    - [iOS](#ios-1)
    - [Android](#android-1)
  - [Building guide](#building-guide)
    - [iOS](#ios-2)
    - [Android](#android-2)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->
# DW-iLeave Documentation
Last updated: 2019-07-31 11:48:21
# Highlights
## Apply Leave
Now includes Casual Leave (CL) & Overtime (OT).
If the leave type requires attachment (set by HR administrator through DW-iHR) and one does not upload it, an error dialog will pop up.
Clicking on an attached image will enlarge it.
## My Applications
Now includes Casual Leave (CL) & Overtime (OT).

The user can also filter records by Leave Date and Leave Description.
## Notifications
Technical details are mentioned below.
## Leave Approval
Now includes Casual Leave (CL) & Overtime (OT).

The HR administrator can also filter records by Leave Date, Applicant and Leave Description.

As mentioned above in the *Apply Leave* section, clicking on an attached image will enlarge it.
## Expense Claims
## Download Payroll & Taxation Forms
### iOS
The app uses Apple's built-in [PDFKit](https://developer.apple.com/documentation/pdfkit).
### Android
Requires an external PDF reader to open the file (usually built in to the phone by manufacturers already). If one is not pre-installed, we recommend MoonReader or Librera.
## Forced password change on first login
A modal will show after the user has successfully logged in for the first time. The new password must confirm to the password format designated by the client. The password policy can be set in Code Settings.
## Chatroom
### iOS
The app uses Apple's proprietary built-in Push Notifications, which is enabled in `.xcodeproj`'s `Capabilities` tab.
### Android
The app uses [Firebase Cloud Messaging](https://firebase.google.com/).
# Security
Security is our prime concern thus all data will be secured, and never be given to third parties. We are aware of the recent sabotages of mobile applications produced by several well-known companies; thus the security of  application has reinforced.
## Sessions
Expires after 180 minutes. See [Meaning of parameters in MobileSvc.asmx](#meaning-of-parameters-in-mobilesvc.asmx), that we check the session every `ScriptMethod` (except `_GenerateNonce()` and `_Login()` ).
## Refresh tokens
As previously mentioned, when a session expires, the user will be forced to log out. Yet, the user continues to receive notifications of leave approvals or rejections. This is due to the refresh token mechanism.

A user can obtain a new token without logging in explicitly, by providing the server his old token and refresh token. Recall that when a user reboots his phone, the notification background service starts automatically. To enable the user receiving notifications, the app attempts to get the old token and refresh token of his last login and ask the server for a new pair of tokens. The notification service will start upon a successful exchange.
# Back end
## Trigger
Install OpenSSL beforehand, make sure it is added to `%PATH%`. Put the cURL folder to `C:\` of the **database** server, check that the P13 private and public key file is inside `C:\curl\bin\curl`.

Remember to configure the server beforehand:
```sql
-- To allow advanced options to be changed.  
EXEC sp_configure 'show advanced options', 1;  
GO  
-- To update the currently configured value for advanced options.  
RECONFIGURE;  
GO  
-- To enable the feature.  
EXEC sp_configure 'xp_cmdshell', 1;  
GO  
-- To update the currently configured value for this feature.  
RECONFIGURE;  
GO  
```

## Meaning of parameters in MobileSvc.asmx
`program`: The name of the app. Altering this value will alter the connection value as well, the database of the default table will be different.

Every `ScriptMethod` (i.e. those called by HTTP POST requests) should start with the code snippet below, to check verify the user session:
``` C#
            // Check token and get UserID if success - Begin
            int uid = CheckToken(token, program);
            if (uid == -1)
            {
                Context.Response.StatusCode = 403;
                return "SESSION_EXPIRED";
            }
            // Check token and get UserID if success - End
```
If the session is invalid, or removed by the timeout, any function will return the integer `-1`. This is intended to mask any exploitable error messages.

## MySQL Version

MariaDB is the free and open-source fork of MySQL, thus, there are almost no differences between them. It is usually chosen over MySQL so that companies do not have to worry about licensing issues.

We use the official ODBC driver to communicate between SQL Server and MariaDB. The syntax is very different from SQL Server.

### Set up SQL mode

Don't use `set global sql_mode='mssql'`. The `global` is misleading as the server still resets the sql_mode upon restarts. Instead, add `sql-mode="mssql"` under `[mysqld]`, like this:
```
[mysqld]
sql-mode="mssql"
```

### Set up ODBC drivers

You need to set up **BOTH** 32-bit and 64-bit versions of the official driver. The 32-bit driver is for data transmission between MariaDB and SQL Server. The 64-bit driver is for ASP.NET to access MariaDB.
* 32-bit: Open `C:\Windows\SysWOW64\odbcad32.exe` and add an entry. (Confusing but yes. The **32**-bit tool is in **SysWOW64**.)

* 64-bit: Open `C:\windows\system32\odbcad32.exe` and add an entry.

### Set up SQL Server

To enhance security, the system is designed as follows. Only SQL Server knows the existence of MariaDB. MariaDB does not know the existence of SQL Server. Set the sync interval in the UI. You may manually sync (push to + pull from MariaDB) the data as well. Synchronisation works by filtering records using the `ModifiedDate`. Select the pointers of the old records where modified date is greater than the reference date by key, delete them using `OPENQUERY()`, and insert the new records.

Create a linked server. Use 64-bit ODBC driver. Notice the connection string.

![](https://drive.google.com/uc?export=view&id=1xjQviov4qWayfolZY1vduoc2iKFF4Xjf)
![](https://drive.google.com/uc?export=view&id=1ARDwpip5W88wA945yRg6THKO8l3f5kj3)
![](https://drive.google.com/uc?export=view&id=1Rspn9WKNgMCQSD6eLNUvjTXpUt4YUP5P)

### Set up ASP.NET (C#)
To access the server, use the connection string bellow.
```
Dsn=helloworld;Driver={MariaDB ODBC 3.1 Driver};user=root;password=sa;database=DWS_LEAVEMGR;OPTIONS=67108864;
```

`67108864` allows multiple statements in one command, separated by `;`. 

### Trigger

We can mimic `xp_cmdshell` using MySQL User-Defined Functions Project, install it by  `lib_mysqludf_sys.dll` into `C:\Program Files\MariaDB 10.3\lib\plugin` and execute:
```sql
DROP FUNCTION IF EXISTS sys_eval;
CREATE FUNCTION sys_eval RETURNS string SONAME 'lib_mysqludf_sys.dll';
```

### Synchronisation

To pull data from or push data to the MariaDB server, call the stored procedures `sp_PullMobile` or `sp_PushData`. These stored procedures use `OPENQUERY()` to communicate with the linked server.

# Front end
## Coding guide
### iOS
The app is written in Swift 4, and requires iOS 10.0+.

To add a string, always use `NSLocalizedString(key, comment)` for translation. To add a formatted string, wrap the `NSLocalizedString` inside `String(format: <NSLocalizedString>, CVarArg...)`.

### Android
The app is written in Java, and requires Android 5.0+ (API level 21+).

To add a string, always use `getString(R.string.some_string, arg0, arg1, arg2...)`.

## Testing
Use Lint on Android.
You may want to use wireless debug on both platforms.

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

Now you may upload the generated file to Play Console.
### Fastlane
iOS is substantially different from Android since many security processses have to be done. We use the *Fastlane* toolchain to speed up the deployment process, from managing provisioning profiles and certificates to taking screenshots, metadata and upload a build to App Store Connect.
