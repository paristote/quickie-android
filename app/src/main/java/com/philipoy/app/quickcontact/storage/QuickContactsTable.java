package com.philipoy.app.quickcontact.storage;

import android.provider.BaseColumns;

public abstract class QuickContactsTable implements BaseColumns {

	public static final String TABLE_NAME = "quick_contacts";
    public static final String COLUMN_CONTACT_ID = "contact_id";
    public static final String COLUMN_EOL = "end_of_life";

}
