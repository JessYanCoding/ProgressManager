package me.jessyan.progressmanager;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import me.jessyan.progressmanager.body.ProgressInfo;
import me.jessyan.progressmanager.body.ProgressRequestBody;
import me.jessyan.progressmanager.body.ProgressResponseBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ProgressManager 一行代码即可监听 App 中所有网络链接的上传以及下载进度,包括 Glide(需要将下载引擎切换为 Okhttp)的图片加载进度,
 * 基于 Okhttp Interceptor,所以使用前请确保你使用 Okhttp 或 Retrofit 进行网络请求
 * 实现原理类似 EventBus,你可在 App 中的任何地方,将多个监听器,以 Url 地址作为标识符,注册到本管理器
 * 当此 Url 地址存在下载或者上传的动作时,管理器会主动调用所有使用此 Url 地址注册过的监听器,达到多个模块的同步更新
 * Created by jess on 02/06/2017 18:37
 * Contact with jess.yan.effort@gmail.com
 */

public final class ProgressManager {
    //WeakHashMap会在java虚拟机回收内存时,找到没被使用的key,将此条目移除,所以不需要手动remove()
    private final Map<String, List<ProgressListener>> mRequestListeners = new WeakHashMap<>();
    private final Map<String, List<ProgressListener>> mResponseListeners = new WeakHashMap<>();
    private final Handler mHandler; //所有监听器在 Handler 中被执行,所以可以保证所有监听器在主线程中被执行
    private final Interceptor mInterceptor;
    private int mRefreshTime = DEFAULT_REFRESH_TIME; //进度刷新时间(单位ms),避免高频率调用

    private static volatile ProgressManager mProgressManager;

    public static final boolean DEPENDENCY_OKHTTP;
    public static final int DEFAULT_REFRESH_TIME = 150;


    static {
        boolean hasDependency;
        try {
            Class.forName("okhttp3.OkHttpClient");
            hasDependency = true;
        } catch (ClassNotFoundException e) {
            hasDependency = false;
        }
        DEPENDENCY_OKHTTP = hasDependency;
    }


    private ProgressManager() {
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return wrapResponseBody(chain.proceed(wrapRequestBody(chain.request())));
            }
        };
    }


    public static ProgressManager getInstance() {
        if (mProgressManager == null) {
            if (!DEPENDENCY_OKHTTP) { //使用本管理器必须依赖 Okhttp
                throw new IllegalStateException("Must be dependency Okhttp");
            }
            synchronized (ProgressManager.class) {
                if (mProgressManager == null) {
                    mProgressManager = new ProgressManager();
                }
            }
        }
        return mProgressManager;
    }


    /**
     * 设置 {@link ProgressListener#onProgress(ProgressInfo)} 每次被调用的间隔时间,单位毫秒
     *
     * @param refreshTime
     */
    public void setRefreshTime(int refreshTime) {
        this.mRefreshTime = refreshTime;
    }

    /**
     * 将需要被监听上传进度的 Url 注册到管理器,此操作请在页面初始化时进行,切勿多次注册同一个(内容相同)监听器
     *
     * @param url
     * @param listener 当此 Url 地址存在上传的动作时,此监听器将被调用
     */
    public void addRequestLisenter(String url, ProgressListener listener) {
        List<ProgressListener> progressListeners;
        synchronized (ProgressManager.class) {
            progressListeners = mRequestListeners.get(url);
            if (progressListeners == null) {
                progressListeners = new LinkedList<>();
                mRequestListeners.put(url, progressListeners);
            }
        }
        progressListeners.add(listener);
    }

    /**
     * 将需要被监听下载进度的 Url 注册到管理器,此操作请在页面初始化时进行,切勿多次注册同一个(内容相同)监听器
     *
     * @param url
     * @param listener 当此 Url 地址存在下载的动作时,此监听器将被调用
     */
    public void addResponseListener(String url, ProgressListener listener) {
        List<ProgressListener> progressListeners;
        synchronized (ProgressManager.class) {
            progressListeners = mResponseListeners.get(url);
            if (progressListeners == null) {
                progressListeners = new LinkedList<>();
                mResponseListeners.put(url, progressListeners);
            }
        }
        progressListeners.add(listener);
    }


    /**
     * 当在 {@link ProgressRequestBody} 和 {@link ProgressResponseBody} 内部处理二进制流时发生错误
     * 会主动调用 {@link ProgressListener#onError(long, Exception)},但是有些错误并不是在它们内部发生的
     * 但同样会引起网络请求的失败,所以向外面提供{@link ProgressManager#notifyOnErorr},当外部发生错误时
     * 手动调用此方法,以通知所有的监听器
     *
     * @param url
     * @param e
     */
    public void notifyOnErorr(String url, Exception e) {
        forEachListenersOnError(mRequestListeners, url, e);
        forEachListenersOnError(mResponseListeners, url, e);
    }

    /**
     * 将 {@link okhttp3.OkHttpClient.Builder} 传入,配置一些本管理器需要的参数
     *
     * @param builder
     * @return
     */
    public OkHttpClient.Builder with(OkHttpClient.Builder builder) {
        return builder
                .addNetworkInterceptor(mInterceptor);
    }

    /**
     * 将 {@link Request} 传入,配置一些本框架需要的参数,常用于自定义 {@link Interceptor}
     * 如已使用 {@link ProgressManager#with(OkHttpClient.Builder)},就不会用到此方法
     *
     * @param request
     * @return
     */
    public Request wrapRequestBody(Request request) {
        if (request == null || request.body() == null)
            return request;

        String key = request.url().toString();
        if (mRequestListeners.containsKey(key)) {
            List<ProgressListener> listeners = mRequestListeners.get(key);
            return request.newBuilder()
                    .method(request.method(), new ProgressRequestBody(mHandler, request.body(), listeners, mRefreshTime))
                    .build();
        }
        return request;
    }

    /**
     * 将 {@link Response} 传入,配置一些本框架需要的参数,常用于自定义 {@link Interceptor}
     * 如已使用 {@link ProgressManager#with(OkHttpClient.Builder)},就不会用到此方法
     *
     * @param response
     * @return
     */
    public Response wrapResponseBody(Response response) {
        if (response == null || response.body() == null)
            return response;

        if (haveRedirect(response)) {
            resolveRedirect(mRequestListeners, response);
            resolveRedirect(mResponseListeners, response);
            return response;
        }

        String key = response.request().url().toString();
        if (mResponseListeners.containsKey(key)) {
            List<ProgressListener> listeners = mResponseListeners.get(key);
            return response.newBuilder()
                    .body(new ProgressResponseBody(mHandler, response.body(), listeners, mRefreshTime))
                    .build();
        }
        return response;
    }

    /**
     * 是否需要重定向
     *
     * @param response
     * @return
     */
    private boolean haveRedirect(Response response) {
        String status = response.header("Status");
        if (status.contains("301") || status.contains("302") || status.contains("303") || status.contains("307")) {
            return true;
        }
        return false;
    }

    /**
     * 使重定向后,也可以监听进度
     *
     * @param map
     * @param response
     */
    private void resolveRedirect(Map<String, List<ProgressListener>> map, Response response) {
        String url = response.request().url().toString();
        List<ProgressListener> progressListeners = map.get(url); //查看此重定向 url ,是否已经注册过监听器
        if (progressListeners != null) {
            String location = response.header("Location");// 重定向地址
            if (!TextUtils.isEmpty(location) && !map.containsKey(location)) {
                map.put(location, progressListeners); //将需要重定向地址的监听器,提供给重定向地址,保证重定向后也可以监听进度
            }
        }
    }


    private void forEachListenersOnError(Map<String, List<ProgressListener>> map, String url, Exception e) {
        if (map.containsKey(url)) {
            List<ProgressListener> progressListeners = map.get(url);
            ProgressListener[] array = progressListeners.toArray(new ProgressListener[progressListeners.size()]);
            for (int i = 0; i < array.length; i++) {
                array[i].onError(-1, e);
            }
        }
    }

}
