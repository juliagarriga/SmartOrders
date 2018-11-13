package sgb.orders;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.widget.Toast;

public class GpsServiceThread extends Service {
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;

	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		private 	Gps gps;
		
		
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.arg2 > 0)
				gps = new Gps();
			else
				gps.stop();
				

			// Stop the service using the startId, so that we don't stop
			// the service in the middle of handling another job
			// stopSelf(msg.arg1);
		}
	}




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	      Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

	      // For each start request, send a message to start a job and deliver the
	      // start ID so we know which request we're stopping when we finish the job
	      Message msg = mServiceHandler.obtainMessage();
	      msg.arg1 = startId;
	      msg.arg2 = 1;

	      mServiceHandler.sendMessage(msg);		
		
		return (START_STICKY);
	}

	class Gps implements LocationListener {
		LocationManager locationManager;

		private OrdersHelper helper;

	     protected void stop()
         {
	 		locationManager.removeUpdates(this);
         }

		Gps() {
			helper = new OrdersHelper(getBaseContext());
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 12000, 0, this);
			boolean enabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			if (!enabled) {
				Intent intent = new Intent(
						Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		}

		public void onLocationChanged(Location location) {
			ContentValues cv = new ContentValues();

			try {
				long latitude = (long) (location.getLatitude() * 1E6);
				long longitude = (long) (location.getLongitude() * 1E6);

				cv.put("user", "user");
				cv.put("session", "1000");
				cv.put("datetime", "current_timestamp");
				cv.put("latitude", latitude);
				cv.put("longitude", longitude);
//				long rt = helper.getWritableDatabase().insertOrThrow(
//						"Locations", "", cv);
			} catch (SQLiteConstraintException e) {
		//		Errors.appendLog(act, Errors.ERROR, "GpsService",
		//				"Error inserint linia", e, cv);
		//		Toast.makeText(act, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			/*
			 * MediaPlayer mp = MediaPlayer.create( act.getApplicationContext(),
			 * R.raw.color); mp.start();
			 * 
			 * Toast.makeText(act, "En servei", Toast.LENGTH_SHORT) .show();
			 * 
			 * /* Errors.appendLog(act, Errors.ERROR, "GpsService",
			 * "Prova en cas d'error" ); Toast.makeText(act,
			 * "No es un error, és una prova", Toast.LENGTH_SHORT) .show();
			 */

		}

		public void onProviderDisabled(String provider) {


		}

		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}

	public void onCreate() {
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_FOREGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public void onDestroy() {
	      Message msg = mServiceHandler.obtainMessage();
	      msg.arg2 = -1;
	      mServiceHandler.sendMessage(msg);			
		Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
