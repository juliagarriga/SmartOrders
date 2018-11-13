package sgb.orders;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import it.sauronsoftware.ftp4j.FTPClient;

public class ImpMaps extends Activity {

	MapTables mapTables = new MapTables();

	String perSD = null;
	String PROGRAMA = "ImpExp";

	private ProgressBar progressFtp = null;
	private ProgressBar progressImp = null;
	private OrdersHelper helper = null;
	private TextView progressText;
	int len = 0;
	String file = "";
	String taula = "";
	FTPClient ftp;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressFtp.setProgress(msg.what);
		}
	};

	class NotifCsv implements NotifyCsv {

		@Override
		public void Avisa(final int pos) {
			handler.post(new Runnable() {
				public void run() {
					progressImp.setProgress(pos);
				}
			});

		}

	}

	void DescarregaFitxers(String sd) {
		mapTables.Load(Utilitats.getWorkFolder(this, Utilitats.CONFIG) + "/"
				+ "import.properties");

		FTPTransferListener listener = new FTPTransferListener(progressFtp,
				len, handler);
		Utilitats.Descarrega(ImpMaps.this, mapTables, listener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.impexp);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		helper = new OrdersHelper(this);

		progressFtp = (ProgressBar) findViewById(R.id.progress_ftp);
		progressImp = (ProgressBar) findViewById(R.id.progress_imp);
		progressText = (TextView) findViewById(R.id.progress_text);

		Bundle extras = getIntent().getExtras();
		if (extras != null)
			perSD = extras.getString("PerSD");
		Thread proces = new Thread(new Runnable() {
			public void run() {
				if (Utilitats.isOnline(ImpMaps.this) == false)
					Utilitats.Toast(ImpMaps.this,
							"Atenció. No hi ha connexió a Internet");
				else {
					Boolean load = perSD != null && perSD.equals("S");
					if (load
							|| Utilitats.DescarregaConfiguracio(ImpMaps.this) == true) {
						Utilitats.InicialitzaBBDD(helper);
						Rebre(perSD);
						
						Utilitats.ShowModal(ImpMaps.this,"Importació Finalitzada");
						finish();
					}
				}
				/*
				 * AlertDialog alertDialog = new
				 * AlertDialog.Builder(ImpMaps.this).create();
				 * alertDialog.setTitle("Atenció");
				 * alertDialog.setMessage("Procés de descàrrega finalitzat");
				 * alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int which) {
				 * dialog.dismiss(); } });
				 * 
				 * alertDialog.show();
				 */
			}
		});

		proces.start();

	}

	public void Importar() {

	}

	void Msg(final String missatge) {
		handler.post(new Runnable() {
			public void run() {
				progressText.setText(missatge);
			}
		});
	}

	public void Rebre(final String perSD) {
		String pSD = perSD == null ? "N" : perSD;


		
		
		if (!pSD.equals("S"))
			DescarregaFitxers(pSD);

		// Descarrega Imatges


		for (final Taules tb : mapTables.getTaules()) {
			Csv2Sqlite sq = new Csv2Sqlite(tb);
			progressImp.setMax(sq.ImportCount(tb.getKey(), ImpMaps.this));
			Msg(tb.getKey());
			sq.ImportFile(tb.getKey(), tb.getValue(), helper, ImpMaps.this,
					new NotifCsv());

		} 

		String dir = Utilitats.getWorkFolder(this, Utilitats.IMAGES)
				.getAbsolutePath();
		
		FTPTransferListener listener = new FTPTransferListener(progressFtp,
				len, handler);

		Utilitats.DescarregaImatges(ImpMaps.this,listener);
		
		
		Msg("Procés Finalitzat");

	}

	public void runProces(View arg0) {
		Rebre("tarifes.csv");
	}

	class FTPTransferListener implements FTPListener {

		private int bytesTransferred = 0;
		long lengthFile;

		Context cv;
		final ProgressBar progressBar;
		int bytesReaded = 0;
		int calcul = 0;

		FTPTransferListener(ProgressBar progressBar, long lengthFile,
				Handler progressBarHandler) {
			this.progressBar = progressBar;
			this.lengthFile = lengthFile;

		}

		public void init(final String fileName, long lengthFile) {
			this.progressBar.setProgress(0);
			this.progressBar.setMax((int) lengthFile);
			this.lengthFile = lengthFile;
			handler.post(new Runnable() {
				public void run() {
					progressText.setText(fileName);

				}
			});

		}

		public void started() {
			System.out.println("Transfer started....");
		}

		public void transferred(int length) {
			bytesTransferred += length;
			int MB = 1024 * 1024;
			if (bytesTransferred % MB == 0)
				System.out.println(bytesTransferred / MB + "MB transferred.");
			if (lengthFile != 0) {
				bytesReaded += length;
				// handler.sendMessage(handler.obtainMessage(bytesReaded));

			}

			handler.post(new Runnable() {
				public void run() {
					progressBar.setProgress(bytesReaded);
				}
			});
		}

		@Override
		public void aborted() {

		}

		@Override
		public void completed() {

		}

		@Override
		public void failed() {

		}
	}

	public void completed() {
		System.out.println("Transfer completed.");
	}

	public void aborted() {
		System.out.println("Transfer aborted.");
	}

	public void failed() {
		System.out.println("Transfer failed.");
	}
}
