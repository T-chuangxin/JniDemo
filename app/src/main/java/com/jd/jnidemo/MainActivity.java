package com.jd.jnidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jd.jnidemo.bean.RepoJson;
import com.jd.jnidemo.bean.ReposListJson;
import com.jd.jnidemo.flat.Repo;
import com.jd.jnidemo.flat.ReposList;
import com.jd.jnidemo.utils.RawDataReader;
import com.jd.jnidemo.utils.SimpleObserver;

import java.nio.ByteBuffer;

import rx.Observable;
import rx.functions.Func2;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private TextView costTime;
    private TextView content;
    private ReposList reposListFlatParsed;
    private RawDataReader rawDataReader;
    private ReposListJson reposListJson;


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    private native byte[] parseJsonNative(String json, String schema);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();


    }

    /**
     * 页面控件初始化
     */
    private void initView() {
        rawDataReader = new RawDataReader(this);
        costTime = (TextView) findViewById(R.id.tv_cost_time);
        content = (TextView) findViewById(R.id.tv_content);





    }


    /**
     * json解析
     * @param view
     */
    public void jsonParse(View view) {
        rawDataReader.loadString(R.raw.repos_json).subscribe(new SimpleObserver<String>() {
            @Override
            public void onNext(String reposStr) {
                parseReposListJson(reposStr);
            }
        });


    }

    /**
     * flat解析
     * @param view
     */
    public void flatParse(View view) {

        Observable.combineLatest(
                rawDataReader.loadString(R.raw.repos_json),
                rawDataReader.loadString(R.raw.repos_schema),
                new Func2<String, String, Object>() {
                    @Override
                    public Object call(String json, String schema) {
                        parseWithFlatBuffers(json, schema);
                        return reposListFlatParsed;
                    }
                }
        ).subscribe();

    }


    /**
     *
     * @param json
     * @param schema
     */
    private void parseWithFlatBuffers(String json, String schema) {
        long startTime = System.currentTimeMillis();

        ByteBuffer byteBuffer = ByteBuffer.wrap(parseJsonNative(json, schema));
        reposListFlatParsed = ReposList.getRootAsReposList(byteBuffer);
        for (int i = 0; i < reposListFlatParsed.reposLength(); i++) {
            Repo repos = reposListFlatParsed.repos(i);
            Log.d("FlatBuffers", "Repo #" + i + ", id: " + repos.id()+",name:"+repos.name());
        }
        long endTime = System.currentTimeMillis() - startTime;
        costTime.setText("Elements: " + reposListFlatParsed.reposLength() + ": load time: " + endTime + "ms");
    }

    /**
     * gson解析数据
     * @param reposStr
     */
    private void parseReposListJson(String reposStr) {
        long startTime = System.currentTimeMillis();
        reposListJson = new Gson().fromJson(reposStr, ReposListJson.class);
        for (int i = 0; i < reposListJson.repos.size(); i++) {
            RepoJson repo = reposListJson.repos.get(i);
            Log.d("FlatBuffers", "Repo #" + i + ", id: " + repo.id);
        }
        long endTime = System.currentTimeMillis() - startTime;
        costTime.setText("Elements: " + reposListJson.repos.size() + ": load time: " + endTime + "ms");
    }
}
