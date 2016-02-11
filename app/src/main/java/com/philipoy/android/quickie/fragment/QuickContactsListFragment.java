package com.philipoy.android.quickie.fragment;

import com.philipoy.android.quickie.R;
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

public class QuickContactsListFragment extends ListFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		
		QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(getActivity());
		setListAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                dbHelper.getAllQuickContacts()));
        setEmptyText(getString(R.string.hint_no_quick_contact));
		super.onResume();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		QuickContact contact = (QuickContact)getListView().getItemAtPosition(position);
        Intent viewContact = new Intent(getContext(), ViewContactActivity.class);
        viewContact.putExtra(ViewContactActivity.CONTACT_EXTRA, contact);
        getContext().startActivity(viewContact);
		super.onListItemClick(l, v, position, id);
	}
}
