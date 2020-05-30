package com.aaron.yespdf.common.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.aaron.yespdf.R;
import com.blankj.utilcode.util.ConvertUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@SuppressLint("AppCompatCustomView")
public class BorderImageView extends ImageView {

    private static final int BORDER_COLOR = Color.parseColor("#66000000");
    private static final float BORDER_RADIUS = ConvertUtils.dp2px(1);

    private int mBorderColor;
    private float mBorderRadius;
    private boolean drawBorder = true;
    private Paint mPaint;

    public BorderImageView(Context context) {
        this(context, null);
    }

    public BorderImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        setScaleType(ScaleType.FIT_XY);
        mPaint = new Paint();
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        mBorderColor = borderColor;
    }

    public float getBorderRadius() {
        return mBorderRadius;
    }

    public void setBorderRadius(float borderRadius) {
        mBorderRadius = borderRadius;
    }

    public boolean isDrawBorder() {
        return drawBorder;
    }

    public void clearBorder() {
        drawBorder = false;
        invalidate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getDrawable() != null && drawBorder) {
            //画边框
            Rect rect = canvas.getClipBounds();
            RectF rectF = new RectF(rect);
            rect.bottom--;
            rect.right--;
            canvas.drawRoundRect(rectF, mBorderRadius, mBorderRadius, mPaint);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BorderImageView);
        mBorderColor = ta.getColor(R.styleable.BorderImageView_borderColor, BORDER_COLOR);
        mBorderRadius = ta.getDimension(R.styleable.BorderImageView_borderRadius, BORDER_RADIUS);
        ta.recycle();
    }
}
