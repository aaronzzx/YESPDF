package com.aaron.yespdf.common.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.aaron.yespdf.R;
import com.aaron.yespdf.common.App;
import com.blankj.utilcode.util.ConvertUtils;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
@SuppressLint("AppCompatCustomView")
public class BorderImageView extends ImageView {

    private static final int BORDER_COLOR = App.getContext().getResources().getColor(R.color.base_black_hint);
    private static final float BORDER_RADIUS = ConvertUtils.dp2px(3);

    private int mBorderColor;
    private float mBorderRadius;
    private Paint mPaint;

    public BorderImageView(Context context) {
        this(context, null);
    }

    public BorderImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画边框
        Rect rect = canvas.getClipBounds();
        RectF rectF = new RectF(rect);
        rect.bottom--;
        rect.right--;
        canvas.drawRoundRect(rectF, mBorderRadius, mBorderRadius, mPaint);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BorderImageView);
        mBorderColor = ta.getColor(R.styleable.BorderImageView_borderColor, BORDER_COLOR);
        mBorderRadius = ta.getDimension(R.styleable.BorderImageView_borderRadius, BORDER_RADIUS);
        ta.recycle();
    }
}
