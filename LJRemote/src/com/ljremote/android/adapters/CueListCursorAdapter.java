package com.ljremote.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.data.CueListManager;
import com.ljremote.android.data.CueManager;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.json.model.Cue;
import com.ljremote.json.model.CueList;

public class CueListCursorAdapter extends AbstractCursorAdapter implements MultiChoiceModeListener {
	private int lastSelectedPos;

	public CueListCursorAdapter(CueListManager abstractDataManager) {
		super(abstractDataManager, R.layout.static_item);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = LayoutInflater.from(context).inflate(
				R.layout.seq_item, null);

		bindView(convertView, context, cursor);
		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		CueList cueList = ((CueListManager) manager).getCueListFromCursor(cursor);

		((TextView) view.findViewById(R.id.id)).setText(String.valueOf(cueList
				.getId()));
		TextView label = (TextView) view.findViewById(R.id.label);
		label.setText(cueList.getLabel());

	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if (table == TABLES.CUELISTS) {
			reloadCursor();
		}
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.load_cue:
			CueList cueList = getCueListAtPosition(lastSelectedPos);
			if (cueList != null) {
				Log.d("CueCursorAdapter",
						"Cue " + cueList.getId() + " - " + cueList.getLabel());
				((CueListManager) manager).loadCueList(cueList.getId());
			}
			mode.finish();
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.cuelist_context, menu);
		lastSelectedPos = -1;
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		if (checked) {
			CueList cueList = getCueListAtPosition(position);
			if (cueList != null) {
				lastSelectedPos = position;
				mode.setTitle(cueList.getLabel());
			}
		}
	}
	
	private CueList getCueListAtPosition(int position) {
		Cursor c = getCursor();
		if (!c.moveToPosition(position)) {
			return null;
		}
		CueList cueList = ((CueListManager) manager).getCueListFromCursor(c);
		return cueList;
	}
}
