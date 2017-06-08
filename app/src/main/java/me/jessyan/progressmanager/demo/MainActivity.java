package me.jessyan.progressmanager.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.jessyan.progressmanager.ProgressInfo;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    // github 服务器可能下载不稳定
    public static final String IMAGE_URL = "https://raw.githubusercontent.com/JessYanCoding/MVPArmsTemplate/master/art/step.png";
    public static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/JessYanCoding/MVPArmsTemplate/master/art/MVPArms.gif";
    public static final String UPLOAD_URL = "http://upload.qiniu.com/";

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOkHttpClient = ((BaseApplication) getApplicationContext()).getOkHttpClient();
        initView();
        initListener();
        //在 Activity 中显示进度条的同时,也在 Fragment 中显示,为了展示此框架的多端同步更新某一个进度信息
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new MainFragment()).commit();
    }


    private void initView() {
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mGlideProgress = (ProgressBar) findViewById(R.id.glide_progress);
        mDownloadProgress = (ProgressBar) findViewById(R.id.download_progress);
        mUploadProgress = (ProgressBar) findViewById(R.id.upload_progress);
        mGlideProgressText = (TextView) findViewById(R.id.glide_progress_text);
        mDownloadProgressText = (TextView) findViewById(R.id.download_progress_text);
        mUploadProgressText = (TextView) findViewById(R.id.upload_progress_text);
        findViewById(R.id.glide_start).setOnClickListener(this);
        findViewById(R.id.download_start).setOnClickListener(this);
        findViewById(R.id.upload_start).setOnClickListener(this);
    }

    private void initListener() {
        //Glide 下载监听
        ProgressManager.getInstance().addResponseListener(IMAGE_URL, getGlideListener());


        //Okhttp/Retofit 下载监听
        ProgressManager.getInstance().addResponseListener(DOWNLOAD_URL, getDownloadListener());


        //Okhttp/Retofit 上传监听
        ProgressManager.getInstance().addRequestLisenter(UPLOAD_URL, getUploadListener());

    }








    @NonNull
    private ProgressListener getGlideListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                int progress = (int) ((100 * progressInfo.getCurrentbytes()) / progressInfo.getContentLength());
                mGlideProgress.setProgress(progress);
                mGlideProgressText.setText(progress + "%");
                Log.d(TAG, progressInfo.getId() + "--glide--" + progress + " %");
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
                // 这里我就取最新的上传操作用来展示,顺便展示下 id 的用法

                if (mLastUploadingingInfo == null) {
                    mLastUploadingingInfo = progressInfo;
                }

                //因为是以请求开始时的时间作为 Id ,所以值越大,说明该请求越新
                if (progressInfo.getId() < mLastUploadingingInfo.getId()) {
                    return;
                } else if (progressInfo.getId() > mLastUploadingingInfo.getId()) {
                    mLastUploadingingInfo = progressInfo;
                }


                int progress = (int) ((100 * mLastUploadingingInfo.getCurrentbytes()) / mLastUploadingingInfo.getContentLength());
                mUploadProgress.setProgress(progress);
                mUploadProgressText.setText(progress + "%");
                Log.d(TAG, mLastUploadingingInfo.getId() + "--upload--" + progress + " %  " +mLastUploadingingInfo.getCurrentbytes() +"  "+mLastUploadingingInfo.getContentLength());
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
                // 这里我就取最新的下载操作用来展示,顺便展示下 id 的用法

                if (mLastDownloadingInfo == null) {
                    mLastDownloadingInfo = progressInfo;
                }

                //因为是以请求开始时的时间作为 Id ,所以值越大,说明该请求越新
                if (progressInfo.getId() < mLastDownloadingInfo.getId()) {
                    return;
                } else if (progressInfo.getId() > mLastDownloadingInfo.getId()) {
                    mLastDownloadingInfo = progressInfo;
                }

                int progress = (int) ((100 * mLastDownloadingInfo.getCurrentbytes()) / mLastDownloadingInfo.getContentLength());
                mDownloadProgress.setProgress(progress);
                mDownloadProgressText.setText(progress + "%");
                Log.d(TAG, mLastDownloadingInfo.getId() + "--download--" + progress + " %");
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
        File file = new File(getCacheDir(), "a.java");
        try {
            //读取Assets里面的数据,作为上传源数据
            writeToFile(getAssets().open("a.java"), file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        final Request request = new Request.Builder()
                .url(UPLOAD_URL)
                .post(RequestBody.create(MediaType.parse("multipart/form-data"), file))
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = mOkHttpClient.newCall(request).execute();
                    response.body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 点击开始下载资源,为了演示,就不做重复点击的处理,即允许用户在还有进度没完成的情况下,使用同一个 url 开始新的下载
     */
    private void downloadStart() {
        final Request request = new Request.Builder()
                .url(DOWNLOAD_URL)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = mOkHttpClient.newCall(request).execute();
                    System.out.println(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 点击开始 Glide 加载图片,为了演示,就不做重复点击的处理,但是 Glide 自己对重复加载做了处理
     * 即重复加载同一个 Url 时,停止还在请求当中的进度,再开启新的加载
     */
    private void glideStart() {
        Glide.with(this)
                .load(IMAGE_URL)
                .centerCrop()
                .placeholder(R.color.colorPrimary)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mImageView);
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
