package me.jessyan.progressmanager.body;

import android.os.Handler;

import java.io.IOException;
import java.util.List;

import me.jessyan.progressmanager.ProgressInfo;
import me.jessyan.progressmanager.ProgressListener;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

import static me.jessyan.progressmanager.ProgressManager.REFRESH_TIME;

/**
 * 继承于{@link ResponseBody},通过此类获取 Okhttp 下载的二进制数据
 * Created by jess on 02/06/2017 18:25
 * Contact with jess.yan.effort@gmail.com
 */

public class ProgressResponseBody extends ResponseBody {

    protected Handler mHandler;
    protected final ResponseBody mDelegate;
    protected final ProgressListener[] mListeners;
    protected final ProgressInfo mProgressInfo;
    private BufferedSource mBufferedSource;

    public ProgressResponseBody(Handler handler, ResponseBody responseBody, List<ProgressListener> listeners) {
        this.mDelegate = responseBody;
        this.mListeners = listeners.toArray(new ProgressListener[listeners.size()]);
        this.mHandler = handler;
        this.mProgressInfo = new ProgressInfo(System.currentTimeMillis());
    }

    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    @Override
    public long contentLength() {
        return mDelegate.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mDelegate.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            private long totalBytesRead = 0L;
            private long lastRefreshTime = 0L;  //最后一次刷新的时间

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = 0L;
                try {
                    bytesRead = super.read(sink, byteCount);
                } catch (IOException e) {
                    e.printStackTrace();
                    for (int i = 0; i < mListeners.length; i++) {
                        mListeners[i].onError(mProgressInfo.getId(), e);
                    }
                    throw e;
                }
                if (mProgressInfo.getContentLength() == 0) { //避免重复调用 contentLength()
                    mProgressInfo.setContentLength(contentLength());
                }
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (mListeners != null) {
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastRefreshTime >= REFRESH_TIME || totalBytesRead == mProgressInfo.getContentLength()) {
                        mProgressInfo.setCurrentbytes(totalBytesRead);
                        for (int i = 0; i < mListeners.length; i++) {
                            final int finalI = i;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mListeners[finalI].onProgress(mProgressInfo);
                                }
                            });
                        }
                        lastRefreshTime = System.currentTimeMillis();
                    }
                }
                return bytesRead;
            }
        };
    }
}
