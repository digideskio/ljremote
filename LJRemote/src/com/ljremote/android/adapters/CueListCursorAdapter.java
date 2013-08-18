package com.ljremote.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.data.CueListManager;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.json.model.CueList;

public class CueListCursorAdapter extends AbstractCursorAdapter {
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
}
