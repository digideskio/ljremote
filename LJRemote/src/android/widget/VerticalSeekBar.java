package android.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressLint("NewApi")
public class VerticalSeekBar extends SeekBar {

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;
	private boolean mIsDragging;
	private int mScaledTouchSlop;

	public VerticalSeekBar(Context context) {
        super(context);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
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
//        MotionEvent newEvent = event;
//        newEvent.setLocation(event.getY(), event.getX());
//        
//        boolean ret =  super.onTouchEvent(newEvent);
//        setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
//        onSizeChanged(getWidth(), getHeight(), 0, 0);
//        return ret;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	setPressed(true);
            	if ( getThumb() != null ) {
            		invalidate(getThumb().getBounds());
            	}
            	onStartTrackingTouch();
            	trackTouchEvent(event);
            	attemptClaimDrag();
            	break;
            case MotionEvent.ACTION_MOVE:
            	if(mIsDragging){
            		trackTouchEvent(event);
            	}else{
            		final float y = event.getY();
            		if( y > mScaledTouchSlop ) {
            			setPressed(true);
            			if ( getThumb() != null ) {
            				invalidate(getThumb().getBounds());
            			}
            			onStartTrackingTouch();
            			trackTouchEvent(event);
            			attemptClaimDrag();
            		}
            	}
            case MotionEvent.ACTION_UP:
            	if (mIsDragging ) {
            		trackTouchEvent(event);
            		onStopTrackingTouch();
            		setPressed(false);
            	} else {
            		// Touch up when we never crossed the touch slop threshold should
                    // be interpreted as a tap-seek to that location.
            		onStartTrackingTouch();
            		trackTouchEvent(event);
            		onStopTrackingTouch();
            	}
            	// ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
            	invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
            	if ( mIsDragging) {
            		onStopTrackingTouch();
            		setPressed(false);
            	}
                invalidate(); // see above explanation
                break;
        }
        return true;
    }
    
    private void trackTouchEvent(MotionEvent event) {
        final int height = getHeight();
        int mPaddingLeft  = 0;
        int mPaddingRight = 0;
        final int available = height - mPaddingLeft - mPaddingRight;
        int y = (int)event.getY();
        float scale;
        float progress = 0;
//        if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
            if (y > height - mPaddingRight) {
                scale = 0.0f;
            } else if (y < mPaddingLeft) {
                scale = 1.0f;
            } else {
                scale = (float)(available - y + mPaddingLeft) / (float)available;
            }
//        } else {
//            if (y < mPaddingLeft) {
//                scale = 0.0f;
//            } else if (y > height - mPaddingRight) {
//                scale = 1.0f;
//            } else {
//                scale = (float)(y - mPaddingLeft) / (float)available;
//            }
//        }
        final int max = getMax();
        progress += scale * max;
        
        setProgress((int) progress);
    }
    
    /**
* Tries to claim the user's drag motion, and requests disallowing any
* ancestors from stealing events in the drag.
*/
    private void attemptClaimDrag() {
        if (getParent() != null) {
        	getParent().requestDisallowInterceptTouchEvent(true);
        }
    }
    
    @Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
		super.setOnSeekBarChangeListener(l);
		mOnSeekBarChangeListener = l;
	}

	void onStartTrackingTouch() {
		mIsDragging = true;
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }
    
	void onStopTrackingTouch() {
		mIsDragging = false;
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
	}
}