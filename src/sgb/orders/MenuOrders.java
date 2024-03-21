package sgb.orders;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;

public class MenuOrders extends Activity implements OnClickListener {
	protected static final int SUB_ACTIVITY_REQUEST_CODE = 100;
	static DatabaseProperties databaseProperties;
	String PROGRAMA = "MenuOrders";

	private MediaPlayer soTecla;

	static OrdersHelper helper1 = null;
	static ArrayList<Linia> linies = new ArrayList();

	private static final String TAG = "Orders";


	/* Class My Location Listener */

	class MyLocationListener implements LocationListener

	{

		@Override
		public void onLocationChanged(Location loc)

		{

			loc.getLatitude();

			loc.getLongitude();

			String Text = "My current location is: " +

			"Latitud = " + loc.getLatitude() +

			"Longitud = " + loc.getLongitude();

			Toast.makeText(getApplicationContext(),

			Text,

			Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onProviderDisabled(String provider)

		{

			Toast.makeText(getApplicationContext(), "Gps Disabled",
					Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_upload_documents);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}


//		requestWindowFeature(Window.FEATURE_PROGRESS); // Inicialitzaem
														// progresbar de la
														// caption
		setContentView(R.layout.mainlogo);

		Intent it = new Intent(MenuOrders.this, ExecTask.class);
		startActivity(it);
			
		MenuOrders.this.finish();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}