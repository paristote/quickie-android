package com.philipoy.android.quickie.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.philipoy.android.quickie.permission.DeniedPermissionException;
import com.philipoy.android.quickie.model.QuickContact;
import com.philipoy.android.quickie.storage.QuickContactsDBHelper;

public class AddQuickContactActivity extends AppCompatActivity {

    private final String LOG_TAG = "AddContactActivity";

    private final int SAVE_QUICK_CONTACT = 100;

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
                Log.d(LOG_TAG, "Has phone: " + phone);
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (R.id.saveqf == item.getItemId()) {
			String contactName = mContactNameText.getText().toString().trim();
			String contactPhone = mContactPhoneText.getText().toString().trim();
            long now = System.currentTimeMillis();
            long eol = now + (mEOLInDays[mContactEOLSelector.getSelectedItemPosition()] * 24 * 60 * 60 * 1000); // now + selected nb of days in ms
            String eolDate = DateFormat.format("yyyy-MM-dd", eol).toString();
            saveQuickContact(contactName, contactPhone, eolDate);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.add_quick_contact, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void saveQuickContact(String name, String phone, String eol) {
        if (!"".equals(name) && !"".equals(phone) && !"".equals(eol)) {
            QuickContact contact = new QuickContact("", eol);
            contact.addContactData(name, phone);
            QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(this);
            long res = -1;
            try {
                res = dbHelper.addQuickContact(contact);
            } catch (DeniedPermissionException e) {
                if (Manifest.permission.WRITE_CONTACTS.equals(e.getDeniedPermission()) ||
                        Manifest.permission.GET_ACCOUNTS.equals(e.getDeniedPermission())) {
                    requestContactsPermission(e.getDeniedPermission());
                }
            }
            if (res == -1 || res == -2) {
                Toast.makeText(this, R.string.error_contact_not_created, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.success_contact_created, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, R.string.error_incorrect_contact_values, Toast.LENGTH_LONG).show();
        }
	}

    private void requestContactsPermission(final String permission) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.title_accounts_contacts_permission_message)
                .setNegativeButton(R.string.action_deny_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AddQuickContactActivity.this.finish();
                        Toast.makeText(AddQuickContactActivity.this, R.string.error_no_permission_contacts, Toast.LENGTH_LONG).show();
                    }
                })
                .setPositiveButton(R.string.action_grant_permission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(AddQuickContactActivity.this,
                                new String[]{permission}, SAVE_QUICK_CONTACT);
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case SAVE_QUICK_CONTACT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String contactName = mContactNameText.getText().toString().trim();
                    String contactPhone = mContactPhoneText.getText().toString().trim();
                    long now = System.currentTimeMillis();
                    long eol = now + (mEOLInDays[mContactEOLSelector.getSelectedItemPosition()] * 24 * 60 * 60 * 1000); // now + selected nb of days in ms
                    String eolDate = DateFormat.format("yyyy-MM-dd", eol).toString();
                    saveQuickContact(contactName, contactPhone, eolDate);
                } else {
                    finish();
                }
        }
    }
}
