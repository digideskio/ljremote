package com.ljremote.android.adapters;

import android.support.v4.widget.SimpleCursorAdapter;

import com.ljremote.android.data.AbstractDataManager;
import com.ljremote.android.data.DataManager.OnDatabaseUpdateListener;

public abstract class AbstractCursorAdapter extends SimpleCursorAdapter implements OnDatabaseUpdateListener {
	protected AbstractDataManager manager;

	public AbstractCursorAdapter(AbstractDataManager manager, int layout) {
		super(manager.getContext(), layout, manager.getCursor(), manager
				.getCursorColumnNames(), null, NO_SELECTION);
		this.manager = manager;
		this.manager.getMainDataManager().registerDatabaseUpdateListener(this);
	}

	protected void reloadCursor() {
		changeCursor(manager.getCursor());
	}
}
