package me.jessyan.progressmanager.body;

import android.os.Handler;

import java.io.IOException;
import java.util.List;

import me.jessyan.progressmanager.ProgressInfo;
import me.jessyan.progressmanager.ProgressListener;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 继承于{@link RequestBody},通过此类获取 Okhttp 上传的二进制数据
 * Created by jess on 02/06/2017 18:05
 * Contact with jess.yan.effort@gmail.com
 */

public class ProgressRequestBody extends RequestBody {

    protected Handler mHandler;
    protected final RequestBody mDelegate;
    protected final ProgressListener[] mListeners;
    protected final ProgressInfo mProgressInfo;
    private BufferedSink mBufferedSink;

    public ProgressRequestBody(Handler handler, RequestBody delegate, List<ProgressListener> listeners) {
        this.mDelegate = delegate;
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
        try {
            return mDelegate.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (mBufferedSink == null) {
            mBufferedSink = Okio.buffer(new CountingSink(sink));
        }
        mDelegate.writeTo(mBufferedSink);
        mBufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {
        private long totalBytesRead = 0L;
        private long contentLength = 0L;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            if (contentLength == 0){ //避免重复调用 contentLength()
                contentLength = contentLength();
            }
            totalBytesRead += byteCount;
            if (mListeners != null) {
                mProgressInfo.setCurrentbytes(totalBytesRead);
                mProgressInfo.setContentLength(contentLength);
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
        }
    }
}
