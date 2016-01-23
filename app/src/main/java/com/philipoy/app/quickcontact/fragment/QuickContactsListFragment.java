package com.philipoy.app.quickcontact.fragment;

import com.philipoy.app.quickcontact.model.QuickContact;
import com.philipoy.app.quickcontact.storage.QuickContactsDBHelper;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class QuickContactsListFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	

	@Override
	public void onResume() {
		
		QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(getActivity());
		setListAdapter(new ArrayAdapter<QuickContact>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, dbHelper.getAllQuickContacts()));
		super.onResume();
	}



	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO open a view contact activity to allow editing the contact details, or deleting it, or canceling the EOL
		super.onListItemClick(l, v, position, id);
	}

	
	
}
