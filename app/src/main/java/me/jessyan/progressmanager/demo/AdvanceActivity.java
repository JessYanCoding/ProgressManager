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

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ================================================
 * 这里为了展示本框架的高级功能,使用同一个 url 地址根据 Post 请求参数的不同而下载或上传不同的资源
 *
 * @see {@link #initListener}
 * Created by JessYan on 08/06/2017 12:59
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class AdvanceActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AdvanceActivity";
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
    private String mNewImageUrl;
    private String mNewDownloadUrl;
    private String mNewUploadUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOkHttpClient = ((BaseApplication) getApplicationContext()).getOkHttpClient();
        mHandler = new Handler();
        initView();
        initListener();
        //在 Activity 中显示进度条的同时,也在 Fragment 中显示对应 url 的进度条,为了展示此框架的多端同步更新某一个进度信息
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                AdvanceFragment.newInstance(mNewImageUrl, mNewDownloadUrl, mNewUploadUrl)).commit();
    }


    private void initView() {
        setContentView(R.layout.activity_advance);
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
    }

    private void initListener() {
        //图片和下载 (上传也同样支持) 使用同一个 url 地址,是为了展示高级功能
        //高级功能是为了应对当需要使用同一个 url 地址根据 Post 请求参数的不同而下载或上传不同资源的情况
        //"http://jessyancoding.github.io/images/RxCache.png" 会重定向到 "http://jessyan.me/images/RxCache.png"
        //所以也展示了高级功能同时完美兼容重定向
        //这里需要注意的是虽然使用的是新的 url 地址进行上传或下载,但实际请求服务器的 url 地址,还是原始的 url 地址
        //在监听器内部已经进行了处理,所以高级功能并不会影响服务器的请求

        //Glide 加载监听
        mNewImageUrl = ProgressManager
                .getInstance()
                .addDiffResponseListenerOnSameUrl("http://jessyancoding.github.io/images/RxCache.png", getGlideListener());


        //Okhttp/Retofit 下载监听
        mNewDownloadUrl = ProgressManager
                .getInstance()
                .addDiffResponseListenerOnSameUrl("http://jessyancoding.github.io/images/RxCache.png", getDownloadListener());


        //Okhttp/Retofit 上传监听
        mNewUploadUrl = ProgressManager
                .getInstance()
                .addDiffRequestListenerOnSameUrl("http://upload.qiniu.com/", "test", getUploadListener());
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
                            .url(mNewUploadUrl)
                            .post(RequestBody.create(MediaType.parse("multipart/form-data"), file))
                            .build();

                    Response response = mOkHttpClient.newCall(request).execute();
                    response.body();
                } catch (IOException e) {
                    e.printStackTrace();
                    //当外部发生错误时,使用此方法可以通知所有监听器的 onError 方法
                    ProgressManager.getInstance().notifyOnErorr(mNewUploadUrl, e);
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
                            .url(mNewDownloadUrl)
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
                    ProgressManager.getInstance().notifyOnErorr(mNewDownloadUrl, e);
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
                .load(mNewImageUrl)
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //记得释放引用
        mNewImageUrl = null;
        mNewDownloadUrl = null;
        mNewUploadUrl = null;
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
