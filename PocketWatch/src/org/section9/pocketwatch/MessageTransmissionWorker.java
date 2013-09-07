package org.section9.pocketwatch;

import java.util.Set;

import android.location.Location;
import android.telephony.SmsManager;

public class MessageTransmissionWorker extends Thread {
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	private Location _location;
	private String _message;
	private Set<String> _recipients;
	private long _transmissionInterval;
	
	public MessageTransmissionWorker(Location location, String message, Set<String> recipients, long transmissionInterval) {
		_location = location;
		_message = message;
		_recipients = recipients;
		_transmissionInterval = transmissionInterval;
	}
	
	private String getLocationSuffix() {
		if (_location == null) {
			return "";
		} else {
			return String.format(
					"https://maps.google.com/maps?q=loc:%s,%s",
					_location.getLatitude(),
					_location.getLongitude()
				);
		}
	}
	
	/** Determines whether the specified Location is better than the current Location.
	  * @param newLocation  The new Location that you want to evaluate
	  */
	protected boolean isBetterThanCurrent(Location newLocation) {
		if (_location == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = newLocation.getTime() - _location.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (newLocation.getAccuracy() - _location.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = newLocation.getProvider().equals(_location.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	@Override
	public void run() {
		long lastTransmissionTime = 0;
		while (!isInterrupted()) {
			synchronized(this) {
				if (System.currentTimeMillis() - lastTransmissionTime > _transmissionInterval) {
					for (String recipientNum : _recipients) {
						SmsManager.getDefault().sendTextMessage(recipientNum, null, _message + getLocationSuffix(), null, null);
						// Break out of loop if thread interrupted.
						if (isInterrupted())
							break;
					}
					lastTransmissionTime = System.currentTimeMillis();
				}
			}
		}
	}

	/**
	 * Update location to be included in the transmission message.
	 * @param location The location to be included in the transmission message
	 *     or null if no location information should be transmitted.
	 */
	public synchronized void updateLocation(Location location) {
		if (location == null || isBetterThanCurrent(location))
			_location = location;
	}
	
	/**
	 * Update the transmission parameters. All parameters except location must be updated
	 * simultaneously to prevent partially updated transmissions.
	 * @param location The location to be included in the transmission message
	 *     or null if no location information should be transmitted.
	 * @param message The message text.
	 * @param recipients The phone numbers of the message recipients.
	 * @param transmissionInterval The time in milliseconds between transmissions.
	 */
	public synchronized void updateTransmissionParameters(Location location, String message, Set<String> recipients, long transmissionInterval) {
		updateLocation(location);
		_message = message;
		_recipients = recipients;
		_transmissionInterval = transmissionInterval;
	}
}
