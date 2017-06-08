# ProgressManager
[ ![Bintray](https://img.shields.io/badge/bintray-v1.0-brightgreen.svg) ](https://bintray.com/jessyancoding/maven/progressmanager/1.0/link)
[ ![Build Status](https://travis-ci.org/JessYanCoding/ProgressManager.svg?branch=master) ](https://travis-ci.org/JessYanCoding/ProgressManager)
[ ![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat-square) ](https://developer.android.com/about/versions/android-4.0.html)
[ ![License](http://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square) ](http://www.apache.org/licenses/LICENSE-2.0)


## Listen the progress of  downloading and uploading in Okhttp (compatible Retrofit and Glide).


## Overview
![gif](arts/progressManager.gif)


## Introduction
ProgressManager 一行代码即可监听 App 中所有网络链接的上传以及下载进度,包括 Glide的图片加载进度,实现原理类似 EventBus,你可在 App 中的任何地方,将多个监听器,以 Url 地址作为标识符,注册到本框架,当此 Url 地址存在下载或者上传的动作时,会主动调用所有使用此 Url 地址注册过的监听器,达到多个模块的同步更新


## Download
```
 compile 'me.jessyan:progressmanager:1.0'
```

## Usage
### Step 1
```
 //构建 OkHttpClient 时,将 OkHttpClient.Builder() 传入 with() 方法,进行初始化配置
 OkHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder())
                .build();
```

### Step 2
```
 //Glide 下载监听
 ProgressManager.getInstance().addResponseListener(IMAGE_URL, getGlideListener());


 //Okhttp/Retofit 下载监听
 ProgressManager.getInstance().addResponseListener(DOWNLOAD_URL, getDownloadListener());


 //Okhttp/Retofit 上传监听
 ProgressManager.getInstance().addRequestLisenter(UPLOAD_URL, getUploadListener());
```


## ProGuard
```
 -keep class me.jessyan.progressmanager.** { *; }
 -keep interface me.jessyan.progressmanager.** { *; }
```


## About Me
* **Email**: <jess.yan.effort@gmail.com>
* **Home**: <http://jessyan.me>
* **掘金**: <https://gold.xitu.io/user/57a9dbd9165abd0061714613>
* **简书**: <http://www.jianshu.com/u/1d0c0bc634db>

## License
```
 Copyright 2017, jessyan

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