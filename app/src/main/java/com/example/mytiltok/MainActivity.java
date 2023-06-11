package com.example.mytiltok;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static List<String> video_path;
    public static List<Uri> video_uri;

    public static Map<String, List<String>> sDirListMap;
    public static Map<String, List<Uri>> sUriListMap;
    public static Map<String, List<Bitmap>> sBitMapListMap;
    public List<Bitmap> mBitMapList;

    public static List<String> sDirList;
    public List<Uri> mUriList;
    public static boolean isDone;

    Button start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        video_path=new ArrayList<>();
        video_uri=new ArrayList<>();
        //checkPermission
        int permission = ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.READ_MEDIA_VIDEO","DatabaseProvider._WRITE_PERMISSION","DatabaseProvider._READ_PERMISSION","android.permission.READ_MEDIA_IMAGES"}, 1);
        }
        else{
            Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = getBaseContext().getContentResolver();
            Cursor mCursor = mContentResolver.query(mVideoUri, new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.MIME_TYPE}, null, null, MediaStore.Video.Media.DATE_ADDED);
            if (mCursor != null && mCursor.moveToFirst()) {
                int idColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int dataColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int mimeTypeColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);

                do {
                    long id = mCursor.getLong(idColumnIndex);
                    String data = mCursor.getString(dataColumnIndex);
                    String mimeType = mCursor.getString(mimeTypeColumnIndex);
                    video_path.add(data);
                    Uri videoUri = ContentUris.withAppendedId(mVideoUri, id);
                    video_uri.add(videoUri);
//                    String thumbnailPath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
//                    Log.println(Log.ASSERT,"thumbnailPath",thumbnailPath);
                    //Bitmap thumbnail = BitmapFactory.decodeFile(thumbnailPath);
                    // Do something with the videoUri, data, and mimeType.
                } while (mCursor.moveToNext());

                mCursor.close();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isDone = false;
                    sDirListMap = new HashMap<>();
                    sDirList = new ArrayList<>();
                    sUriListMap = new HashMap<>();
                    mUriList = new ArrayList<>();
                    sBitMapListMap = new HashMap<>();
                    mBitMapList = new ArrayList<>();
                    ContentResolver contentResolver = getContentResolver();
                    String[] projection = {MediaStore.Video.Thumbnails.DATA};
                    for (int i = 0; i < MainActivity.video_path.size(); i++) {
                        String[] index = MainActivity.video_path.get(i).split("/");
                        String dirname = index[index.length - 2];
                        List<String> list;
                        List<Uri> uriList;
                        List<Bitmap> bitmapList;
                        if (!sDirListMap.containsKey(dirname)) {
                            list = new ArrayList();
                            uriList = new ArrayList<>();
                            bitmapList = new ArrayList<>();
                            list.add(MainActivity.video_path.get(i));
                            uriList.add(MainActivity.video_uri.get(i));
                            Cursor cursor = contentResolver.query(MainActivity.video_uri.get(i), projection, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                String thumbnailPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
                                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());

                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(thumbnailPath)
                                        .centerCrop()
                                        .override(width, height)
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                // Do something with the loaded and resized bitmap
                                                bitmapList.add(resource);
                                            }
                                        });
                            }
                            cursor.close();
                            sDirListMap.put(dirname, list);
                            sUriListMap.put(dirname, uriList);
                            sBitMapListMap.put(dirname, bitmapList);
                            sDirList.add(dirname);
                        } else {
                            list = sDirListMap.get(dirname);
                            uriList = sUriListMap.get(dirname);
                            bitmapList = sBitMapListMap.get(dirname);
                            list.add(MainActivity.video_path.get(i));
                            uriList.add(MainActivity.video_uri.get(i));
                            Cursor cursor = contentResolver.query(MainActivity.video_uri.get(i), projection, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                String thumbnailPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
                                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());

                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(thumbnailPath)
                                        .centerCrop()
                                        .override(width, height)
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                // Do something with the loaded and resized bitmap
                                                bitmapList.add(resource);
                                            }
                                        });
                            }
                            cursor.close();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                sDirListMap.replace(dirname, list);
                                sUriListMap.replace(dirname, uriList);
                                sBitMapListMap.replace(dirname, bitmapList);
                            }
                        }
                    }
                    isDone = true;
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"处理完成,可以选择文件夹了",Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }).start();
            startActivity(new Intent(MainActivity.this, ActivityTikTok.class));

        }
        start = (Button)findViewById(R.id.btn_main);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(video_path.isEmpty())
                {
                    Toast.makeText(MainActivity.this, "未检索到视频文件，请检查", Toast.LENGTH_SHORT).show();
                }else {
                    startActivity(new Intent(MainActivity.this, ActivityTikTok.class));
                }
                //startActivity(new Intent(MainActivity.this, LocalVideoActivity.class));
            }
        });

        start.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(MainActivity.this, LocalVideoActivity.class));
                return true;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==0){
            for (int i = 0; i < permissions.length; i++)
            {
                if (grantResults[i]!=-1){
                    Uri mVideoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver mContentResolver = getBaseContext().getContentResolver();
                    Cursor mCursor = mContentResolver.query(mVideoUri, new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA, MediaStore.Video.Media.MIME_TYPE}, null, null, MediaStore.Video.Media.DATE_ADDED);
                    if (mCursor != null && mCursor.moveToFirst()) {
                        int idColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                        int dataColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                        int mimeTypeColumnIndex = mCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);

                        do {
                            long id = mCursor.getLong(idColumnIndex);
                            String data = mCursor.getString(dataColumnIndex);
                            String mimeType = mCursor.getString(mimeTypeColumnIndex);
                            video_path.add(data);
                            Uri videoUri = ContentUris.withAppendedId(mVideoUri, id);
                            video_uri.add(videoUri);
                            // Do something with the videoUri, data, and mimeType.
                        } while (mCursor.moveToNext());

                        mCursor.close();
                    }
                    startActivity(new Intent(MainActivity.this, ActivityTikTok.class));

                }else {
                    //T.showShort(mContext,"拒绝权限");
                    // 权限被拒绝，弹出dialog 提示去开启权限
                    break;
                }
            }

        }
    }
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            //Toast.makeText(this, "你确定要退出吗", Toast.LENGTH_SHORT).show();
            if((System.currentTimeMillis()-exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else
            {
                finish();
                System.exit(0);
            }
            return true;// true 事件不继续传递， false 事件继续传递
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }
}