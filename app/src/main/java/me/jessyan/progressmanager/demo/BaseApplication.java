package me.jessyan.progressmanager.demo;

import android.app.Application;

import me.jessyan.progressmanager.ProgressManager;
import okhttp3.OkHttpClient;

/**
 * Created by jess on 06/06/2017 16:29
 * Contact with jess.yan.effort@gmail.com
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
