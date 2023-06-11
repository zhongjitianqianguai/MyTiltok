package com.example.mytiltok;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.example.mytiltok.jzvd.JZUtils;
import com.example.mytiltok.jzvd.Jzvd;
import com.example.mytiltok.jzvd.JzvdStd;

/**
 * @author duguodong
 * @time 2019-12-30
 * @des
 */
public class LocalVideoActivity extends AppCompatActivity {

    public String localVideoPath;

    private JzvdStd jzvdLocalPath1,jzvdLocalPath2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setTitle(getString(R.string.local_video));
        setContentView(R.layout.activity_local_video);
        jzvdLocalPath1 = findViewById(R.id.lcoal_path1);
        jzvdLocalPath2 = findViewById(R.id.lcoal_path2);

        localVideoPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        //cp video 防止视频被意外删除
        cpAssertVideoToLocalPath();
        //setUp jzvd
        jzvdLocalPath1.setUp(localVideoPath + "/DCIM/Camera/tiktok004.mp4", "Play Local Video");
        jzvdLocalPath2.setUp(localVideoPath + "/DCIM/Camera/tiktok005.mp4", "Play Local Video");
    }

    @Override
    protected void onPause() {
        super.onPause();
        JZUtils.clearSavedProgress(this, null);
        Jzvd.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
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


    public void cpAssertVideoToLocalPath() {
        if (new File(localVideoPath).exists()) return;

        try {
            InputStream myInput;
            OutputStream myOutput = new FileOutputStream(localVideoPath);
            myInput = this.getAssets().open("local_video.mp4");
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
            Toast.makeText(this, "cp from assert to local path succ", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cpAssertVideoToLocalPath();
            } else {
                finish();
            }
        }
    }
}
