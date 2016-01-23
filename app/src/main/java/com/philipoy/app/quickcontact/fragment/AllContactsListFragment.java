package com.philipoy.app.quickcontact.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AllContactsListFragment extends ListFragment implements LoaderCallbacks<Cursor> {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		populateContactList();
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO open a dialog and invite the user to add a EOL to the selected contact
		super.onListItemClick(l, v, position, id);
	}

	/**
     * Loads the quick contacts list from the Contacts Provider
     */
    private void populateContactList() {
        
    	// We display only the DISPLAY_NAME and PHONE NUMBER of each contact
    	setListAdapter(new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2, null, 
        					new String[] {
                				ContactsContract.Data.DISPLAY_NAME,
                				ContactsContract.Data.DATA1
        					}, 
        					new int[] {
    							android.R.id.text1,
    							android.R.id.text2
							}, 0));
    	
        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
    	getLoaderManager().initLoader(0, null, this);
        
    }
    
    @Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// We get each contact data separately, i.e. if a contact has 2 phone numbers it will appear twice in the result
		Uri uri = ContactsContract.Data.CONTENT_URI;
		
		String[] projection = {  
								ContactsContract.Data._ID,
								ContactsContract.Data.DISPLAY_NAME, // Name
								ContactsContract.Data.DATA1 // Will contain the phone number as specified in the selection
							  };
		
		String[] selectionArgs = {};
		String selection = ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' ";
		String orderBy = ContactsContract.Data.DISPLAY_NAME + " ASC";
		return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, orderBy);
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		((SimpleCursorAdapter)getListAdapter()).swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
		((SimpleCursorAdapter)getListAdapter()).swapCursor(null);
	}
	
}
