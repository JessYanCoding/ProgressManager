/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.jessyan.progressmanager.demo;

import android.app.Application;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;

/**
 * ================================================
 * Created by JessYan on 06/06/2017 16:29
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class BaseApplication extends Application {

    //这里我就不写管理类了,捡个懒,直接在 Application 中管理单例 Okhttp
    private OkHttpClient mOkHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mOkHttpClient = ProgressManager.getInstance().with(new OkHttpClient.Builder())
                .build();
    }


    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }
}
