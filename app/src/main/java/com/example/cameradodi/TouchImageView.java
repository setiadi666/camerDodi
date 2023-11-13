package com.example.cameradodi;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends androidx.appcompat.widget.AppCompatImageView {
    Matrix matrix;
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = 0;
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1.0F;
    float maxScale = 3.0F;
    float[] m;
    int viewWidth;
    int viewHeight;
    static final int CLICK = 3;
    float saveScale = 1.0F;
    protected float origWidth;
    protected float origHeight;
    int oldMeasuredWidth;
    int oldMeasuredHeight;
    ScaleGestureDetector mScaleDetector;
    Context context;
    private OnTouchListeners listener;

    public TouchImageView(Context context) {
        super(context);
        this.sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.sharedConstructing(context);
    }

    public void setOnTouchListener(OnTouchListeners listener) {
        this.listener = listener;
    }

    private void sharedConstructing(Context context) {
        super.setClickable(true);
        this.context = context;
        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.matrix = new Matrix();
        this.m = new float[9];
        this.setImageMatrix(this.matrix);
        this.setScaleType(ScaleType.MATRIX);
        this.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                TouchImageView.this.mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());
                switch (event.getAction()) {
                    case 0:
                        TouchImageView.this.last.set(curr);
                        TouchImageView.this.start.set(TouchImageView.this.last);
                        TouchImageView.this.mode = 1;
                        break;
                    case 1:
                        TouchImageView.this.mode = 0;
                        int xDiff = (int)Math.abs(curr.x - TouchImageView.this.start.x);
                        int yDiff = (int)Math.abs(curr.y - TouchImageView.this.start.y);
                        if (xDiff < 3 && yDiff < 3) {
                            TouchImageView.this.performClick();
                        }
                        break;
                    case 2:
                        if (TouchImageView.this.mode == 1) {
                            float deltaX = curr.x - TouchImageView.this.last.x;
                            float deltaY = curr.y - TouchImageView.this.last.y;
                            float fixTransX = TouchImageView.this.getFixDragTrans(deltaX, (float)TouchImageView.this.viewWidth, TouchImageView.this.origWidth * TouchImageView.this.saveScale);
                            float fixTransY = TouchImageView.this.getFixDragTrans(deltaY, (float)TouchImageView.this.viewHeight, TouchImageView.this.origHeight * TouchImageView.this.saveScale);
                            TouchImageView.this.matrix.postTranslate(fixTransX, fixTransY);
                            TouchImageView.this.fixTrans();
                            TouchImageView.this.last.set(curr.x, curr.y);
                        }
                    case 3:
                    case 4:
                    case 5:
                    default:
                        break;
                    case 6:
                        TouchImageView.this.mode = 0;
                }

                if (TouchImageView.this.mode == 0 && TouchImageView.this.listener != null) {
                    TouchImageView.this.listener.onTouch();
                }

                TouchImageView.this.setImageMatrix(TouchImageView.this.matrix);
                TouchImageView.this.invalidate();
                return true;
            }
        });
    }

    public void setMaxZoom(float x) {
        this.maxScale = x;
    }

    void fixTrans() {
        this.matrix.getValues(this.m);
        float transX = this.m[2];
        float transY = this.m[5];
        float fixTransX = this.getFixTrans(transX, (float)this.viewWidth, this.origWidth * this.saveScale);
        float fixTransY = this.getFixTrans(transY, (float)this.viewHeight, this.origHeight * this.saveScale);
        if (fixTransX != 0.0F || fixTransY != 0.0F) {
            this.matrix.postTranslate(fixTransX, fixTransY);
        }

    }

    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans;
        float maxTrans;
        if (contentSize <= viewSize) {
            minTrans = 0.0F;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0.0F;
        }

        if (trans < minTrans) {
            return -trans + minTrans;
        } else {
            return trans > maxTrans ? -trans + maxTrans : 0.0F;
        }
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        return contentSize <= viewSize ? 0.0F : delta;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        if ((this.oldMeasuredHeight != this.viewWidth || this.oldMeasuredHeight != this.viewHeight) && this.viewWidth != 0 && this.viewHeight != 0) {
            this.oldMeasuredHeight = this.viewHeight;
            this.oldMeasuredWidth = this.viewWidth;
            if (this.saveScale == 1.0F) {
                Drawable drawable = this.getDrawable();
                if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0) {
                    return;
                }

                int bmWidth = drawable.getIntrinsicWidth();
                int bmHeight = drawable.getIntrinsicHeight();
                Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);
                float scaleX = (float)this.viewWidth / (float)bmWidth;
                float scaleY = (float)this.viewHeight / (float)bmHeight;
                float scale = Math.min(scaleX, scaleY);
                this.matrix.setScale(scale, scale);
                float redundantYSpace = (float)this.viewHeight - scale * (float)bmHeight;
                float redundantXSpace = (float)this.viewWidth - scale * (float)bmWidth;
                redundantYSpace /= 2.0F;
                redundantXSpace /= 2.0F;
                this.matrix.postTranslate(redundantXSpace, redundantYSpace);
                this.origWidth = (float)this.viewWidth - 2.0F * redundantXSpace;
                this.origHeight = (float)this.viewHeight - 2.0F * redundantYSpace;
                this.setImageMatrix(this.matrix);
            }

            this.fixTrans();
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            TouchImageView.this.mode = 2;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();
            float origScale = TouchImageView.this.saveScale;
            TouchImageView var10000 = TouchImageView.this;
            var10000.saveScale *= mScaleFactor;
            if (TouchImageView.this.saveScale > TouchImageView.this.maxScale) {
                TouchImageView.this.saveScale = TouchImageView.this.maxScale;
                mScaleFactor = TouchImageView.this.maxScale / origScale;
            } else if (TouchImageView.this.saveScale < TouchImageView.this.minScale) {
                TouchImageView.this.saveScale = TouchImageView.this.minScale;
                mScaleFactor = TouchImageView.this.minScale / origScale;
            }

            if (!(TouchImageView.this.origWidth * TouchImageView.this.saveScale <= (float)TouchImageView.this.viewWidth) && !(TouchImageView.this.origHeight * TouchImageView.this.saveScale <= (float)TouchImageView.this.viewHeight)) {
                TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            } else {
                TouchImageView.this.matrix.postScale(mScaleFactor, mScaleFactor, (float)(TouchImageView.this.viewWidth / 2), (float)(TouchImageView.this.viewHeight / 2));
            }

            TouchImageView.this.fixTrans();
            return true;
        }
    }

    public interface OnTouchListeners {
        void onTouch();
    }
}
