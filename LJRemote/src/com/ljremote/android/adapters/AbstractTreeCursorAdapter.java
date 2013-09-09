package com.ljremote.android.adapters;

import android.widget.SimpleCursorTreeAdapter;

import com.ljremote.android.data.AbstractDataManager;
import com.ljremote.android.data.DataManager.OnDatabaseUpdateListener;

public abstract class AbstractTreeCursorAdapter extends SimpleCursorTreeAdapter implements OnDatabaseUpdateListener {
	protected AbstractDataManager manager;

	public AbstractTreeCursorAdapter(AbstractDataManager manager, int groupLayout, int childrenLayout) {
		super(manager.getContext(), manager.getCursor(),
				groupLayout, manager.getCursorColumnNames(), null,
				childrenLayout,manager.getCursorColumnNames(), null);
		this.manager = manager;
		this.manager.getMainDataManager().registerDatabaseUpdateListener(this);
	}

	protected void reloadCursor() {
		changeCursor(manager.getCursor());
	}
	
}
