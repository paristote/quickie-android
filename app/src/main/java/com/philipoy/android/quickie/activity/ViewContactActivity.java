package com.philipoy.android.quickie.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.philipoy.android.quickie.R;
import com.philipoy.android.quickie.model.QuickContact;
import com.philipoy.android.quickie.storage.QuickContactsDBHelper;


public class ViewContactActivity extends AppCompatActivity {

    // TODO: allow editing the contact details or canceling the EOL

    public static final String CONTACT_EXTRA = "Contact_Extra";

    private QuickContact mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        if (getIntent().hasExtra(CONTACT_EXTRA)) {
            mContact = getIntent().getParcelableExtra(CONTACT_EXTRA);
            setupActivity();
        } else {
            // this activity needs a CONTACT_EXTRA content
            finish();
        }
    }

    private void setupActivity() {
        TextView contactName, contactPhone, contactEOL;
        contactName = (TextView)findViewById(R.id.view_contact_name);
        contactPhone = (TextView)findViewById(R.id.view_contact_phone);
        contactEOL = (TextView)findViewById(R.id.view_contact_eol);
        setTitle(mContact.getContactPrimaryName());
        contactName.setText(mContact.getContactPrimaryName());
        contactPhone.setText(mContact.getContactPrimaryPhone());
        contactEOL.setText(mContact.contactEOL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_contact, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_quick_contact) {
            deleteContactAction();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteContactAction() {
        AlertDialog confirm = new AlertDialog.Builder(this)
                .setMessage(R.string.title_warning_delete_contact)
                .setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(ViewContactActivity.this);
                        dbHelper.deleteQuickContact(mContact.contactId, true);
                        Toast.makeText(ViewContactActivity.this, R.string.success_contact_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();

        confirm.show();
    }

    public void callContactAction(View v) {
        Intent call = new Intent(Intent.ACTION_CALL);
        call.setData(Uri.parse("tel:"+mContact.getContactPrimaryPhone()));
        startActivity(call);
    }
}
