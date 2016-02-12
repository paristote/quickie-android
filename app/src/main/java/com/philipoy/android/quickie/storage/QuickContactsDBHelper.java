package com.philipoy.android.quickie.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.util.Log;

import com.philipoy.android.quickie.BuildConfig;
import com.philipoy.android.quickie.permission.DeniedPermissionException;
import com.philipoy.android.quickie.model.QuickContact;

public class QuickContactsDBHelper extends SQLiteOpenHelper {
	
	private Context mContext;

    private final String LOG_TAG = "QuickContactsHelper";
	
	private static final String DATABASE_NAME = "QuickContacts.db";
	private static final int DATABASE_VERSION = 1;
    private static final String SQL_TABLE_CREATE =
                "CREATE TABLE " + QuickContactsTable.TABLE_NAME + " (" +
                " "+QuickContactsTable.COLUMN_CONTACT_ID+" TEXT, " +
                " "+QuickContactsTable.COLUMN_EOL+" TEXT);";

	
	public QuickContactsDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_TABLE_CREATE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
	}
	
	/**
	 * Returns the list of quick contacts whose EOL is in less than x day
	 * @param x the number of days
	 * @return an ArrayList of QuickContacts
	 */
	public ArrayList<QuickContact> getQuickContactsToDeleteInXDays(long x) {
		if (x <= 0) return null;
		long inXDays = System.currentTimeMillis() + (x * 24 * 60 * 60 * 1000);
		String eolDate = DateFormat.format("yyyy-MM-dd", inXDays).toString();
		SQLiteDatabase db = getReadableDatabase();
		String[] sqlColumns = { QuickContactsTable.COLUMN_CONTACT_ID, QuickContactsTable.COLUMN_EOL };
		String selection = QuickContactsTable.COLUMN_EOL+" <= ?";
		String[] args = { eolDate };
		Cursor c = db.query(QuickContactsTable.TABLE_NAME, sqlColumns, selection, args, null, null, null);
		ArrayList<QuickContact> results = getRealContactsOf(c);
		c.close();
		db.close();
		return results;
	}
	
	/**
	 * Returns the list of quick contacts whose EOL is in less than 1 day
	 * @return an ArrayList of QuickContacts
	 */
	public ArrayList<QuickContact> getQuickContactsToDelete() {
		return getQuickContactsToDeleteInXDays(1);
	}
	
	/**
	 * Retrieves all the entries of the QuickContacts table and the corresponding real contacts
	 * @return an ArrayList of fully detailed QuickContacts
	 */
	public ArrayList<QuickContact> getAllQuickContacts() {
		// get all the IDs of real contacts that have a corresponding quick contact
		SQLiteDatabase db = getReadableDatabase();
		String[] sqlColumns = { QuickContactsTable.COLUMN_CONTACT_ID, QuickContactsTable.COLUMN_EOL };
		String orderBy = QuickContactsTable.COLUMN_EOL + " ASC";
		Cursor c = db.query(QuickContactsTable.TABLE_NAME, sqlColumns, null, null, null, null, orderBy);
		ArrayList<QuickContact> results = getRealContactsOf(c);
		c.close();
		db.close();
		return results;
	}
	
	/**
	 * Takes a Cursor with QuickContact realID and EOL values, <br/>
	 * and queries the Contacts Provider to create an ArrayList of QuickContacts with all details.
	 * @param cursor a Cursor with columns QuickContactsTable.COLUMN_CONTACT_ID and QuickContactsTable.COLUMN_EOL
	 * @return an ArrayList of fully detailed QuickContacts
	 */
	private ArrayList<QuickContact> getRealContactsOf(Cursor cursor) {
		int n = cursor.getCount();
		HashMap<String, QuickContact> contacts = new HashMap<>(n);
		if (n > 0) {
			// get the real contact info from each retrieved ID
			StringBuilder selection = new StringBuilder();
			ArrayList<String> args = new ArrayList<String>(n);
			selection.append("("); // initial ( for the list of OR clauses
			while (cursor.moveToNext())
			{
				selection.append(ContactsContract.Data.RAW_CONTACT_ID).append(" = ? OR ");
				String realID = cursor.getString(cursor.getColumnIndex(QuickContactsTable.COLUMN_CONTACT_ID));
				String eol = cursor.getString(cursor.getColumnIndex(QuickContactsTable.COLUMN_EOL));
				contacts.put(realID, new QuickContact(realID, eol));
				args.add(realID);
			}
			selection.delete(selection.lastIndexOf("OR"), selection.length()); // remove the last OR clause
			selection.append(")"); // closing ) for the list of OR clauses
			selection.append(" AND ").append(ContactsContract.Data.MIMETYPE).append(" = ?"); // add the Phone data type parameter
			args.add(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
			
			String[] qProjection = {  
					ContactsContract.Data._ID,
					ContactsContract.Data.RAW_CONTACT_ID,
					ContactsContract.Data.DISPLAY_NAME, // Name
					ContactsContract.Data.DATA1 // Will contain the phone number as specified in the selection
			};
			
			Cursor cc = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI, 
															qProjection, 
															selection.toString(), 
															args.toArray(new String[args.size()]), 
															null);

			while (cc.moveToNext()) {
				String realID = cc.getString(cc.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
				String contactName = cc.getString(cc.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				String contactPhone = cc.getString(cc.getColumnIndex(ContactsContract.Data.DATA1));
				QuickContact contact = contacts.get(realID);
				contact.addContactData(contactName, contactPhone);
			}
		}

		ArrayList<QuickContact> results = new ArrayList<>(n);
		Iterator<QuickContact> iterator = contacts.values().iterator();
		while (iterator.hasNext()) {
			results.add(iterator.next());
		}
		return results;
	}
	
	/**
	 * Gets the name and type of the 1st account configured on the device
	 * @return an array that contains the [name, type] of the account
	 */
	private String[] getAccountInfo() throws DeniedPermissionException {
		// TODO better handling of multiple accounts
		String[] info = new String[2];
        int permCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.GET_ACCOUNTS);
        if (permCheck == PackageManager.PERMISSION_GRANTED) {
            Account[] accounts = AccountManager.get(mContext).getAccounts();
            if (accounts.length > 0) {
                info[0] = accounts[0].name;
                info[1] = accounts[0].type;
            }
        } else {
            throw new DeniedPermissionException(Manifest.permission.GET_ACCOUNTS);
        }

		return info;
	}
	
	/**
	 * Create the Raw Contact of the given QuickContact.
	 * @param contact a QuickContact with at least a contactName and contactPhone properties
	 * @return the _ID of the created Raw Contact
	 */
	private String addRealContact(QuickContact contact) throws DeniedPermissionException {
		String[] account = getAccountInfo();
		String realId = "";

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		// insert the Raw Contact
		ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
				.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account[1])
				.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account[0])
				.build());
		// insert the Raw Contact name
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.getContactPrimaryName())
                .build());
		// insert the Raw Contact phone number
		ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getContactPrimaryPhone())
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)
                .build());
		
		ContentProviderResult[] result = null;
		try {
			int permCheck = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CONTACTS);
            if (permCheck == PackageManager.PERMISSION_GRANTED) {
                result = mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } else {
                throw new DeniedPermissionException(Manifest.permission.WRITE_CONTACTS);
            }
		} catch (RemoteException|OperationApplicationException e) {
            Log.e(LOG_TAG, e.getMessage());
            if (BuildConfig.DEBUG)
                Log.d(LOG_TAG, Log.getStackTraceString(e));
        }
        // The ID of the real contact is the last part of its URI, that is stored in the result of the 1st operation above
		// ContentUris.parseId is a utility method provided by the framework
		if (result != null && result.length == 3) {
			realId = String.valueOf(ContentUris.parseId(result[0].uri));
		}
		return realId;
	}
	
	/**
	 * Inserts a quick contact and the corresponding real contact
	 * @param contact the QuickContact to insert in DB
	 * @return -1 if an error occurred while creating the quick contact, -2 if an error occurred creating the real contact,
	 *          or the row ID of the newly inserted quick contact
	 */
	public long addQuickContact(QuickContact contact) throws DeniedPermissionException {
		// first, create the real contact and collect the raw contact ID
		String contactId = addRealContact(contact);
		if ("".equals(contactId)) return -2;
		// then add the info about the quick contact in DB
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(QuickContactsTable.COLUMN_CONTACT_ID, contactId);
		values.put(QuickContactsTable.COLUMN_EOL, contact.contactEOL);
		long newRowId = db.insert(QuickContactsTable.TABLE_NAME, "null", values);
		db.close();
		return newRowId;
	}
	
	/**
	 * Updates the contact with the given ID to postpone its EOL of 1 day
	 * @param id the ID of the raw contact
	 * @return true if the contact was updated, false otherwise
	 */
	public boolean postponeEOLOfContact(String id) {
		SQLiteDatabase db = getWritableDatabase();
		String where = QuickContactsTable.COLUMN_CONTACT_ID + " = ?";
		String[] args = {id};
		long now = System.currentTimeMillis();
		long eol = now + (2 * 24 * 60 * 60 * 1000); // now + 2 days in ms
		String eolDate = DateFormat.format("yyyy-MM-dd", eol).toString();
		ContentValues values = new ContentValues(1);
		values.put(QuickContactsTable.COLUMN_EOL, eolDate);
		int updated = db.update(QuickContactsTable.TABLE_NAME, values, where, args);
		db.close();
		return (updated == 1);
	}
	
	/**
	 * Deletes the real contact with given ID from the ContactsProvider
	 * @param id the ID of the raw contact
	 * @return the number of rows deleted
	 */
	private int deleteRealContact(String id) {
		String where = ContactsContract.RawContacts._ID + " = ?";
		String[] args = {id};
		return mContext.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, where, args);
	}
	
	/**
	 * Deletes a Quick Contact. If alsoDeleteRealContact is true, deletes the corresponding real contact as well.
	 * @param id ID of the contact to delete
	 * @param alsoDeleteRealContact if true is passed, the operation will delete the corresponding real contact
	 * @return 2 if both quick and real contacts are deleted, <br/>
	 *         1 if only the quick contact is deleted, but not the real contact, as expected <br/>
	 *        -1 otherwise
	 */
	public int deleteQuickContact(String id, boolean alsoDeleteRealContact) {
		SQLiteDatabase db = getWritableDatabase();
		String where = QuickContactsTable.COLUMN_CONTACT_ID + " = ?";
		String[] args = {id};
		int deleted = db.delete(QuickContactsTable.TABLE_NAME, where, args);
		int realDeleted = -1;
		if (alsoDeleteRealContact) realDeleted = deleteRealContact(id);
		db.close();
		
		if (deleted > 0) {
			if (deleted == realDeleted) return 2; // both quick and real contacts where deleted
			else if (realDeleted == -1 && !alsoDeleteRealContact) return 1; // only quick contact was deleted as expected
			else return -1; // unexpected case
		} else {
			return -1; // no contact deleted
		}
	}
	
	/**
	 * Deletes all Quick Contacts whose IDs are given
	 * @param ids the List of contacts' IDs to delete
	 * @return twice the size of the list if all contacts were deleted successfully, <br/>
	 *         less if some contacts could not be deleted 
	 */
	public int deleteAllQuickContacts(ArrayList<String> ids) {
		int total = 0;
		for (String id : ids) {
			total += deleteQuickContact(id, true);
		}
		return total;
	}

}
