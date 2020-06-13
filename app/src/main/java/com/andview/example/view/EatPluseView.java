package com.andview.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Author： fanyafeng
 * Date： 17/9/28 下午1:46
 * Email: fanyafeng@live.cn
 */
public class EatPluseView extends View {

    private static final String TAG = EatPluseView.class.getSimpleName();

    private static int width = 0;
    private static int height = 0;

    private final static int sunR = 120;
    private Paint sunPaint;
    private int sunX;
    private int sunY;
    private static RectF sunRectF;

    private int startAngle = 0;
    private int swipeAngle = 360;

    private boolean isAdd = true;

    private int pluseR;

    public EatPluseView(Context context) {
        super(context);
        init();
    }

    public EatPluseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EatPluseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        sunPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunPaint.setAntiAlias(true);
        sunPaint.setColor(Color.BLUE);
        sunPaint.setStyle(Paint.Style.FILL);
        sunPaint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0) {
            width = getWidth();
        }
        if (height == 0) {
            height = getHeight();
        }

        if (sunX == 0) {
            sunX = width / 2;
        }
        if (sunY == 0) {
            sunY = height / 2;
        }
        Log.d(TAG, "宽度：" + width + "高度：" + height);

        Log.d(TAG, "sunX - sunR：" + (sunX - sunR) + "       sunY - sunR：" + (sunY - sunR) + "       sunX + sunR：" + (sunX + sunR) + "       sunY - sunR：" + (sunY + sunR));
        if (sunRectF == null) {
            sunRectF = new RectF(0, sunY - sunR, 2 * sunR, sunY + sunR);

            pluseR = sunR * 11 / 6 + sunR;
        }

        canvas.drawArc(sunRectF, startAngle, swipeAngle, true, sunPaint);

        canvas.drawCircle(pluseR, sunY, sunR / 6, sunPaint);
        if (pluseR <= sunR * 5 / 6) {
            pluseR = sunR * 11 / 6 + sunR;
        } else {
            pluseR -= 4;
        }
        if (!isAdd) {
            startAngle--;
            swipeAngle += 2;
        } else {
            startAngle++;
            swipeAngle -= 2;
        }

        if (startAngle == 30) {
            isAdd = false;
        }

        if (startAngle == 0) {
            isAdd = true;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        }, 10);

    }


}
