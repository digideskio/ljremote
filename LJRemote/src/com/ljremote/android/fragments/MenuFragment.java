package com.ljremote.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ljremote.android.MainActivity;
import com.ljremote.android.R;

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
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, ((MainActivity) getActivity()).getFragmentLabels());
			menu.setAdapter(adapter);
			menu.setOnItemClickListener(this);		
		}
	}

	// @Override
	// public void onClick(View v) {
	// ((MainActivity)getActivity()).onSelectedMenu(null);
	// }
	//
}
