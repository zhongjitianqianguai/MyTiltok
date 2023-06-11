package com.example.mytiltok.jzvd;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.mytiltok.R;


public class JzvdStdSpeed extends JzvdStd {
    TextView tvSpeed;
    int currentSpeedIndex = 3;

    public JzvdStdSpeed(Context context) {
        super(context);
    }

    public JzvdStdSpeed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
        tvSpeed = findViewById(R.id.tv_speed);
        tvSpeed.setOnClickListener(this);
    }

    public void setScreenNormal() {
        super.setScreenNormal();
        tvSpeed.setVisibility(View.VISIBLE);
    }

    @Override
    public void setScreenFullscreen() {
        super.setScreenFullscreen();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            tvSpeed.setVisibility(View.VISIBLE);

        if (jzDataSource.objects == null) {
            jzDataSource.objects = new Object[]{2};
            currentSpeedIndex = 3;
        } else {
            currentSpeedIndex = (int) jzDataSource.objects[0];
        }
        if (currentSpeedIndex == 3) {
            tvSpeed.setText("倍速");
        } else {
            tvSpeed.setText(getSpeedFromIndex(currentSpeedIndex) + "X");
        }
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.tv_speed) {//0.02 0.5 0.75 1.0 1.25 1.5 1.75 2.0
            if (currentSpeedIndex == 8) {
                currentSpeedIndex = 0;
            } else {
                currentSpeedIndex += 1;
            }
            mediaInterface.setSpeed(getSpeedFromIndex(currentSpeedIndex));
            tvSpeed.setText(getSpeedFromIndex(currentSpeedIndex) + "X");
            if (jzDataSource.objects == null) {
                jzDataSource.objects = new Object[]{2};
                currentSpeedIndex = 3;
            }
            jzDataSource.objects[0] = currentSpeedIndex;
        }
    }
    CountDownTimer timer = new CountDownTimer(1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            // 这里写按下超过一秒后要执行的操作
            mediaInterface.setSpeed(2.0f);
            currentSpeedIndex=7;
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.layout_std_speed;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_DOWN){
            timer.start();
        }else {
            timer.cancel();
            if (currentSpeedIndex==7) {
                if (jzDataSource.objects!=null) {
                    mediaInterface.setSpeed(getSpeedFromIndex((Integer) jzDataSource.objects[0]));
                    currentSpeedIndex= (int) jzDataSource.objects[0];
                }
                else {
                    mediaInterface.setSpeed(1.0f);

                    currentSpeedIndex = 3;
                }
            }
        }
        return super.onTouch(v, event);
    }

    private float getSpeedFromIndex(int index) {
        float ret = 0f;
        if (index == 0){
            ret=0.1f;
        } else if (index == 1) {
            ret = 0.5f;
        } else if (index == 2) {
            ret = 0.75f;
        } else if (index == 3) {
            ret = 1.0f;
        } else if (index == 4) {
            ret = 1.25f;
        } else if (index == 5) {
            ret = 1.5f;
        } else if (index == 6) {
            ret = 1.75f;
        } else if (index == 7) {
            ret = 2.0f;
        }else if (index == 8) {
            ret = 4.0f;
        }
        return ret;
    }

}
