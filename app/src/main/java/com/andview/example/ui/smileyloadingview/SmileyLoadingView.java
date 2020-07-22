/**
 * Copyright 2016 andy
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andview.example.ui.smileyloadingview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.andview.example.R;

/**
 * Create by andy (https://github.com/andyxialm)
 * Create time: 16/8/18 10:17
 *
 * Modify by huxq17 (https://github.com/huxq17) at 16/8/29 17:30
 *
 * Description : SmileyLoadingView
 */
public class SmileyLoadingView extends View {

    private static final int DEFAULT_WIDHT = 30;
    private static final int DEFAULT_HEIGHT = 30;
    private static final int DEFAULT_PAINT_WIDTH = 5;

    private static final int DEFAULT_ANIM_DURATION = 2000;
    private static final int DEFAULT_PAINT_COLOR = Color.parseColor("#b3d8f3");
    private static final int ROTATE_OFFSET = 90;

    private Paint mArcPaint, mCirclePaint;
    private Path mCirclePath, mArcPath;
    private RectF mRectF;

    private float[] mCenterPos, mLeftEyePos, mRightEyePos;
    private float mStartAngle, mSweepAngle;
    private float mEyeCircleRadius;

    private int mStrokeColor;
    private int mAnimDuration;
    private int mAnimRepeatCount;

    private int mStrokeWidth;
    public boolean mRunning;
    private boolean mStopping;

    private boolean mFirstStep;
    private boolean mShowLeftEye, mShowRightEye;
    private boolean mStopUntilAnimationPerformCompleted;

    private OnAnimPerformCompletedListener mOnAnimPerformCompletedListener;
    private ValueAnimator mValueAnimator;

    public SmileyLoadingView(Context context) {
        this(context, null);
    }

    public SmileyLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmileyLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SmileyLoadingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        // get attrs
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SmileyLoadingView);
        mStrokeColor = ta.getColor(R.styleable.SmileyLoadingView_strokeColor, DEFAULT_PAINT_COLOR);
        mStrokeWidth = ta.getDimensionPixelSize(R.styleable.SmileyLoadingView_strokeWidth, dp2px(DEFAULT_PAINT_WIDTH));
        mAnimDuration = ta.getInt(R.styleable.SmileyLoadingView_duration, DEFAULT_ANIM_DURATION);
        mAnimRepeatCount = ta.getInt(R.styleable.SmileyLoadingView_animRepeatCount, ValueAnimator.INFINITE);
        ta.recycle();

        mSweepAngle = 180; // init sweepAngle, the mouth line sweep angle
        mCirclePath = new Path();
        mArcPath = new Path();

        mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setStrokeJoin(Paint.Join.ROUND);
        mArcPaint.setStrokeWidth(mStrokeWidth);
        mArcPaint.setColor(mStrokeColor);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(mStrokeColor);

        mCenterPos = new float[2];
        mLeftEyePos = new float[2];
        mRightEyePos = new float[2];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidthSize(widthMeasureSpec), measureHeightSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRunning) {
            if (mShowLeftEye) {
                canvas.drawCircle(mLeftEyePos[0], mLeftEyePos[1], mEyeCircleRadius, mCirclePaint);
            }

            if (mShowRightEye) {
                canvas.drawCircle(mRightEyePos[0], mRightEyePos[1], mEyeCircleRadius, mCirclePaint);
            }

            if (mFirstStep) {
                mArcPath.reset();
                mArcPath.addArc(mRectF, mStartAngle, mSweepAngle);
                canvas.drawPath(mArcPath, mArcPaint);
            } else {
                mArcPath.reset();
                mArcPath.addArc(mRectF, mStartAngle, mSweepAngle);
                canvas.drawPath(mArcPath, mArcPaint);
            }
        } else {
            canvas.drawCircle(mLeftEyePos[0], mLeftEyePos[1], mEyeCircleRadius, mCirclePaint);
            canvas.drawCircle(mRightEyePos[0], mRightEyePos[1], mEyeCircleRadius, mCirclePaint);

            mArcPath.reset();
            mArcPath.addArc(mRectF, mStartAngle, mSweepAngle);
            canvas.drawPath(mArcPath, mArcPaint);
        }
    }

    /**
     * measure width
     *
     * @param measureSpec spec
     * @return width
     */
    private int measureWidthSize(int measureSpec) {
        int defSize = dp2px(DEFAULT_WIDHT);
        int specSize = MeasureSpec.getSize(measureSpec);
        int specMode = MeasureSpec.getMode(measureSpec);

        int result = 0;
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = Math.min(defSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    /**
     * measure height
     *
     * @param measureSpec spec
     * @return height
     */
    private int measureHeightSize(int measureSpec) {
        int defSize = dp2px(DEFAULT_HEIGHT);
        int specSize = MeasureSpec.getSize(measureSpec);
        int specMode = MeasureSpec.getMode(measureSpec);

        int result = 0;
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                result = Math.min(defSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int width = getWidth();
        int height = getHeight();
        mCenterPos[0] = (width - paddingRight + paddingLeft) >> 1;
        mCenterPos[1] = (height - paddingBottom + paddingTop) >> 1;


        float radiusX = (width - paddingRight - paddingLeft - mStrokeWidth) >> 1;
        float radiusY = (height - paddingTop - paddingBottom - mStrokeWidth) >> 1;
        float radius = Math.min(radiusX, radiusY);
        mEyeCircleRadius = mStrokeWidth / 2;

        mRectF = new RectF(paddingLeft + mStrokeWidth, paddingTop + mStrokeWidth,
                width - mStrokeWidth - paddingRight, height - mStrokeWidth - paddingBottom);

        mArcPath.arcTo(mRectF, 0, 180);
        mCirclePath.addCircle(mCenterPos[0], mCenterPos[1], radius, Path.Direction.CW);
        PathMeasure circlePathMeasure = new PathMeasure(mCirclePath, true);

        circlePathMeasure.getPosTan(circlePathMeasure.getLength() / 8 * 5, mLeftEyePos, null);
        circlePathMeasure.getPosTan(circlePathMeasure.getLength() / 8 * 7, mRightEyePos, null);
        mLeftEyePos[0] += mStrokeWidth >> 2;
        mLeftEyePos[1] += mStrokeWidth >> 1;
        mRightEyePos[0] -= mStrokeWidth >> 2;
        mRightEyePos[1] += mStrokeWidth >> 1;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.end();
        }
    }

    /**
     * Set paint color alpha
     *
     * @param alpha alpha
     */
    public void setPaintAlpha(int alpha) {
        mArcPaint.setAlpha(alpha);
        mCirclePaint.setAlpha(alpha);
        invalidate();
    }

    /**
     * Set paint stroke color
     *
     * @param color color
     */
    public void setStrokeColor(int color) {
        mStrokeColor = color;
        invalidate();
    }

    /**
     * Set paint stroke width
     *
     * @param width px
     */
    public void setStrokeWidth(int width) {
        mStrokeWidth = width;
    }

    /**
     * Set animation running duration
     *
     * @param duration duration
     */
    @SuppressWarnings("unused")
    public void setAnimDuration(int duration) {
        mAnimDuration = duration;
    }

    /**
     * Set animation repeat count, ValueAnimator.INFINITE(-1) means cycle
     *
     * @param repeatCount repeat count
     */
    @SuppressWarnings("unused")
    public void setAnimRepeatCount(int repeatCount) {
        mAnimRepeatCount = repeatCount;
    }

    public void start(int animRepeatCount) {
        if (mRunning) {
            return;
        }
        mAnimRepeatCount = animRepeatCount;

        mFirstStep = true;

        mValueAnimator = ValueAnimator.ofFloat(ROTATE_OFFSET, 720.0f + 2 * ROTATE_OFFSET);
        mValueAnimator.setDuration(mAnimDuration);
        mValueAnimator.setInterpolator(new LinearInterpolator());
        mValueAnimator.setRepeatCount(mAnimRepeatCount);
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (!animation.isRunning()) {
                    return;
                }
                float animatedValue = (float) animation.getAnimatedValue();
                update(animatedValue);
            }
        });
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRunning = false;
                mStopping = false;
                if (mOnAnimPerformCompletedListener != null) {
                    mOnAnimPerformCompletedListener.onCompleted();
                }
                reset();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mRunning = false;
                mStopping = false;
                if (mOnAnimPerformCompletedListener != null) {
                    mOnAnimPerformCompletedListener.onCompleted();
                }
                reset();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (mStopUntilAnimationPerformCompleted) {
                    animation.cancel();
                    mStopUntilAnimationPerformCompleted = false;
                }
            }
        });
        mValueAnimator.start();
    }

    private void update(float angle) {
        mFirstStep = angle / 360.0f <= 1;
        if (mFirstStep) {
            mShowLeftEye = angle % 360 > 225.0f;
            mShowRightEye = angle % 360 > 315.0f;

            // set arc sweep angle when the step is first, set value: 0.1f similar to a circle
            mSweepAngle = 0.1f;
            mStartAngle = angle;
        } else {
            mShowLeftEye = (angle / 360.0f <= 2) && angle % 360 <= 225.0f;
            mShowRightEye = (angle / 360.0f <= 2) && angle % 360 <= 315.0f;
            if (angle >= (720.0f + ROTATE_OFFSET)) {
                mStartAngle = angle - (720.0f + ROTATE_OFFSET);
                mSweepAngle = ROTATE_OFFSET - mStartAngle;
            } else {
                mStartAngle = (angle / 360.0f <= 1.625) ? 0 : angle - mSweepAngle - 360;
                mSweepAngle = (angle / 360.0f <= 1.625) ? angle % 360 : 225 - (angle - 225 - 360) / 5 * 3;
            }
        }
        invalidate();
    }

    /**
     * smile
     *
     * @param angle
     */
    public void smile(float angle) {
        update(angle);
    }

    /**
     * Start animation
     */
    public void start() {
        start(ValueAnimator.INFINITE);
    }

    /**
     * Stop animation
     */
    public void stop() {
        stop(true);
    }

    /**
     * stop it after animation perform completed
     *
     * @param stopUntilAnimationPerformCompleted boolean
     */
    public void stop(boolean stopUntilAnimationPerformCompleted) {
        if (mStopping || mValueAnimator == null || !mValueAnimator.isRunning()) {
            return;
        }
        mStopping = stopUntilAnimationPerformCompleted;

        mStopUntilAnimationPerformCompleted = stopUntilAnimationPerformCompleted;
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            if (!stopUntilAnimationPerformCompleted) {
                mValueAnimator.end();
            }
        } else {
            mStopping = false;
            if (mOnAnimPerformCompletedListener != null) {
                mOnAnimPerformCompletedListener.onCompleted();
            }
        }
    }

    /**
     * set status changed listener
     *
     * @param l OnStatusChangedListener
     */
    public void setOnAnimPerformCompletedListener(OnAnimPerformCompletedListener l) {
        mOnAnimPerformCompletedListener = l;
    }

    /**
     * reset UI
     */
    private void reset() {
        mStartAngle = 0;
        mSweepAngle = 180;
        invalidate();
    }

    /**
     * dp to px
     *
     * @param dpValue dp
     * @return px
     */
    private int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Callback
     */
    public interface OnAnimPerformCompletedListener {
        void onCompleted();
    }
}
