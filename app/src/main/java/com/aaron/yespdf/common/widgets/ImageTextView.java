package com.aaron.yespdf.common.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aaron.yespdf.R;

@SuppressLint("AppCompatCustomView")
public class ImageTextView extends TextView {

    // left
    private Drawable mIconLeft;
    private int mIconLeftTint;
    private int mIconLeftWidth;
    private int mIconLeftHeight;
    // right
    private Drawable mIconRight;
    private int mIconRightTint;
    private int mIconRightWidth;
    private int mIconRightHeight;
    // top
    private Drawable mIconTop;
    private int mIconTopTint;
    private int mIconTopWidth;
    private int mIconTopHeight;
    // bottom
    private Drawable mIconBottom;
    private int mIconBottomTint;
    private int mIconBottomWidth;
    private int mIconBottomHeight;

    private int mDrawFlag = 0;

    public ImageTextView(Context context) {
        this(context, null, 0);
    }

    public ImageTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ImageTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawFlag != 2) {
            setIconSize();
            mDrawFlag++;
        }
    }

    public Drawable getIconLeft() {
        return mIconLeft;
    }

    public void setIconLeft(Drawable drawable) {
        mIconLeft = drawable;
        invalidate();
    }

    public void setIconLeft(int drawableRes) {
        mIconLeft = getResources().getDrawable(drawableRes);
        invalidate();
    }

    public Drawable getIconRight() {
        return mIconRight;
    }

    public void setIconRight(Drawable drawable) {
        mIconRight = drawable;
        invalidate();
    }

    public void setIconRight(int drawableRes) {
        mIconRight = this.getResources().getDrawable(drawableRes);
        invalidate();
    }

    public Drawable getIconTop() {
        return mIconTop;
    }

    public void setIconTop(Drawable drawable) {
        mIconTop = drawable;
        invalidate();
    }

    public void setIconTop(int drawableRes) {
        mIconTop = this.getResources().getDrawable(drawableRes);
        invalidate();
    }

    public Drawable getIconBottom() {
        return mIconBottom;
    }

    public void setIconBottom(Drawable drawable) {
        mIconBottom = drawable;
        invalidate();
    }

    public void setIconBottom(int drawableRes) {
        mIconBottom = this.getResources().getDrawable(drawableRes);
        invalidate();
    }

    public int getIconLeftTint() {
        return mIconLeftTint;
    }

    public void setIconLeftTint(int iconLeftTint) {
        mIconLeftTint = iconLeftTint;
    }

    public int getIconLeftWidth() {
        return mIconLeftWidth;
    }

    public void setIconLeftWidth(int iconLeftWidth) {
        mIconLeftWidth = iconLeftWidth;
    }

    public int getIconLeftHeight() {
        return mIconLeftHeight;
    }

    public void setIconLeftHeight(int iconLeftHeight) {
        mIconLeftHeight = iconLeftHeight;
    }

    public int getIconRightTint() {
        return mIconRightTint;
    }

    public void setIconRightTint(int iconRightTint) {
        mIconRightTint = iconRightTint;
    }

    public int getIconRightWidth() {
        return mIconRightWidth;
    }

    public void setIconRightWidth(int iconRightWidth) {
        mIconRightWidth = iconRightWidth;
    }

    public int getIconRightHeight() {
        return mIconRightHeight;
    }

    public void setIconRightHeight(int iconRightHeight) {
        mIconRightHeight = iconRightHeight;
    }

    public int getIconTopTint() {
        return mIconTopTint;
    }

    public void setIconTopTint(int iconTopTint) {
        mIconTopTint = iconTopTint;
    }

    public int getIconTopWidth() {
        return mIconTopWidth;
    }

    public void setIconTopWidth(int iconTopWidth) {
        mIconTopWidth = iconTopWidth;
    }

    public int getIconTopHeight() {
        return mIconTopHeight;
    }

    public void setIconTopHeight(int iconTopHeight) {
        mIconTopHeight = iconTopHeight;
    }

    public int getIconBottomTint() {
        return mIconBottomTint;
    }

    public void setIconBottomTint(int iconBottomTint) {
        mIconBottomTint = iconBottomTint;
    }

    public int getIconBottomWidth() {
        return mIconBottomWidth;
    }

    public void setIconBottomWidth(int iconBottomWidth) {
        mIconBottomWidth = iconBottomWidth;
    }

    public int getIconBottomHeight() {
        return mIconBottomHeight;
    }

    public void setIconBottomHeight(int iconBottomHeight) {
        mIconBottomHeight = iconBottomHeight;
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ImageTextView);
        // left
        mIconLeft = ta.getDrawable(R.styleable.ImageTextView_iconLeft);
        mIconLeftTint = ta.getColor(R.styleable.ImageTextView_iconLeftTint, -1);
        mIconLeftWidth = (int) ta.getDimension(R.styleable.ImageTextView_iconLeftWidth, -1);
        mIconLeftHeight = (int) ta.getDimension(R.styleable.ImageTextView_iconLeftHeight, -1);
        // right
        mIconRight = ta.getDrawable(R.styleable.ImageTextView_iconRight);
        mIconRightTint = ta.getColor(R.styleable.ImageTextView_iconRightTint, -1);
        mIconRightWidth = (int) ta.getDimension(R.styleable.ImageTextView_iconRightWidth, -1);
        mIconRightHeight = (int) ta.getDimension(R.styleable.ImageTextView_iconRightHeight, -1);
        // top
        mIconTop = ta.getDrawable(R.styleable.ImageTextView_iconTop);
        mIconTopTint = ta.getColor(R.styleable.ImageTextView_iconTopTint, -1);
        mIconTopWidth = (int) ta.getDimension(R.styleable.ImageTextView_iconTopWidth, -1);
        mIconTopHeight = (int) ta.getDimension(R.styleable.ImageTextView_iconTopHeight, -1);
        // bottom
        mIconBottom = ta.getDrawable(R.styleable.ImageTextView_iconBottom);
        mIconBottomTint = ta.getColor(R.styleable.ImageTextView_iconBottomTint, -1);
        mIconBottomWidth = (int) ta.getDimension(R.styleable.ImageTextView_iconBottomWidth, -1);
        mIconBottomHeight = (int) ta.getDimension(R.styleable.ImageTextView_iconBottomHeight, -1);
        ta.recycle();
        // set tint
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mIconLeft != null && mIconLeftTint != -1) {
                mIconLeft.setTint(mIconLeftTint);

            } else if (mIconRight != null && mIconRightTint != -1) {
                mIconRight.setTint(mIconRightTint);

            } else if (mIconTop != null && mIconTopTint != -1) {
                mIconTop.setTint(mIconTopTint);

            } else if (mIconBottom != null && mIconBottomTint != -1) {
                mIconBottom.setTint(mIconBottomTint);
            }
        }
    }

    private void setIconSize() {
        if (mIconLeftWidth != -1 && mIconLeftHeight != -1) {
            mIconLeft.setBounds(0, 0, mIconLeftWidth, mIconLeftHeight);

        } else if (mIconRightWidth != -1 && mIconRightHeight != -1) {
            mIconRight.setBounds(0, 0, mIconRightWidth, mIconRightHeight);

        } else if (mIconTopWidth != -1 && mIconTopHeight != -1) {
            mIconTop.setBounds(0, 0, mIconTopWidth, mIconTopHeight);

        } else if (mIconBottomWidth != -1 && mIconBottomHeight != -1) {
            mIconBottom.setBounds(0, 0, mIconBottomWidth, mIconBottomHeight);

        } else {
            setCompoundDrawablesWithIntrinsicBounds(mIconLeft, mIconTop, mIconRight, mIconBottom);
            return;
        }
        setCompoundDrawables(mIconLeft, mIconTop, mIconRight, mIconBottom);
    }
}
