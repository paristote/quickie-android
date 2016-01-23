package com.philipoy.android.quickie.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.philipoy.android.quickie.R;
import com.philipoy.android.quickie.model.QuickContact;
import com.philipoy.android.quickie.storage.QuickContactsDBHelper;

public class DeleteQuickContactActivity extends ActionBarActivity {

	private ListView mContactsListView;
	private ArrayList<QuickContact> mContactsList;
	private ContactsToDeleteAdapter mAdapter;
	
	public static final String QUICK_CONTACTS = "QUICK_CONTACTS";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delete_quick_contact);
		
		mContactsListView = (ListView)findViewById(R.id.list_contacts_to_delete);
		
		Intent i = getIntent();
		
		// TODO check if the list passed in the Intent does not contain already deleted contacts
		if (i != null && i.getParcelableArrayListExtra(QUICK_CONTACTS) != null) {
			// We have the list of contacts to delete
			mContactsList = i.getParcelableArrayListExtra(QUICK_CONTACTS);
			mAdapter = new ContactsToDeleteAdapter(getLayoutInflater());
			mContactsListView.setAdapter(mAdapter);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.delete_quick_contact, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.delete_all_contacts_btn) {
			handleClick(id, -1);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void handleClick(int buttonId, int itemPos) {
		QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(this);
		QuickContact contact = null;
		int result = -1;
		switch (buttonId) {
		/*
		 * Delete all contacts
		 */
		case R.id.delete_all_contacts_btn:
			ArrayList<String> IDs = new ArrayList<String>(mContactsList.size());
			for (QuickContact c : mContactsList) {
				IDs.add(c.contactId);
			}
			result = dbHelper.deleteAllQuickContacts(IDs);
			if (result == (IDs.size()*2)) {
				Toast.makeText(this, R.string.success_all_contacts_deleted, Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(this, R.string.error_all_contacts_not_deleted, Toast.LENGTH_LONG).show();
			}
		break;
		/*
		 * Delete selected contact
		 */
		case R.id.delete_contact_btn:
			contact = mContactsList.get(itemPos);
			result = dbHelper.deleteQuickContact(contact.contactId, true);
			if (result == 2) {
				Toast.makeText(this, R.string.success_contact_deleted, Toast.LENGTH_SHORT).show();
				mContactsList.remove(itemPos);
				if (mContactsList.isEmpty()) finish();
				else mAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(this, R.string.error_contact_not_deleted, Toast.LENGTH_LONG).show();
			}
		break;
		/*
		 * Postpone selected contact's EOL to tomorrow
		 */
		case R.id.postpone_eol_btn:
			contact = mContactsList.get(itemPos);
			boolean ok = dbHelper.postponeEOLOfContact(contact.contactId);
			if (ok) {
				Toast.makeText(this, R.string.success_ask_tomorrow, Toast.LENGTH_SHORT).show();
				mContactsList.remove(itemPos);
				if (mContactsList.isEmpty()) finish();
				else mAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(this, R.string.error_contact_not_postponed, Toast.LENGTH_LONG).show();
			}
		break;
		/*
		 * Keep the selected contact permanently
		 */
		case R.id.cancel_eol_btn:
			contact = mContactsList.get(itemPos);
			result = dbHelper.deleteQuickContact(contact.contactId, false);
			if (result == 1) {
				Toast.makeText(this, R.string.success_quick_contact_only_deleted, Toast.LENGTH_SHORT).show();
				mContactsList.remove(itemPos);
				if (mContactsList.isEmpty()) finish();
				else mAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(this, R.string.error_quick_contact_only_not_deleted, Toast.LENGTH_LONG).show();
			}
		break;
		}
	}
	
	public class ContactsToDeleteAdapter extends BaseAdapter {
		
		private LayoutInflater inflater;
		
		public ContactsToDeleteAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
		}
		
		@Override
		public int getCount() {
			return (mContactsList != null ? mContactsList.size() : 0);
		}

		@Override
		public Object getItem(int position) {
			return (mContactsList != null ? mContactsList.get(position) : null);
		}
		
		public QuickContact getContact(int position) {
			return (QuickContact)getItem(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.contact_to_delete_item, null);
				holder = new ViewHolder();
				holder.mContactTxt = (TextView)convertView.findViewById(R.id.contact_to_delete_txt);
				holder.mContactPhoneTxt = (TextView)convertView.findViewById(R.id.contact_to_delete_phone_txt);
				holder.mDeleteBtn = (Button)convertView.findViewById(R.id.delete_contact_btn);
				holder.mPostponeBtn = (Button)convertView.findViewById(R.id.postpone_eol_btn);
				holder.mCancelBtn = (Button)convertView.findViewById(R.id.cancel_eol_btn);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			ContactsToDeleteClickListener clickListener = new ContactsToDeleteClickListener(position);
			QuickContact contact = getContact(position);
			holder.mContactTxt.setText(contact.getContactPrimaryName());
			holder.mContactPhoneTxt.setText(contact.getContactPrimaryPhone());
			holder.mDeleteBtn.setOnClickListener(clickListener);
			holder.mPostponeBtn.setOnClickListener(clickListener);
			holder.mCancelBtn.setOnClickListener(clickListener);
			return convertView;
		}
	}
	
	public static class ViewHolder {
		public TextView mContactTxt;
		public TextView mContactPhoneTxt;
		public Button   mDeleteBtn;
		public Button   mPostponeBtn;
		public Button   mCancelBtn;
	}
	
	public class ContactsToDeleteClickListener implements View.OnClickListener {

		private int position;
		
		public ContactsToDeleteClickListener(int pos) {
			position = pos;
		}
		
		@Override
		public void onClick(View v) {
			handleClick(v.getId(), position);
		}
		
	}
}
