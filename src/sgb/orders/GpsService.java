package sgb.orders;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/*import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;*/

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class GpsService extends Service {
	OrdersHelper helper;
	Activity act;

	LocationManager locationManager;
	Gps gps;
	public static ServiceUpdateUIListener UI_UPDATE_LISTENER;
	
	
	private Timer timer = new Timer();
	private static final long UPDATE_INTERVAL = 50000;
	private final IBinder mBinder = new MyBinder();
	private ArrayList<String> list = new ArrayList<String>();
	
	
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	    return(START_NOT_STICKY);
	  }
	
	public static void setUpdateListener(ServiceUpdateUIListener l) {
		UI_UPDATE_LISTENER = l;
	}
	
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			UI_UPDATE_LISTENER.update(long longitut,long latitud);
		}
	};
	
	void EnviarPosicio(long longitud,long lat) {
/*  	       HttpClient httpClient = new DefaultHttpClient();
	        String url = "http://www.reset.es/geo/insertar_datos.php?usuari=1&data=2012-08-07&hora=10&longitut=1000&latitud=1112";
	        HttpGet httpGet = new HttpGet(url);
	        try {
	            HttpResponse response = httpClient.execute(httpGet);
	            StatusLine statusLine = response.getStatusLine();
	            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
	                HttpEntity entity = response.getEntity();
	                ByteArrayOutputStream out = new ByteArrayOutputStream();
	                entity.writeTo(out);
	                out.close();
	                String responseStr = out.toString();
	                Toast.makeText(getBaseContext(), 
	                        responseStr, 
	                        Toast.LENGTH_LONG).show();

	                // do something with response 
	            } else {
	                // handle bad response
	            }
	        } catch (ClientProtocolException e) {
	            // handle exception
	        } 
	        catch (IOException e) {
	            Toast.makeText(getBaseContext(), 
	                    e.toString(), 
	                    Toast.LENGTH_LONG).show();
	        } */
	}
	
	class Gps implements LocationListener {

		private OrdersHelper helper;
		
		Gps(OrdersHelper helper) {
			this.helper = helper;
		}
		
		public void onLocationChanged(Location location) {
			ContentValues cv = new ContentValues();

			try {
				long latitude =   (long)(location.getLatitude()*1E6);
				long longitude =   (long)(location.getLongitude()*1E6);
			
			cv.put("user", "user");
			cv.put("session", "1000");
			cv.put("datetime", "current_timestamp");
			cv.put("latitude", latitude);
			cv.put("longitude", longitude);
			long rt = helper.getWritableDatabase().insertOrThrow(
							"Locations", "", cv);
			}
			catch (SQLiteConstraintException e) {
				Errors.appendLog(act, Errors.ERROR, "GpsService",
						"Error inserint linia", e, cv);
				Toast.makeText(act, e.getMessage(), Toast.LENGTH_SHORT)
						.show();
			}
/*			MediaPlayer mp = MediaPlayer.create(
					act.getApplicationContext(),
					R.raw.color);
			mp.start();
			
			Toast.makeText(act, "En servei", Toast.LENGTH_SHORT)
			.show();

/*			Errors.appendLog(act, Errors.ERROR, "GpsService",
					"Prova en cas d'error" );
			Toast.makeText(act, "No es un error, és una prova", Toast.LENGTH_SHORT)
					.show();  */ 

		}

		public void onProviderDisabled(String provider) {
				list.add("Desconnectat");
			
		}

		public void onProviderEnabled(String provider) {
			list.add("Connectat");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
		
	}

	public void onCreate() {
		super.onCreate();

		helper = new OrdersHelper(this);
		gps = new Gps(helper);
		
		
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.checkmark;
		CharSequence tickerText = "Hello";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "My notification";
		CharSequence contentText = "Hello World!";
		Intent notificationIntent = new Intent(this, EditPreferences.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		mNotificationManager.notify(1, notification);
		
		
		
		
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 12000, 0, gps);
		boolean enabled = locationManager
		  .isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!enabled) {
		  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		  startActivity(intent);
		} 
		pollForUpdates();
	}

	
	private void pollForUpdates() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {


			}
		}, 0, UPDATE_INTERVAL);
		MediaPlayer mp = MediaPlayer.create(
				getApplicationContext(),
				R.raw.fi_transmissio);
		mp.start();

		Log.i(getClass().getSimpleName(), "Timer started.");

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		helper.close();
		locationManager.removeUpdates(gps);
		if (timer != null) {
			timer.cancel();
		}
		Log.i(getClass().getSimpleName(), "Timer stopped.");
		MediaPlayer mp = MediaPlayer.create(
				getApplicationContext(),
				R.raw.fi_transmissio);
		mp.start();


	}

	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public class MyBinder extends Binder {
		GpsService getService() {
			return GpsService.this;
		}
	}

	public List<String> getWordList() {
		return list;
	}
}
