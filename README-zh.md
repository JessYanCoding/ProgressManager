# ProgressManager
[ ![Jcenter](https://img.shields.io/badge/Jcenter-v1.5.0-brightgreen.svg?style=flat-square) ](https://bintray.com/jessyancoding/maven/progressmanager/1.5.0/link)
[ ![Build Status](https://travis-ci.org/JessYanCoding/ProgressManager.svg?branch=master) ](https://travis-ci.org/JessYanCoding/ProgressManager)
[ ![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ProgressManager-brightgreen.svg?style=flat-square)](https://android-arsenal.com/details/1/5865)
[ ![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat-square) ](https://developer.android.com/about/versions/android-4.0.html)
[ ![License](http://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square) ](http://www.apache.org/licenses/LICENSE-2.0)
[ ![Author](https://img.shields.io/badge/Author-JessYan-orange.svg?style=flat-square) ](https://www.jianshu.com/u/1d0c0bc634db)
[ ![QQ-Group](https://img.shields.io/badge/QQ群-301733278-orange.svg?style=flat-square) ](https://shang.qq.com/wpa/qunwpa?idkey=1a5dc5e9b2e40a780522f46877ba243eeb64405d42398643d544d3eec6624917)


## Listen the progress of  downloading and uploading in Okhttp (compatible Retrofit and Glide).


## Overview
![gif](arts/progressManager.gif)


## Introduction
**ProgressManager** 一行代码即可监听 **App** 中所有网络链接的上传以及下载进度,包括 **Glide** 的图片加载进度,实现原理类似 **EventBus**,你可在 **App** 中的任何地方,将多个监听器,以 **Url** 地址作为标识符,注册到本框架,当此 **Url** 地址存在下载或者上传的动作时,框架会主动调用所有使用此 **Url** 地址注册过的监听器,达到多个模块的同步更新.

> [**框架的构思和实现可以看这篇文章**](https://juejin.im/post/593d85e55c497d006b90433d)

## Feature
* 使用简单,只需一行代码即可实现进度监听.
* 多平台支持,支持 **Okhttp** , **Retrofit** , **Glide** ,使用 **Okhttp** 原生 **Api** ,不存在兼容问题.
* 低耦合,实际请求端和进度接收端并不存在直接或间接的关联关系,即可以在 **App** 任何地方接收进度信息.
* 侵入性低,使用本框架你并不需要更改之前进行上传或下载的代码,即使用或不使用本框架并不会影响到原有的代码.
* 多端同步,同一个数据源的上传或下载进度可以指定多个不同的接收端,少去了使用 **EventBus** 实现多个端口同步更新进度.
* 支持多文件上传.
* 支持 **URL** 重定向.
* 自动管理监听器,少去了手动注销监听器的烦恼.
* 默认运行在主线层,少去了切换线程的烦恼.
* 轻量级框架,不包含任何三方库,体积极小.

## Download
``` gradle
 implementation 'me.jessyan:progressmanager:1.5.0'
```

## Usage
### Step 1
``` java
 // 构建 OkHttpClient 时,将 OkHttpClient.Builder() 传入 with() 方法,进行初始化配置
 OkHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder())
                .build();
```

### Step 2
``` java
 // Glide 下载监听
 ProgressManager.getInstance().addResponseListener(IMAGE_URL, getGlideListener());


 // Okhttp/Retofit 下载监听
 ProgressManager.getInstance().addResponseListener(DOWNLOAD_URL, getDownloadListener());


 // Okhttp/Retofit 上传监听
 ProgressManager.getInstance().addRequestListener(UPLOAD_URL, getUploadListener());
```


## ProGuard
```
 -keep class me.jessyan.progressmanager.** { *; }
 -keep interface me.jessyan.progressmanager.** { *; }
```

## Donate
![alipay](https://raw.githubusercontent.com/JessYanCoding/MVPArms/master/image/pay_alipay.jpg) ![](https://raw.githubusercontent.com/JessYanCoding/MVPArms/master/image/pay_wxpay.jpg)

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
