package com.ljremote.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;
import com.ljremote.android.json.LJClientService.MODE;

public class MenuFragment extends Fragment implements OnItemClickListener {
	OnArticleSelectedListener listener;
	
	public interface OnArticleSelectedListener{
		public void onArticleSelected(int position);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnArticleSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
	}

	private ListView menu;
//	private final static String[] MENU_ITEMS = { "Sequences", "Statics",
//			"Cues", "CueLists", "BGCues", "Master Intensity" };
//	private final static int MENU_SEQUENCES_POS = 0;
//	private final static int MENU_STATICS_POS = 1;
//	private final static int MENU_CUES_POS = 2;
//	private final static int MENU_CUELISTS_POS = 3;
//	private final static int MENU_BGCUES_POS = 4;
//	private final static int MENU_MSTR_INT_POS = 5;
	
	View mainView= null;
private ImageView server_mode_icon;

private TextView server_mode_text;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.menu_fragment, null, true);
		updateList();
		return mainView;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		listener.onArticleSelected(position);
//		Fragment fragment= null;
//		;
//		switch (position) {
//		case MENU_SEQUENCES_POS:
//			break;
//		case MENU_STATICS_POS:
//			fragment = new StaticFragment();
//			break;
//		case MENU_CUES_POS:
//			break;
//		case MENU_CUELISTS_POS:
//			break;
//		case MENU_BGCUES_POS:
//			break;
//		case MENU_MSTR_INT_POS:
//			break;
//		default:
//			break;
//		}
//		if(fragment != null){
//			getFragmentManager().beginTransaction().replace(R.id.detail_container,fragment).addToBackStack(null).commit();
//		}
	}
	
	public void updateList(){
		if(mainView != null){
			menu = (ListView) mainView.findViewById(R.id.menu);
			server_mode_icon = (ImageView) mainView.findViewById(R.id.server_mode_icon);
			server_mode_text = (TextView) mainView.findViewById(R.id.server_mode_text);
			
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, ((MainActivity) getActivity()).getFragmentLabels());
			menu.setAdapter(adapter);
			menu.setOnItemClickListener(this);		
		}
	}

	public void changeServerMode(MODE newMode) {
		int icon_id;
		int text_id;
		switch (newMode) {
		case DRIVE:
			icon_id= R.drawable.ic_menu_cycle_green;
			text_id= R.string.server_mode_driver;
			break;
		case BOUND:
			icon_id= R.drawable.ic_menu_cycle_orange;
			text_id= R.string.server_mode_bound;
			break;
		default:
			icon_id= R.drawable.ic_menu_cycle_red;
			text_id= R.string.server_mode_unbound;
			break;
		}
		server_mode_icon.setImageResource(icon_id);
//		((MenuItem) mainView.findViewById(R.id.menu_change_mode)).setIcon(icon_id);
		server_mode_text.setText(text_id);
	}

	// @Override
	// public void onClick(View v) {
	// ((MainActivity)getActivity()).onSelectedMenu(null);
	// }
	//
}
