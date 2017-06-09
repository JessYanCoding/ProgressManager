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

/**
 * 继承于{@link ResponseBody},通过此类获取 Okhttp 下载的二进制数据
 * Created by jess on 02/06/2017 18:25
 * Contact with jess.yan.effort@gmail.com
 */

public class ProgressResponseBody extends ResponseBody {

    private Handler mHandler;
    private final ResponseBody mDelegate;
    private final ProgressListener[] mListeners;
    private BufferedSource bufferedSource;
    protected ProgressInfo mProgressInfo;

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
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(mDelegate.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                if (mListeners != null) {
                    mProgressInfo.setCurrentbytes(totalBytesRead);
                    mProgressInfo.setContentLength(contentLength());
                    for (int i = 0; i < mListeners.length; i++) {
                        final int finalI = i;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mListeners[finalI].onProgress(mProgressInfo);
                            }
                        });
                    }

                }
                return bytesRead;
            }
        };
    }
}
