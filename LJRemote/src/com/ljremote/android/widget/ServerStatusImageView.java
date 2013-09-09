package com.ljremote.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ljremote.android.R;
import com.ljremote.android.json.ServerStatus;

public class ServerStatusImageView extends ImageView {

//	private static final ServerStatus[] sServerStatusArray = {
//		
//	};
	private static final int[] SERVER_STATUS = {R.attr.serverStatus};
	private ServerStatus mServerStatus = ServerStatus.UNBOUND;
	
	public ServerStatusImageView(Context context) {
		super(context);
	}

	public ServerStatusImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadAttributes(context,attrs);
	}

	public ServerStatusImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		loadAttributes(context,attrs);
	}

	private void loadAttributes(Context context, AttributeSet attrs) {
//		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ServerConnectionStates);
//		mServerStatus = typedArray.getV
	}

	public ServerStatus getServerStatus() {
		return mServerStatus;
	}

	public void setServerStatus(ServerStatus serverStatus) {
		this.mServerStatus = serverStatus;
		refreshDrawableState();
	}

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		mergeDrawableStates(drawableState, SERVER_STATUS);
		return drawableState;
	}
}
