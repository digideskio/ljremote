package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class VerticalSeekBar extends SeekBar {

    public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//    	Log.e("VerticalSeekBar", "onSizeChanged : w => " + w +", h => " + h );
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
//        int h =getMeasuredHeight();int w =getMeasuredWidth();
//        Log.e("VerticalSeekBar", "onMeasure : w => " + w +", h => " + h );
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
//        int thumbHeight = mThumb == null ? 0 : mThumb.getIntrinsicHeight();
//        int dw = 0;
//        int dh = 0;
//        if (d != null) {
//            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
//            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
//            dh = Math.max(thumbHeight, dh);
////        }
//        dw += mPaddingLeft + mPaddingRight;
//        dh += mPaddingTop + mPaddingBottom;
        
//        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
//                resolveSizeAndState(dh, heightMeasureSpec, 0));
    }

    @Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}

	protected void onDraw(Canvas c) {
        c.rotate(-90);
        c.translate(-getHeight(), 0);

        super.onDraw(c);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }
}