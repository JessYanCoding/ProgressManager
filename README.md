# ProgressManager
[![](https://jitpack.io/v/kibotu/ProgressManager.svg)](https://jitpack.io/#kibotu/ProgressManager)
[![Build Status](https://app.travis-ci.com/kibotu/ProgressManager.svg?branch=master)](https://app.travis-ci.com/kibotu/ProgressManager)
[ ![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ProgressManager-brightgreen.svg?style=flat-square)](https://android-arsenal.com/details/1/5865)
[ ![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat-square) ](https://developer.android.com/about/versions/android-4.0.html)
[ ![License](http://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square) ](http://www.apache.org/licenses/LICENSE-2.0)
[ ![Author](https://img.shields.io/badge/Author-JessYan-orange.svg?style=flat-square) ](https://www.jianshu.com/u/1d0c0bc634db)
[ ![Author](https://img.shields.io/badge/Author-Jan%20Rabe-orange.svg?style=flat-square) ](https://kibotu.net)


## Listen the progress of  downloading and uploading in Okhttp (compatible Retrofit and Glide).

[中文说明](README-zh.md)

## Overview
![gif](arts/progressManager.gif)

## Introduction
**ProgressManager** a line of code to listen **App** all the links and upload the progress of the network, including **Glide** picture loading progress, to achieve the principle of similar **EventBus**, you can be in anywhere in **App**, the number of listeners to **Url** address as an identifier, registered to the framework, when this **Url** address haves to download or upload the action, the framework will take the initiative to call All listeners registered with this **Url** address are synchronized to multiple modules.


## Feature
* Easy to use, just a line of code to listen progress.
* Multi-platform support, support **Okhttp** , **Retrofit** , **Glide** ,Use **Okhttp** native **Api** , there is no compatibility problem.
* Low coupling, the actual request and the progress of the receiver does not exist directly or indirectly, that can be anywhere in **App** to receive progress information.
* Low intrusion, use this framework you do not need to change the code before uploading or downloading, ie using or not using this framework does not affect the original code.
* Multi-end synchronization, the same data source upload or download progress can specify a number of different receivers, less to use **EventBus** achieve multiple port synchronization update progress.
* Support multi-file upload.
* Support **URL** redirection.
* Automatic management of the listener, less to manually cancel the trouble of the listener.
* The default run in the main line layer, less to switch the thread of trouble.
* Lightweight framework, does not contain any three-party library, very small size.

## Download
``` gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
    implementation 'com.github.kibotu:ProgressManager:Tag'
}
```

## Usage
### Step 1
``` java
 // When building OkHttpClient, the OkHttpClient.Builder() is passed to the with() method to initialize the configuration
 OkHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder())
                .build();
```

### Step 2
``` java
 // Glide load
 ProgressManager.getInstance().addResponseListener(IMAGE_URL, getGlideListener());


 // Okhttp/Retofit download
 ProgressManager.getInstance().addResponseListener(DOWNLOAD_URL, getDownloadListener());


 // Okhttp/Retofit upload
 ProgressManager.getInstance().addRequestListener(UPLOAD_URL, getUploadListener());
```

## About Me

Follow me on Twitter: [@wolkenschauer](https://twitter.com/wolkenschauer)
Let me know what you think: [jan.rabe@kibotu.net](mailto:jan.rabe@kibotu.net)

Contributions welcome!

## License
```
 Copyright 2021, Jan Rabe

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
