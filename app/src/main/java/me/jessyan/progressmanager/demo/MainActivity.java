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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * ================================================
 * 这里展示框架的基本功能, 高级功能请看 {@link AdvanceActivity}
 * 代码虽然多,但核心方法就在 {@link BaseApplication#onCreate()} 和 {@link #initListener()} 这两处
 * 其他代码都是在做请求和下载以及 UI 展示, 和框架没有任何关系, 可以作为参考, 但这些代码每个项目都不一样
 * 比如你喜欢用 Retrofit 的 {@code @Multipart} 进行资源的上传, 这些看个人的喜好进行修改
 * <p>
 * 请注意 Demo 只展示了 Okhttp 的下载上传监听和 Glide 的加载监听
 * 但是 Retrofit 的下载和上传监听同样完美支持
 * 因为 Retrofit 底层默认使用的是 Okhttp 做网络请求, 所以只要您照着 {@link BaseApplication#onCreate()} 中的代码
 * 给 Okhttp 配置了 {@link okhttp3.Interceptor}, 并且使用 {@link ProgressManager#addResponseListener(String, ProgressListener)}
 * 或 {@link ProgressManager#addResponseListener(String, ProgressListener)} 给对应的 {@code url} 添加了监听器
 * <p>
 * 当做了以上两步操作后, 不管您是使用 Retrofit, Okhttp 还是 Glide, 以及请求或下载的方式, 代码的结构层次, 这些东西不管如何变化, 都不会对监听效果有任何影响
 * 只要这个 {@code url} 存在上传 (请求时有 {@link RequestBody}) 或下载 (服务器有返回 {@link ResponseBody}) 的动作时, 监听器就一定会被调用
 * <p>
 * Created by JessYan on 08/06/2017 12:59
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    /**
     * 全局持有 url 是为了使用 {@link WeakHashMap} 的特性,在 {@link ProgressManager} 中顶部有介绍
     * 使用 String mUrl = new String("url");, 而不是 String mUrl = "url";
     * 为什么这样做? 因为如果直接使用 String mUrl = "url", 这个 url 字符串会被加入全局字符串常量池, 池中的字符串将不会被回收
     * 既然 {@code key} 没被回收, 那 {@link WeakHashMap} 中的值也不会被移除
     * 在 {@link #onDestroy()} 中一定记得释放被引用的 url (将 url 设为 null), 这样框架就能在 java 虚拟机 GC 时释放对应的监听器
     */
    public String mImageUrl = new String("https://raw.githubusercontent.com/JessYanCoding/MVPArmsTemplate/master/art/step.png");
    public String mDownloadUrl = new String("https://raw.githubusercontent.com/JessYanCoding/MVPArmsTemplate/master/art/MVPArms.gif");
    public String mUploadUrl = new String("http://upload.qiniu.com/");

    private ImageView mImageView;
    private OkHttpClient mOkHttpClient;
    private ProgressBar mGlideProgress;
    private ProgressBar mDownloadProgress;
    private ProgressBar mUploadProgress;
    private TextView mGlideProgressText;
    private TextView mDownloadProgressText;
    private TextView mUploadProgressText;

    private ProgressInfo mLastDownloadingInfo;
    private ProgressInfo mLastUploadingingInfo;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOkHttpClient = ((BaseApplication) getApplicationContext()).getOkHttpClient();
        mHandler = new Handler();
        initView();
        initListener();
        //在 Activity 中显示进度条的同时,也在 Fragment 中显示对应 url 的进度条,为了展示此框架的多端同步更新某一个进度信息
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                MainFragment.newInstance(mImageUrl, mDownloadUrl, mUploadUrl)).commit();
    }


    private void initView() {
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.imageView);
        mGlideProgress = findViewById(R.id.glide_progress);
        mDownloadProgress = findViewById(R.id.download_progress);
        mUploadProgress = findViewById(R.id.upload_progress);
        mGlideProgressText = findViewById(R.id.glide_progress_text);
        mDownloadProgressText = findViewById(R.id.download_progress_text);
        mUploadProgressText = findViewById(R.id.upload_progress_text);
        findViewById(R.id.glide_start).setOnClickListener(this);
        findViewById(R.id.download_start).setOnClickListener(this);
        findViewById(R.id.upload_start).setOnClickListener(this);
        findViewById(R.id.advance).setOnClickListener(this);
    }

    private void initListener() {
        //Glide 加载监听
        ProgressManager.getInstance().addResponseListener(mImageUrl, getGlideListener());


        //Okhttp/Retofit 下载监听
        ProgressManager.getInstance().addResponseListener(mDownloadUrl, getDownloadListener());


        //Okhttp/Retofit 上传监听
        ProgressManager.getInstance().addRequestListener(mUploadUrl, getUploadListener());
    }


    @NonNull
    private ProgressListener getGlideListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                int progress = progressInfo.getPercent();
                mGlideProgress.setProgress(progress);
                mGlideProgressText.setText(progress + "%");
                Log.d(TAG, "--Glide-- " + progress + " %  " + progressInfo.getSpeed() + " byte/s  " + progressInfo.toString());
                if (progressInfo.isFinish()) {
                    //说明已经加载完成
                    Log.d(TAG, "--Glide-- finish");
                }
            }

            @Override
            public void onError(long id, Exception e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mGlideProgress.setProgress(0);
                        mGlideProgressText.setText("error");
                    }
                });
            }
        };
    }

    @NonNull
    private ProgressListener getUploadListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                // 如果你不屏蔽用户重复点击上传或下载按钮,就可能存在同一个 Url 地址,上一次的上传或下载操作都还没结束,
                // 又开始了新的上传或下载操作,那现在就需要用到 id(请求开始时的时间) 来区分正在执行的进度信息
                // 这里我就取最新的上传进度用来展示,顺便展示下 id 的用法

                if (mLastUploadingingInfo == null) {
                    mLastUploadingingInfo = progressInfo;
                }

                //因为是以请求开始时的时间作为 Id ,所以值越大,说明该请求越新
                if (progressInfo.getId() < mLastUploadingingInfo.getId()) {
                    return;
                } else if (progressInfo.getId() > mLastUploadingingInfo.getId()) {
                    mLastUploadingingInfo = progressInfo;
                }

                int progress = mLastUploadingingInfo.getPercent();
                mUploadProgress.setProgress(progress);
                mUploadProgressText.setText(progress + "%");
                Log.d(TAG, "--Upload-- " + progress + " %  " + mLastUploadingingInfo.getSpeed() + " byte/s  " + mLastUploadingingInfo.toString());
                if (mLastUploadingingInfo.isFinish()) {
                    //说明已经上传完成
                    Log.d(TAG, "--Upload-- finish");
                }
            }

            @Override
            public void onError(long id, Exception e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mUploadProgress.setProgress(0);
                        mUploadProgressText.setText("error");
                    }
                });
            }
        };
    }

    @NonNull
    private ProgressListener getDownloadListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                // 如果你不屏蔽用户重复点击上传或下载按钮,就可能存在同一个 Url 地址,上一次的上传或下载操作都还没结束,
                // 又开始了新的上传或下载操作,那现在就需要用到 id(请求开始时的时间) 来区分正在执行的进度信息
                // 这里我就取最新的下载进度用来展示,顺便展示下 id 的用法

                if (mLastDownloadingInfo == null) {
                    mLastDownloadingInfo = progressInfo;
                }

                //因为是以请求开始时的时间作为 Id ,所以值越大,说明该请求越新
                if (progressInfo.getId() < mLastDownloadingInfo.getId()) {
                    return;
                } else if (progressInfo.getId() > mLastDownloadingInfo.getId()) {
                    mLastDownloadingInfo = progressInfo;
                }

                int progress = mLastDownloadingInfo.getPercent();
                mDownloadProgress.setProgress(progress);
                mDownloadProgressText.setText(progress + "%");
                Log.d(TAG, "--Download-- " + progress + " %  " + mLastDownloadingInfo.getSpeed() + " byte/s  " + mLastDownloadingInfo.toString());
                if (mLastDownloadingInfo.isFinish()) {
                    //说明已经下载完成
                    Log.d(TAG, "--Download-- finish");
                }
            }

            @Override
            public void onError(long id, Exception e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadProgress.setProgress(0);
                        mDownloadProgressText.setText("error");
                    }
                });
            }
        };
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.glide_start:
                glideStart();
                break;
            case R.id.download_start:
                downloadStart();
                break;
            case R.id.upload_start:
                uploadStart();
                break;
            case R.id.advance:
                startActivity(new Intent(getApplicationContext(), AdvanceActivity.class));
                break;
        }
    }

    /**
     * 点击开始上传资源,为了演示,就不做重复点击的处理,即允许用户在还有进度没完成的情况下,使用同一个 url 开始新的上传
     */
    private void uploadStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //为了方便就不动态申请权限了,直接将文件放到CacheDir()中
                    File file = new File(getCacheDir(), "a.java");
                    //读取Assets里面的数据,作为上传源数据
                    writeToFile(getAssets().open("a.java"), file);

                    Request request = new Request.Builder()
                            .url(mUploadUrl)
                            .post(RequestBody.create(MediaType.parse("multipart/form-data"), file))
                            .build();

                    Response response = mOkHttpClient.newCall(request).execute();
                    response.body();
                } catch (IOException e) {
                    e.printStackTrace();
                    //当外部发生错误时,使用此方法可以通知所有监听器的 onError 方法
                    ProgressManager.getInstance().notifyOnErorr(mUploadUrl, e);
                }
            }
        }).start();
    }

    /**
     * 点击开始下载资源,为了演示,就不做重复点击的处理,即允许用户在还有进度没完成的情况下,使用同一个 url 开始新的下载
     */
    private void downloadStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url(mDownloadUrl)
                            .build();

                    Response response = mOkHttpClient.newCall(request).execute();

                    InputStream is = response.body().byteStream();
                    //为了方便就不动态申请权限了,直接将文件放到CacheDir()中
                    File file = new File(getCacheDir(), "download");
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    bis.close();
                    is.close();


                } catch (IOException e) {
                    e.printStackTrace();
                    //当外部发生错误时,使用此方法可以通知所有监听器的 onError 方法
                    ProgressManager.getInstance().notifyOnErorr(mDownloadUrl, e);
                }
            }
        }).start();
    }

    /**
     * 点击开始 Glide 加载图片,为了演示,就不做重复点击的处理,但是 Glide 自己对重复加载做了处理
     * 即重复加载同一个 Url 时,停止还在请求当中的进度,再开启新的加载
     */
    private void glideStart() {
        GlideApp.with(this)
                .load(mImageUrl)
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得释放引用
        mImageUrl = null;
        mDownloadUrl = null;
        mUploadUrl = null;
    }

    public static File writeToFile(InputStream in, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int num = 0;
        while ((num = in.read(buf)) != -1) {
            out.write(buf, 0, buf.length);
        }
        out.close();
        return file;
    }
}
