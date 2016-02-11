package com.philipoy.android.quickie.activity;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.philipoy.android.quickie.BuildConfig;
import com.philipoy.android.quickie.R;
import com.philipoy.android.quickie.fragment.QuickContactsListFragment;
import com.philipoy.android.quickie.task.CheckContactsToDeleteService;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getSupportFragmentManager().beginTransaction().add(R.id.fragment, new QuickContactsListFragment()).commit();

		scheduleCheckContactsToDeleteTask();
		
	}
	
	/**
	 * Schedules an alarm, via Alarm Manager, to start the service CheckContactsToDeleteService every day.
	 */
	private void scheduleCheckContactsToDeleteTask() {
		Intent checkContacts = new Intent(this, CheckContactsToDeleteService.class);
		PendingIntent alarmIntent = PendingIntent.getService(this, CheckContactsToDeleteService.CODE, checkContacts, 0);

		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		if (BuildConfig.DEBUG) {
			Calendar cal = Calendar.getInstance();
            // In debug mode we run the service every minute
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60*1000, alarmIntent);
		} else {
			alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, alarmIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.action_add_quick_contact:
			Intent i = new Intent(this, AddQuickContactActivity.class);
			startActivity(i);
			return true;
//			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
