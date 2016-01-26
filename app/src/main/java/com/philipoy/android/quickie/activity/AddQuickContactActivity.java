package com.philipoy.android.quickie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.philipoy.android.quickie.BuildConfig;
import com.philipoy.android.quickie.R;
import com.philipoy.android.quickie.model.QuickContact;
import com.philipoy.android.quickie.storage.QuickContactsDBHelper;

public class AddQuickContactActivity extends ActionBarActivity {
	
	private EditText mContactNameText;
	private EditText mContactPhoneText;
	private Spinner  mContactEOLSelector;
	private long[]   mEOLInDays = {15, 30, 90, 180}; // 15d, 30d, 90d or 180d
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_quick_contact);
		
		mContactNameText = (EditText)findViewById(R.id.new_qc_name);
		mContactPhoneText = (EditText)findViewById(R.id.new_qc_phone);
		mContactEOLSelector = (Spinner)findViewById(R.id.eol);

		Intent i = getIntent();
        if (i.hasExtra(ContactsContract.Intents.Insert.PHONE)) {
            String phone = i.getStringExtra(ContactsContract.Intents.Insert.PHONE);
            mContactPhoneText.setText(phone);
            if (BuildConfig.DEBUG)
                Log.d("ADD_CONTACT", "Has phone: " + phone);
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (R.id.saveqf == item.getItemId()) {
			String contactName = mContactNameText.getText().toString().trim();
			String contactPhone = mContactPhoneText.getText().toString().trim();
            if (!"".equals(contactName) && !"".equals(contactPhone)) {
                long now = System.currentTimeMillis();
                long eol = now + (mEOLInDays[mContactEOLSelector.getSelectedItemPosition()] * 24 * 60 * 60 * 1000); // now + selected nb of days in ms
                String eolDate = DateFormat.format("yyyy-MM-dd", eol).toString();
                QuickContact contact = new QuickContact("", eolDate);
				contact.addContactData(contactName, contactPhone);
                QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(this);
                long result = dbHelper.addQuickContact(contact);

                if (result == -1)
                    Toast.makeText(this, R.string.error_contact_not_created, Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, R.string.success_contact_created, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error_incorrect_contact_values, Toast.LENGTH_LONG).show();
            }
			
			finish();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.add_quick_contact, menu);
		return super.onCreateOptionsMenu(menu);
	}
	

}
