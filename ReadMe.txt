Repository contains a full stack software for REST API. And a mock Single Page Application.
Contents:
CodeTest\RestApi        REST API implementation
CodeTest\RestApi\db     Database for REST API
CodeTest\AndroidApp     REST API client application
CodeTest\SPA            A prototype SPA

1. Development Environment:
==========================
BackEnd     : MySQL
Middleware  : PHP, Slim Framework
FrontEnd    : Android App
Server      : Apache Server

2. Environment Setup:
==============================
2.1) WAMP Server
    WampServer is a local server package for Windows, allows to install and host web applications 
    that use Apache, PHP and MySQL.
    i) Download WAMP Server installer at following location:
        http://www.wampserver.com/en/#download-wrapper
        
    ii) Install WampServer through Setup Wizard. On successful completion, a desktop/quick launch 
        icon is created.
    
    iii) Start WampServer services
        When Wamp Server is launched, WAMP icon (W) is added to system tray . Click on it and 
        choose "Start All Services" from menu. 
    
    iv) Check if all services are up and running
        a) Apache-
            Open a browser and enter the url-> http://localhost/
            It will show WampServer home page.
        b) MySQL-
            Open a browser and enter http://localhost/phpmyadmin/
            It will show the login page for database administration. Enter "root" as username and 
            press "Go" to login. Once logged in new user accounts, databases, tables, etc can be 
            managed from the browser.
    
2.2) Slim Framework
        Slim is a PHP micro framework, used to handle HTTP request and response objects.
        Download the open source from https://github.com/slimphp/Slim.
        It needs to be copied into the PHP project on server.

2.3) Android
    i) Command line tools (Only to build apps):
        Android SDK and Tools required to build existing android apps. 
        a) Download command line package from following location and extract it  (c:\android\)
            https://dl.google.com/android/repository/sdk-tools-windows-4333796.zip
        b) Install latest Android SDK API:
            Goto extracted folder and run the following batch: 
            c:\android\tools\bin>sdkmanager.bat  "platform-tools" "platforms;android-28"
        C) Install Java JDK
            http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

    ii) Android Studio IDE (For Development of Android)
        All components required for Android are bundled into Android Studio. It is available @ 
        https://developer.android.com/studio/

    iii) Building Android App
        a) Open a terminal and change to the source of Android app
        b) Set environment variable
            > cd C:\CodeTest-master\AndroidApp\RetailStore\
            > set ANDROID_HOME=D:\android-sdk
        c) Build the app
            > gradlew assembleDebug
        d) Android apk is generated in build folder
            > C:\CodeTest-master\AndroidApp\RetailStore\app\build\outputs\apk
    
3. Deploying CodeTest project
=====================================================
3.1) Download Project Source
    Source code is available in following location:
        https://github.com/venkat-kamuju/CodeTest
    Download and extract it to a folder: C:\CodeTest-master\

3.2) Setup Database
    i) Db file is present in project: 
        C:\CodeTest-master\RestApi\db\database.sql

    ii) Open phpmyAdmin in browser and setup MySql User, Database and Tables
        Create a db user account: codetest/codetest
        Create database and tables as in database.sql

3.3) Setup middleware on server
    i) Middleware PHP source is present at following:
        C:\CodeTest-master\RestApi

    ii) Create a folder "retail_store" in Server to host
        C:\wamp64\www\retail_store
    
        Copy the contents of RestApi from Project to Server
         cp C:\CodeTest-master\RestApi\include   => C:\wamp64\www\retail_store\include
         cp C:\CodeTest-master\RestApi\v1   => C:\wamp64\www\retail_store\v1

    iii) Copy Slim Framework to Server
        cp C:\Slim = C:\wamp64\www\retail_store\libs\Slim
        
3.4) Install REST API client (Android apk) onto target phone
    i) Prebuilt apk is present at following location:
        C:\CodeTest\AndroidApp\prebuilt_apk\retail_store.apk
    
    ii) Connect phone to desktop through USB and change settings on phone to transfer files
    
    iii) Open terminal, change to Android installation folder and install the apk 
        c:\android\platform-tools>adb install C:\CodeTest\AndroidApp\prebuilt_apk\retail_store.apk

    iv) After installation a new app with label "Inmar Retail" appear on phone.
    
    v) Run the app and check Rest API calls

3.5) Single Page Application
    i) HTML based SPA is present in following location:
        C:\CodeTest-master\SPA
    
    ii) Open index.html in browser to run it

