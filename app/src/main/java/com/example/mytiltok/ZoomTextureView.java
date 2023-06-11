package com.example.mytiltok;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;

public class ZoomTextureView extends TextureView {
    private float mScaleFactor = 1.0f;
    private ScaleGestureDetector mScaleGestureDetector;

    public ZoomTextureView(Context context) {
        super(context);
        init();
    }

    public ZoomTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
            setScaleX(mScaleFactor);
            setScaleY(mScaleFactor);
            return true;
        }
    }
}

