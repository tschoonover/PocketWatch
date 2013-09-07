package org.section9.pocketwatch;

import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;

public class MessageTransmissionService extends Service implements LocationListener {
	
	private static boolean _isRunning = false;
	private static final int TEN_SECONDS = 1000 * 10;
	
	private LocationManager _lm;
	private SharedPreferences _sp;
	private MessageTransmissionWorker _worker;

	public static boolean isRunning(){
		return _isRunning;
	}

	@Override
	public IBinder onBind(Intent i) {
		return null;
	}

	@Override
	public void onCreate() {
		_sp = PreferenceManager.getDefaultSharedPreferences(this);
		_lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public void onDestroy() {
		// Clear running flag.
		_isRunning = false;
		
		// Unregister for location updates.
		_lm.removeUpdates(this);
		
		// Stop worker thread.
		if (_worker != null) {
			_worker.interrupt();
			try {
				_worker.join();
				_worker = null;
			} catch (InterruptedException e) {}
			
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Set running flag.
		_isRunning = true;
		
		// Load message text. 
		String message = _sp.getString(getResources().getString(R.string.pref_message_key), null);
		
		// Load recipient ID's.
		Set<String> recipientIDs = _sp.getStringSet(getResources().getString(R.string.pref_recipients_key), null);
		Cursor recipientsCursor = this.getContentResolver().query(
	     		ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	     		new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
	     		ContactsContract.CommonDataKinds.Phone._ID + " IN (" + TextUtils.join(",", recipientIDs) + ")",
	     		null,
	     		ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
	     	);	
		Set<String> recipientNumbers = new HashSet<String>();
		while(recipientsCursor.moveToNext()) {
			recipientNumbers.add(recipientsCursor.getString(0));
		}
		
		// Load the transmission interval preference.
		long transmissionInterval = Long.parseLong(
				_sp.getString(
						getResources().getString(R.string.pref_transmission_interval_key),
						getResources().getString(R.string.pref_transmission_interval_default)
					)
			);
		
		// Load the location preference.
		boolean useLocationService = _sp.getBoolean(
				getResources().getString(R.string.pref_location_key),
				Boolean.parseBoolean(getResources().getString(R.string.pref_location_default))
			);

		if (_worker == null) {
			// Create worker thread to send asynchronous transmissions.
			_worker = new MessageTransmissionWorker(null, message, recipientNumbers, transmissionInterval);
						
			// Configure location service (if needed).
			if (useLocationService) {
				_worker.updateLocation(_lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
				_lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TEN_SECONDS, 10, this);
				_lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, TEN_SECONDS, 10, this);
			}
			
			// Begin transmitting.
			_worker.start();
		} else {
			// Re-configure location service.
			Location initialLocation = null;
			if (useLocationService) {
				initialLocation = _lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);				
			} else {
				_lm.removeUpdates(this);
			}
				
			// Update transmission parameters.
			_worker.updateTransmissionParameters(initialLocation, message, recipientIDs, transmissionInterval);
		}
		
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onLocationChanged(Location l) {
		_worker.updateLocation(l);
	}

	@Override
	public void onProviderDisabled(String arg0) {}

	@Override
	public void onProviderEnabled(String arg0) {}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
}
