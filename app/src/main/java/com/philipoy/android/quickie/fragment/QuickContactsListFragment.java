package com.philipoy.android.quickie.fragment;

import com.philipoy.android.quickie.activity.ViewContactActivity;
import com.philipoy.android.quickie.model.QuickContact;
import com.philipoy.android.quickie.storage.QuickContactsDBHelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class QuickContactsListFragment extends ListFragment {

    private ArrayList<QuickContact> items;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	

	@Override
	public void onResume() {
		
		QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(getActivity());
        items = dbHelper.getAllQuickContacts();
		setListAdapter(new ArrayAdapter<QuickContact>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, items));
		super.onResume();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
        QuickContact contact = items.get(position);
        Intent viewContact = new Intent(getContext(), ViewContactActivity.class);
        viewContact.putExtra(ViewContactActivity.CONTACT_EXTRA, contact);
        getContext().startActivity(viewContact);
		super.onListItemClick(l, v, position, id);
	}
	
}
