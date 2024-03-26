package sgb.orders;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ServeiBoot extends Service {
	static int i = 0;
	Location lastLocation = null;
	Location currentLocation = null;
	String lastDateTimeGps = null;
	String lastDateTimeUrl = null;
	int AlarmTry = 0;

	private final int NOTIFICATION_ID = 1010;
	MediaPlayer mp;
	MediaPlayer mpTick;

	private static Timer timer = new Timer();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Controla els canvis de situacio cada 30 segons i si la distància es
		// superior a 10 mts

//		mp = MediaPlayer.create(this, R.raw.ringing);
//		mpTick = MediaPlayer.create(this, R.raw.ringing);
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				// InetAddress.getByName("resettecnic.no-ip.info").isReachable(3000);
				triggerNotification();

			}
		}, 5000, 10 * 60 * 1000); // Cada 5 minuts

		Toast.makeText(this, "Servei en Marxa", Toast.LENGTH_LONG).show();
		Log.d("SERVICEBOOT", "Servicio creado");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Servicio destruido", Toast.LENGTH_LONG).show();
		Log.d("SERVICEBOOT", "Servicio destruido");
	}

	private void triggerNotification() {

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.podcast,
				"Alarma Botiga!", System.currentTimeMillis());

		String s = currentDate() + " ";

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notification_layout);

		if (isOnline() && isIpReachable(" ") == false) {
			AlarmTry++;
			contentView.setImageViewResource(R.id.img_notification,
					R.drawable.phone);
			s = "Atenció No arriba senyal de \nla Càmera de Vigilància";
			contentView.setTextViewText(R.id.txt_notification, s);
			notification.contentView = contentView;

			Intent notificationIntent = new Intent(this, ExecTask.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, PendingIntent.FLAG_IMMUTABLE);
			notification.contentIntent = contentIntent;

			notificationManager.notify(NOTIFICATION_ID, notification);

			if (AlarmTry > 10) // L'alarma no comença a sonar fins a la tercer
			{
				mpTick.start();
				AlarmTry = 0;
			}

			else
				{
				mpTick.start();
				triggerNotification();
				}

		}

		i++;
	}

	private static boolean isIpReachable(String ip) {

		try {
			String myUrl = "http://resettecnic.no-ip.info:81";
			URL url = new URL(myUrl);
			URLConnection myURLConnection = url.openConnection();
			myURLConnection.setConnectTimeout(10000);
			myURLConnection.connect();
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
		/*
		 * boolean exists = true; try { InetAddress address =
		 * InetAddress.getByName("http://resettecnic.no-ip.info:81"); /*
		 * URLConnection connection = new URL(
		 * "http://resettecnic.no-ip.info:81").openConnection(); int
		 * i=connection.getConnectTimeout(); connect(); // if (i == 0) // exists
		 * = false; } catch (IOException e) { exists = false; }
		 * 
		 * return exists;
		 */
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	String currentDate() {
		DateFormat df = new SimpleDateFormat("yyyyMMdd  HH:mm");
		return df.format(new Date(System.currentTimeMillis()));

	}




}
