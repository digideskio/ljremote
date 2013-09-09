package com.ljremote.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ljremote.android.R;
import com.ljremote.android.data.DataManager.TABLES;
import com.ljremote.android.data.LJFunctionManager;
import com.ljremote.json.model.LJFunction;

public class SimpleLJFunctionCursorAdapter extends AbstractCursorAdapter {
	
	private int group_id;
	
	public SimpleLJFunctionCursorAdapter(LJFunctionManager abstractDataManager, int group_id) {
		super(abstractDataManager,
				R.id.func_list,false);
		this.group_id = group_id;
		reloadCursor();
	}
	
	

	@Override
	protected void reloadCursor() {
		changeCursor(((LJFunctionManager)manager).getFunctionCursor(group_id));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1,null);

		bindView(convertView, context, cursor);
		return convertView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		LJFunction func = ((LJFunctionManager) manager).getLJFunctionFromCursor(cursor);

		TextView label = (TextView) view;
		label.setText(func.getName());
		attachOnClickListener(label,context,func.getId());
	}

	private void attachOnClickListener(View view, final Context context, final int id) {
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((LJFunctionManager) manager).execute(id);
			}
		});
	}

	@Override
	public void onTableUpdateListener(Context context, TABLES table) {
		if (table == TABLES.LJFUNCTIONS) {
			reloadCursor();
		}
	}

}
