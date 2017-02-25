package ca.jeffrey.apodgallery;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyTaskService extends GcmTaskService {
    public static final String TAG_TASK_DAILY = "tag_task_daily";
    public static final String TAG_TASK_ONEOFF = "tag_oneoff";
    public static final String TAG_TASK_MINUTELY = "tag_minutely";

    private void getImageData() {
        String url = "https://api.nasa.gov/planetary/apod?api_key=" + MainActivity.API_KEY;

        doJsonRequest(url);
    }

    private void doJsonRequest(String url) {
        OkHttpClient client;
        // Initialize OkHttp client and cache
        client = new OkHttpClient.Builder().cache(new Cache(getCacheDir(), 10 * 1024 * 1024)) // 10M
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        request = request.newBuilder().header("Cache-Control", "public, " +
                                "max-age=" + 60).build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();

        // Build request
        Request request = new Request.Builder().cacheControl(new CacheControl.Builder()
                .onlyIfCached().build()).url(url).build();
        // Request call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String res = response.body().string();

                // Parse JSON object

                try {
                    JSONObject object = new JSONObject(res);
                    onJsonResponse(object);

                }
                // Error handling
                catch (JSONException e) {
                    e.printStackTrace();

                    int code = response.code();

                    switch (code) {
                        // Server error
                        case 400:
                            // Too early
                        case 500:
                            // Too early
                            // /break;
                        // Client-side network error
                        case 504:
                            // break;
                        // Default server error
                        default:
                    }
                }
                catch (Exception e) {

                }
            }
        });
    }

    private void onJsonResponse(JSONObject response) throws JSONException, ExecutionException, InterruptedException, IOException {
        final String IMAGE_TYPE = "image";
        String mediaType;
        String sdUrl;
        String hdUrl;

        mediaType = response.getString("media_type");
        sdUrl = response.getString("url").replaceAll("http://", "https://");
        hdUrl = response.getString("hdurl").replaceAll("http://", "https://");

        if (mediaType.equals(IMAGE_TYPE)) {
            Bitmap result = Glide.with(this).load(sdUrl)
                    .asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
            WallpaperManager manager = WallpaperManager.getInstance(this);
            manager.setBitmap(result);
        }
    }


    @Override
    public int onRunTask(TaskParams taskParams) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        switch (taskParams.getTag()) {
            case TAG_TASK_DAILY:
                int count = sharedPreferences.getInt(TAG_TASK_MINUTELY, 0) + 1;
                sharedPreferences.edit().putInt(TAG_TASK_MINUTELY, count).apply();
                getImageData();
                return GcmNetworkManager.RESULT_SUCCESS;
            case TAG_TASK_ONEOFF:
                String url = "http://androidwalls.net/wp-content/uploads/2017/01/San%20Francisco%20Golden%20Gate%20Bridge%20Fog%20Lights%20Android%20Wallpaper.jpg";
                try {
                    Log.i("ONE_OFF", "Reached");
                    // Bitmap result = Glide.with(this).load(url)
                    //         .asBitmap().into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
                    // WallpaperManager manager = WallpaperManager.getInstance(this);
                    // manager.setBitmap(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return GcmNetworkManager.RESULT_SUCCESS;
            default:
                return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
    }
}