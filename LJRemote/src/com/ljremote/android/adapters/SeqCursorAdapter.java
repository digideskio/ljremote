package com.ljremote.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.SequenceManager;
import com.ljremote.json.model.Seq;

public class SeqCursorAdapter extends AbstractCursorAdapter {
	
	public SeqCursorAdapter(SequenceManager abstractDataManager) {
		super(abstractDataManager,
				R.id.seq_list);
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
		Seq seq = ((SequenceManager) manager).getCueFromCursor(cursor);

		((TextView) view.findViewById(R.id.id)).setText(String.valueOf(seq
				.getId()));
		TextView label = (TextView) view.findViewById(R.id.label);
		label.setText(seq.getLabel());

	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if (table == TABLES.SEQUENCES) {
			reloadCursor();
		}
	}
}
