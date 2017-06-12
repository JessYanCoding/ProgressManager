package me.jessyan.progressmanager.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.jessyan.progressmanager.ProgressInfo;
import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;

import static me.jessyan.progressmanager.demo.MainActivity.DOWNLOAD_URL;
import static me.jessyan.progressmanager.demo.MainActivity.IMAGE_URL;
import static me.jessyan.progressmanager.demo.MainActivity.UPLOAD_URL;

/**
 * Created by jess on 08/06/2017 12:59
 * Contact with jess.yan.effort@gmail.com
 */

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private ProgressBar mGlideProgress;
    private ProgressBar mDownloadProgress;
    private ProgressBar mUploadProgress;
    private TextView mGlideProgressText;
    private TextView mDownloadProgressText;
    private TextView mUploadProgressText;
    private View mRootView;

    private ProgressInfo mLastDownloadingInfo;
    private ProgressInfo mLastUploadingingInfo;
    private Handler mHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_main, container, false);
        return mRootView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
        initView();
        initData();
    }


    private void initView() {
        mGlideProgress = (ProgressBar) mRootView.findViewById(R.id.glide_progress);
        mDownloadProgress = (ProgressBar) mRootView.findViewById(R.id.download_progress);
        mUploadProgress = (ProgressBar) mRootView.findViewById(R.id.upload_progress);
        mGlideProgressText = (TextView) mRootView.findViewById(R.id.glide_progress_text);
        mDownloadProgressText = (TextView) mRootView.findViewById(R.id.download_progress_text);
        mUploadProgressText = (TextView) mRootView.findViewById(R.id.upload_progress_text);
    }

    private void initData() {
        //Glide 加载监听
        ProgressManager.getInstance().addResponseListener(IMAGE_URL, getGlideListener());

        //Okhttp/Retofit 下载监听
        ProgressManager.getInstance().addResponseListener(DOWNLOAD_URL, getDownloadListener());

        //Okhttp/Retofit 上传监听
        ProgressManager.getInstance().addRequestLisenter(UPLOAD_URL, getUploadListener());
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
                Log.d(TAG, mLastUploadingingInfo.getId() + "--upload--" + progress + " %");
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

    @NonNull
    private ProgressListener getGlideListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                int progress = (int) ((100 * progressInfo.getCurrentbytes()) / progressInfo.getContentLength());
                mGlideProgress.setProgress(progress);
                mGlideProgressText.setText(progress + "%");
                Log.d(TAG, progressInfo.getId() + "--glide--"+progress + " %");
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
}
