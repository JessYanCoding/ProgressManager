package me.jessyan.progressmanager.demo;

import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a.java href="http://d.android.com/tools/testing">Testing documentation</a.java>
 */
public class ExampleUnitTest {

    @Test
    public void test(){
//        testWeakHashMapAPIs();
        testString();
    }

    private void testRequest(){
        String newUrl = ProgressManager.getInstance().addDiffResponseListenerOnSameUrl("http://www.baidu.com/user", new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {

            }

            @Override
            public void onError(long id, Exception e) {

            }
        });
        Request request = new Request
                .Builder()
                .url(newUrl)
                .build();
        ProgressManager.getInstance().wrapRequestBody(request);
    }

    private void testString(){
        String s =  "http://www.baidu.com/user$JessYan$456";
        String substring = s.substring(0, s.indexOf("$JessYan$"));
        System.out.println(HttpUrl.parse("http://www.baidu.com/abc/d").toString());
        System.out.println(substring);
        System.out.println(s);
    }

    private void testWeakHashMapAPIs() {
        // 初始化3个“弱键”
        String w1 = new String("one");
        String w2 = new String("two");
        String w3 = new String("three");
        // 新建WeakHashMap
        Map wmap = new WeakHashMap();

        // 添加键值对
        wmap.put(w1, "w1");
        wmap.put(w2, "w2");
        wmap.put(w3, "w3");

        // 打印出wmap
        System.out.printf("\nwmap:%s\n",wmap );

        // remove(Object key) ： 删除键key对应的键值对
        wmap.remove("three");

        System.out.printf("wmap: %s\n",wmap );


        // ---- 测试 WeakHashMap 的自动回收特性 ----

        // 将w1设置null。
        // 这意味着“弱键”w1再没有被其它对象引用，调用gc时会回收WeakHashMap中与“w1”对应的键值对
        w1 = null;
        // 内存回收。这里，会回收WeakHashMap中与“w1”对应的键值对
        System.gc();

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 遍历WeakHashMap
        Iterator iter = wmap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry en = (Map.Entry)iter.next();
            System.out.printf("next : %s - %s\n",en.getKey(),en.getValue());
        }
        // 打印WeakHashMap的实际大小
        System.out.printf(" after gc WeakHashMap size:%s\n", wmap.size());
    }
}