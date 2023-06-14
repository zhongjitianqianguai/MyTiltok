package com.example.mytiltok;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChooseActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ThumbnailAdapter mAdapter;
    private List<Bitmap> mBitmapList;
    private List<Uri> mUriList;
    private String mPath;
    private ImageButton mImageButton;
    private TextView mTextView;
    public ActivityResultLauncher<IntentSenderRequest> mIntentSenderRequestActivityResultLauncher;

    public void updateUI(List<Uri> uri, List<Bitmap> bitmap) {
        mAdapter = new ThumbnailAdapter(uri,bitmap);
        mRecyclerView.setAdapter(mAdapter);
    }

    class ThumbnailHolder extends RecyclerView.ViewHolder{
        ImageView mImageView;
        Uri uri;
        boolean isChoose;
        LinearLayout mLinearLayout;
        public ThumbnailHolder(@NonNull View itemView) {
            super(itemView);
            isChoose=false;
            mImageView=itemView.findViewById(R.id.img);
            mLinearLayout=itemView.findViewById(R.id.background);
            mAdapter.color=itemView.getDrawingCacheBackgroundColor();
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAdapter.isMultiSelect) {
                        mImageButton.setClickable(mAdapter.mSelectedPositions.size() != 0);
                        if (mAdapter.mSelectedPositions.contains(getAdapterPosition())) {
                            isChoose=false;
                            mAdapter.mSelectedPositions.remove(getAdapterPosition());
                            mLinearLayout.setBackgroundColor(mAdapter.color);
                        } else {
                            isChoose=true;
                            mAdapter.mSelectedPositions.add(getAdapterPosition());
                            mLinearLayout.setBackgroundColor(Color.LTGRAY);
                        }
                    }else {
                        startActivity(new Intent(ChooseActivity.this,ActivityTikTok.class).putExtra("position",getAdapterPosition()).putExtra("path",mPath));
                        finish();
                    }
                }
            });
            mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //mAdapter.color=itemView.getBackground();
                    Log.println(Log.ASSERT,"111","11");
                    mAdapter.isMultiSelect = true;
                    mAdapter.mSelectedPositions.add(getAdapterPosition());
                    mLinearLayout.setBackgroundColor(Color.LTGRAY);
                    mImageButton.setVisibility(View.VISIBLE);
                    isChoose=true;
                    return true;
                }
            });
        }
        public void bind(Uri uri, Bitmap bitmap){
            mImageView.setImageBitmap(bitmap);
            this.uri=uri;
        }
    }
    class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailHolder>{
        List<Uri> uris;
        List<Bitmap> bitmaps;
        Set<Integer> mSelectedPositions = new HashSet<>();
        boolean isMultiSelect = false;
        List<Integer> delPositions;
        int color;
        ThumbnailHolder mThumbnailHolder;
        public ThumbnailAdapter(List<Uri> uri,List<Bitmap> bitmap){

            uris=uri;
            bitmaps=bitmap;
        }
        @NonNull
        @Override
        public ThumbnailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_thumbnail, parent, false);
            return new ThumbnailHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ThumbnailHolder holder, int position) {
            mThumbnailHolder=holder;
            holder.bind(uris.get(position),bitmaps.get(position));
            if (mSelectedPositions.contains(position)) {
                holder.mLinearLayout.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.mLinearLayout.setBackgroundColor(color);
            }
        }
        public void deleteSelectedItems() {
            delPositions = new ArrayList<>(mSelectedPositions);
            Collections.sort(delPositions, Collections.reverseOrder());
            List<Uri> delUris=new ArrayList<>();
            for (int position : delPositions) {
                delUris.add(mUriList.get(position));
            }
            mSelectedPositions.clear();
            isMultiSelect = false;
            delete(mIntentSenderRequestActivityResultLauncher,delUris);
            mImageButton.setVisibility(View.GONE);
        }
        public void delete(ActivityResultLauncher<IntentSenderRequest> launcher, List<Uri> uris) {

            ContentResolver contentResolver = getContentResolver();

            try {
                for (Uri uri : uris)
                //delete object using resolver
                     contentResolver.delete(uri, null, null);
                //Log.println(Log.ASSERT, "res", String.valueOf(res));

            } catch (SecurityException e) {

                PendingIntent pendingIntent = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                    ArrayList<Uri> collection = new ArrayList<>();
                    collection.addAll(uris);
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

        @Override
        public int getItemCount() {
            return uris.size();
        }
    }
    private void dirListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择您要刷的文件夹");
        builder.setCancelable(true);
        String[] dir = MainActivity.sDirList.toArray(new String[0]);
        builder.setIcon(R.drawable.api);
        builder.setIcon(R.drawable.api)
                .setItems(dir, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = dir[which];
                        startActivity(new Intent(ChooseActivity.this, ChooseActivity.class).putExtra("path", path));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        mPath=getIntent().getStringExtra("path");
        Log.println(Log.ASSERT,"path",mPath);
        mRecyclerView=findViewById(R.id.choose_list);
        mImageButton=findViewById(R.id.del_multi);
        mImageButton.setVisibility(View.GONE);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.deleteSelectedItems();
            }
        });
        mTextView=findViewById(R.id.choose);
        mTextView.setText("当前文件夹:"+mPath+"(点击可以更换文件夹)");
        mIntentSenderRequestActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            Log.println(Log.ASSERT, "recode", String.valueOf(result.getResultCode()));
            if (result.getResultCode() == -1) {
                for (int position : mAdapter.delPositions) {
                    MainActivity.sBitMapListMap.get(mPath).remove( position);
                    MainActivity.sUriListMap.get(mPath).remove( position);
                    MainActivity.sDirListMap.get(mPath).remove( position);
                    //mRecyclerView.getChildAt(position).setBackgroundColor(mRecyclerView.getChildAt(position).getDrawingCacheBackgroundColor());
                    mAdapter.notifyItemRemoved(position);

                }
                mAdapter.delPositions.clear();

            } else if (result.getResultCode() == 0) {
                for (int i : mAdapter.mSelectedPositions) {
                    ThumbnailHolder holder = (ThumbnailHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
                    if (holder != null) {
                        holder.mLinearLayout.setBackgroundColor(mAdapter.color);
                    }
                }
                mAdapter.isMultiSelect=false;
                mAdapter.mSelectedPositions.clear();
                mImageButton.setVisibility(View.GONE);
            }
        });

        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dirListDialog();
            }
        });
        mBitmapList=MainActivity.sBitMapListMap.get(mPath);
        mUriList=MainActivity.sUriListMap.get(mPath);
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,4);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        if (mBitmapList.size()==0) {
            Toast.makeText(this, "加载预览图失败，跳转至直接刷视频", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ChooseActivity.this,ActivityTikTok.class).putExtra("position",0).putExtra("path",mPath));
            finish();
        }
        updateUI(mUriList,mBitmapList);
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.isMultiSelect){
            for (int i : mAdapter.mSelectedPositions) {
                ThumbnailHolder holder = (ThumbnailHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    holder.mLinearLayout.setBackgroundColor(mAdapter.color);
                }
            }
            mAdapter.isMultiSelect=false;
            mAdapter.mSelectedPositions.clear();
            mImageButton.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }
}