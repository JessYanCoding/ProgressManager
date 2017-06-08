package me.jessyan.progressmanager;

/**
 * Created by jess on 07/06/2017 12:09
 * Contact with jess.yan.effort@gmail.com
 */

public class ProgressInfo {
    private long currentbytes; //当前的进度大小
    private long contentLength; //总进度大小
    private long id; //如果同一个 Url 地址,上一次的上传或下载操作都还没结束,
                    //又开始了新的上传或下载操作(比如用户点击多次点击上传或下载同一个 Url 地址,当然你也可以在上层屏蔽掉用户的重复点击),
                    //此 id (请求开始时的时间)就变得尤为重要,用来区分正在执行的进度信息,因为是以请求开始时的时间作为 id ,所以值越大,说明该请求越新

    public ProgressInfo(long id) {
        this.id = id;
    }

    public void setCurrentbytes(long currentbytes) {
        this.currentbytes = currentbytes;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getCurrentbytes() {
        return currentbytes;
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getId() {
        return id;
    }
}
