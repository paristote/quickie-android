package com.philipoy.app.quickcontact.task;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.philipoy.app.quickcontact.BuildConfig;
import com.philipoy.app.quickcontact.R;
import com.philipoy.app.quickcontact.activity.DeleteQuickContactActivity;
import com.philipoy.app.quickcontact.model.QuickContact;
import com.philipoy.app.quickcontact.storage.QuickContactsDBHelper;

public class CheckContactsToDeleteService extends Service {

	public static final int CODE = 1;
	
	private final String TAG = "Check_Delete_Service";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		new CheckContactsToDeleteTask().execute();
		
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private class CheckContactsToDeleteTask extends AsyncTask<Void, Void, ArrayList<QuickContact>>
	{
		@Override
		protected ArrayList<QuickContact> doInBackground(Void... params) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "Task is running");
			QuickContactsDBHelper dbHelper = new QuickContactsDBHelper(getApplicationContext());
			ArrayList<QuickContact> contactsToDelete = dbHelper.getQuickContactsToDelete();
			return contactsToDelete;
		}

		@Override
		protected void onPostExecute(ArrayList<QuickContact> results) {
			
			if (results != null && results.size()>0) {
				int nbContacts = results.size();
				if (BuildConfig.DEBUG)
					Log.d(TAG, nbContacts+" contact(s) to delete");
				// Build notification message
				NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext());
				nb.setAutoCancel(true);
				nb.setSmallIcon(R.drawable.ic_launcher);
                // TODO i18n of the notification title
				String s = "";
				if (nbContacts > 1) s = "s";  
				nb.setContentTitle("You have "+nbContacts+" contact"+s+" to delete");
				nb.setDefaults(NotificationCompat.DEFAULT_ALL);
				// Build action intent when user taps the notification
				Intent i = new Intent(getApplicationContext(), DeleteQuickContactActivity.class);
				i.putParcelableArrayListExtra(DeleteQuickContactActivity.QUICK_CONTACTS, results);
				TaskStackBuilder sb = TaskStackBuilder.create(getApplicationContext());
				sb.addNextIntent(i);
				PendingIntent pi = sb.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
				nb.setContentIntent(pi);
				// Display notification
				NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				manager.notify(1, nb.build());
			}
			// Stops the service until it is restarted by the Alarm Manager
			stopSelf();			
		}
	}
}
