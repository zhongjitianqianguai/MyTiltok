package com.example.mytiltok;

import static com.example.mytiltok.MainActivity.isDone;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mytiltok.jzvd.JZDataSource;
import com.example.mytiltok.jzvd.Jzvd;
import com.example.mytiltok.jzvd.JzvdStdSpeed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityTikTok extends AppCompatActivity {

    private RecyclerView rvTiktok;
    private TikTokRecyclerViewAdapter mAdapter;
    private ViewPagerLayoutManager mViewPagerLayoutManager;
    private int mCurrentPosition = -1;
    public ActivityResultLauncher<IntentSenderRequest> mIntentSenderRequestActivityResultLauncher;

    public List<String> mDirList;

    private boolean isDel;

    public void updateUI(List<String> path, List<Uri> uri) {
        mAdapter = new TikTokRecyclerViewAdapter(this, path, uri);
        mViewPagerLayoutManager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        rvTiktok.setLayoutManager(mViewPagerLayoutManager);
        rvTiktok.setAdapter(mAdapter);
        mViewPagerLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {
                //自动播放第一条
                autoPlayVideo();
            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                if (mCurrentPosition == position && !isDel) {
                    Jzvd.releaseAllVideos();
                }

                Log.println(Log.ASSERT, "onPageRelease:position", String.valueOf(position));
                Log.println(Log.ASSERT, "onPageRelease:mCurrentPosition", String.valueOf(mCurrentPosition));

            }

            @Override
            public void onPageSelected(int position, boolean isBottom) {
                if (mCurrentPosition == position) {
                    return;
                }
                Log.println(Log.ASSERT, "onPageSelected", String.valueOf(position));

                autoPlayVideo();
                isDel = false;
                mCurrentPosition = position;
            }
        });
        rvTiktok.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                Jzvd jzvd = view.findViewById(R.id.videoplayer);
                if (jzvd != null && Jzvd.CURRENT_JZVD != null && jzvd.jzDataSource != null &&
                        jzvd.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.getCurrentUrl())) {
                    if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                        Jzvd.releaseAllVideos();
                    }
                }
            }
        });
    }

    public Bitmap cropBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int targetWidth = (int) (120 * getResources().getDisplayMetrics().density + 0.5f);
        int targetHeight = (int) (160 * getResources().getDisplayMetrics().density + 0.5f);

        int startX = (width - targetWidth) / 2;
        int startY = 0;

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, Math.max(0, startX), Math.min(0, startY), Math.min(width, targetWidth), Math.min(height, targetHeight));
        return croppedBitmap;
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap b = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            b = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();

        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return b;
    }

    public void delete(ActivityResultLauncher<IntentSenderRequest> launcher, Uri uri) {

        ContentResolver contentResolver = this.getContentResolver();

        try {

            //delete object using resolver
            int res = contentResolver.delete(uri, null, null);
            Log.println(Log.ASSERT, "res", String.valueOf(res));

        } catch (SecurityException e) {

            PendingIntent pendingIntent = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                ArrayList<Uri> collection = new ArrayList<>();
                collection.add(uri);
                pendingIntent = MediaStore.createDeleteRequest(contentResolver, collection);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //if exception is recoverable then again send delete request using intent
                if (e instanceof RecoverableSecurityException) {
                    RecoverableSecurityException exception = (RecoverableSecurityException) e;
                    pendingIntent = exception.getUserAction().getActionIntent();
                }
            }

            if (pendingIntent != null) {
                IntentSender sender = pendingIntent.getIntentSender();
                IntentSenderRequest request = new IntentSenderRequest.Builder(sender).build();
                launcher.launch(request);
            }


        }
    }

    class TikTokRecyclerViewAdapter extends RecyclerView.Adapter<TikTokRecyclerViewAdapter.MyViewHolder> {

        public static final String TAG = "AdapterTikTokRecyclerView";
        private int video_num = 1;
        private Context context;

        public List<String> paths;
        public List<Uri> uris;

        public TikTokRecyclerViewAdapter(Context context, List<String> path, List<Uri> uri) {
            this.context = context;
            paths = path;
            uris = uri;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tiktok, parent, false));

            return holder;
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            //Log.i(TAG, "onBindViewHolder [" + holder.jzvdStd.hashCode() + "] position=" + position);
            //String video_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/tiktok00"+String.valueOf(position+1)+".mp4";
            Log.println(Log.ASSERT, "position", String.valueOf(position));
            JZDataSource jzDataSource = new JZDataSource(paths.get(Math.min(position, paths.size() - 1)));
            jzDataSource.looping = true;
            holder.jzvdStd.setUp(jzDataSource, Jzvd.SCREEN_NORMAL);
            holder.bind(paths.get(Math.min(position, paths.size() - 1)));
            Glide.with(holder.jzvdStd.getContext()).load("").into(holder.jzvdStd.posterImageView);
        }

        @Override
        public int getItemCount() {
            return paths.size();
        }

        private void dirListDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("选择您要刷的文件夹");
            builder.setCancelable(true);
            String[] dir = MainActivity.sDirList.toArray(new String[0]);
            builder.setIcon(R.drawable.api);
            builder.setIcon(R.drawable.api)
                    .setItems(dir, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String path = dir[which];
                            startActivity(new Intent(ActivityTikTok.this, ChooseActivity.class).putExtra("path", path));
                            finish();
                        }
                    }).create();

            //设置反面按钮
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();     //创建AlertDialog对象
            dialog.show();                              //显示对话框
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            JzvdStdSpeed jzvdStd;
            ImageView mImageView;
            TextView mDirTextView;
            TextView mFileTextView;


            public MyViewHolder(View itemView) {
                super(itemView);
                jzvdStd = itemView.findViewById(R.id.videoplayer);
                mImageView = itemView.findViewById(R.id.del);
                mDirTextView = itemView.findViewById(R.id.dirname);
                mFileTextView = itemView.findViewById(R.id.filename);
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delete(mIntentSenderRequestActivityResultLauncher, uris.get(getAdapterPosition()));
                    }
                });
                mDirTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isDone)
                            dirListDialog();
                        else
                            Toast.makeText(getApplicationContext(), "还未扫描加载完成，请稍后再试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void bind(String path) {
                mFileTextView.setText(paths.indexOf(path) + 1 + " / " + getItemCount());
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//
//            hideStatusBar();
//        }
        setContentView(R.layout.activity_tiktok);
        rvTiktok = findViewById(R.id.rv_tiktok);
        //tv_my = findViewById(R.id.tv_mine);
        isDel = false;
        String path=getIntent().getStringExtra("path");
        int position=getIntent().getIntExtra("position",0);
        if (path==null)
            updateUI(MainActivity.video_path, MainActivity.video_uri);
        else {
            updateUI(MainActivity.sDirListMap.get(path), MainActivity.sUriListMap.get(path));
            rvTiktok.scrollToPosition(position);
            mCurrentPosition=position;
        }
        mIntentSenderRequestActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            Log.println(Log.ASSERT, "recode", String.valueOf(result.getResultCode()));
            if (result.getResultCode() == -1) {
                mAdapter.notifyItemRemoved(mCurrentPosition);
                mAdapter.notifyDataSetChanged();
                Log.println(Log.ASSERT, "111", String.valueOf(mCurrentPosition));
                if (mAdapter.paths.size() > mCurrentPosition + 1) {
                    rvTiktok.scrollToPosition(mCurrentPosition+1);
//                    mAdapter.paths.remove(mCurrentPosition);
//                    mAdapter.uris.remove(mCurrentPosition);
                    if (path!=null) {
                        Log.println(Log.ASSERT,"path!=null mCurrentPosition:", String.valueOf(mCurrentPosition));
                        Log.println(Log.ASSERT,"path!=null size:", String.valueOf(MainActivity.sDirListMap.get(path).size()));
                        MainActivity.sDirListMap.get(path).remove(mCurrentPosition);
                        MainActivity.sUriListMap.get(path).remove(mCurrentPosition);
                        MainActivity.sBitMapListMap.get(path).remove(mCurrentPosition);
                        Log.println(Log.ASSERT,"path!=null size:", String.valueOf(MainActivity.sDirListMap.get(path).size()));
                    }else {
                        MainActivity.video_path.remove(mAdapter.paths.get(mCurrentPosition));
                        MainActivity.video_uri.remove(mAdapter.uris.get(mCurrentPosition));
                    }
                    isDel = true;
                    autoPlayVideo();
                } else {
                    rvTiktok.scrollToPosition(mCurrentPosition-1);
//                    mAdapter.paths.remove(mCurrentPosition);
//                    mAdapter.uris.remove(mCurrentPosition);
                    if (path!=null) {
                        MainActivity.sDirListMap.get(path).remove(mCurrentPosition);
                        MainActivity.sUriListMap.get(path).remove(mCurrentPosition);
                        MainActivity.sBitMapListMap.get(path).remove(mCurrentPosition);
                    }else {
                        MainActivity.video_path.remove(mAdapter.paths.get(mCurrentPosition));
                        MainActivity.video_uri.remove(mAdapter.uris.get(mCurrentPosition));
                    }
                    //Log.println(Log.ASSERT, "remove后path值",mAdapter.paths.get(mCurrentPosition) );

                }
            rvTiktok.scrollToPosition(mCurrentPosition);
            } else if (result.getResultCode() == 0) {
                autoPlayVideo();
            }
        });


    }

    private void autoPlayVideo() {
        if (rvTiktok == null || rvTiktok.getChildAt(0) == null) {
            return;
        }
        JzvdStdSpeed player = rvTiktok.getChildAt(0).findViewById(R.id.videoplayer);
        if (player != null) {
            player.startVideoAfterPreloading();
        }
    }


    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
