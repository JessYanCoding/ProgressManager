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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;

/**
 * ================================================
 * Created by JessYan on 08/06/2017 12:59
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
public class AdvanceFragment extends Fragment {
    private static final String TAG = "AdvanceFragment";

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
    private static final String URL_BUNDLE_KEY = "url_bundle_key";

    public static AdvanceFragment newInstance(String imageUrl, String downloadUrl, String uploadUrl) {
        AdvanceFragment fragment = new AdvanceFragment();
        Bundle bundle = new Bundle();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(imageUrl, downloadUrl, uploadUrl));
        bundle.putStringArrayList(URL_BUNDLE_KEY, list);
        fragment.setArguments(bundle);
        return fragment;
    }

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
        mGlideProgress = mRootView.findViewById(R.id.glide_progress);
        mDownloadProgress = mRootView.findViewById(R.id.download_progress);
        mUploadProgress = mRootView.findViewById(R.id.upload_progress);
        mGlideProgressText = mRootView.findViewById(R.id.glide_progress_text);
        mDownloadProgressText = mRootView.findViewById(R.id.download_progress_text);
        mUploadProgressText = mRootView.findViewById(R.id.upload_progress_text);
    }

    private void initData() {
        Bundle arguments = getArguments();
        ArrayList<String> list = arguments.getStringArrayList(URL_BUNDLE_KEY);
        if (list == null || list.isEmpty())
            return;
        //Glide 加载监听
        ProgressManager.getInstance().addResponseListener(list.get(0), getGlideListener());

        //Okhttp/Retofit 下载监听
        ProgressManager.getInstance().addResponseListener(list.get(1), getDownloadListener());

        //Okhttp/Retofit 上传监听
        ProgressManager.getInstance().addRequestListener(list.get(2), getUploadListener());
        list.clear(); //清理 list 的引用
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
                Log.d(TAG, "--Upload-- " + progress + " %");
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
                Log.d(TAG, "--Download-- " + progress + " %");
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
                int progress = progressInfo.getPercent();
                mGlideProgress.setProgress(progress);
                mGlideProgressText.setText(progress + "%");
                Log.d(TAG, "--Glide-- " + progress + " %");
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
