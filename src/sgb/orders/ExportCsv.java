package sgb.orders;

import android.app.Activity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

class TransferListener implements FTPDataTransferListener {
	ExportCsv expcsv;

	int factor;

	TransferListener(ExportCsv expcsv, long fileLength) {
		this.expcsv = expcsv;
		factor = (int) (fileLength);
	}

	public void started() {

		expcsv.setProgres(0, ExportCsv.INI_TRAMESA);
	}

	public void transferred(int length) {
		expcsv.setProgres(factor * length, 0);
	}

	public void completed() {
		expcsv.setProgres(0, ExportCsv.FI_TRAMESA);
	}

	public void aborted() {
		expcsv.setProgres(0, ExportCsv.FI_TRAMESA);
	}

	public void failed() {
		// Transfer failed
	}

}

public class ExportCsv extends Thread {
	Activity act;
	FTPClient ftp;
	String PROGRAMA = "ExportCSV";
	int comandesAEnviar;
	String perSD = "";

	static final int INI_EXPORTACIO = -1;
	static final int INI_TRAMESA = -2;
	static final int FI_EXPORTACIO = -3;
	static final int FI_TRAMESA = -4;

	ExportCsv(Activity act, String perSD) {
		this.act = act;
		this.perSD = perSD;
	}

	void setProgres(final int pos, final int ope) {

		act.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (pos >= 0)

					act.setProgress(pos);

				if (ope < 0)
					switch (ope) {
					case ExportCsv.FI_TRAMESA:
						act.setProgressBarVisibility(false);
						MediaPlayer mp = MediaPlayer.create(
								act.getApplicationContext(),
								R.raw.fi_transmissio);
						mp.start();
						break;
					case ExportCsv.INI_EXPORTACIO:
						act.setProgressBarVisibility(true);
						break;
					case ExportCsv.INI_TRAMESA:
						break;
					case ExportCsv.FI_EXPORTACIO:
						// Toast.makeText(act.getApplicationContext(),
						// "Fi exportacio", Toast.LENGTH_SHORT).show();
						break;
					}
			}

		});
	}

	void enviar(String fitxers[],String fitxerClient) {

		Prefs prefs = Prefs.getInstance(act.getApplicationContext());
		String carpeta = prefs.getString("ftpFolder", "");
		String host = prefs.getString("ftpServer", "");
		String userName = prefs.getString("ftpUser", "??");
		String password = prefs.getString("ftpPwd", "??");
		String port = prefs.getString("ftpPort", "");
		if (port.length() <= 1)
			port = "21";
		prefs.close();

		if (host.length() <= 0) {
			host = "ftp.reset.cat";
			userName = "dem01";
			password = "dem01pda";
		}

		try {
			ftp = new FTPClient();
			ftp.setPassive(true);
			ftp.connect(host,Integer.parseInt(port));
			ftp.login(userName, password);


			ftp.setType(FTPClient.TYPE_BINARY);
			if (!carpeta.isEmpty())
				ftp.changeDirectory(carpeta);

			for (int i = 0; i < fitxers.length; i++) {
				String file = fitxers[i];
				setProgres(0, 0);
				File fp = new java.io.File(file);
				if (fp.length() > 0) {
					ftp.upload(fp, new TransferListener(this, fp.length()));
				}

			}
			if (fitxerClient != null) {
				File fp = new java.io.File(Utilitats.getWorkFolder(act, Utilitats.EXPORT)
						+ "/" + fitxerClient);
				ftp.upload(fp, new TransferListener(this, fp.length()));
			}
			ftp.changeDirectory("/");
			ftp.disconnect(true);
		} catch (FTPAbortedException e) {
			Log.w("Error", " " + e.getMessage());
		} catch (FTPDataTransferException e) {
			Log.w("Error", " " + e.getMessage());
		} catch (FTPException e) {
			Log.w("Error", " " + e.getMessage());

		}

		catch (FTPIllegalReplyException ex) {
			Toast.makeText(act.getApplicationContext(),
					ex.getMessage() + " Codi:" + ex.getCause(),
					Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
		} catch (IOException e) {
			Log.w("Error", " " + e.getMessage());

		} finally {
			
		}

	}

	Boolean comprovar(String fitxers[]) {

		Prefs prefs = Prefs.getInstance(act.getApplicationContext());
		String host = prefs.getString("ftpServer", "");
		String userName = prefs.getString("ftpUser", "??");
		String password = prefs.getString("ftpPwd", "??");
		String port = prefs.getString("ftpPort", "");
		if (port.length() <= 1)
			port = "21";
		prefs.close();

		if (host.length() <= 0) {
			host = "ftp.reset.es";
			userName = "dem01";
			password = "dem01pda";
		}

		try {
			ftp = new FTPClient();
			ftp.setPassive(true);
			ftp.connect(host,Integer.parseInt(port));
			ftp.login(userName, password);
			ftp.setType(FTPClient.TYPE_BINARY);

			for (int i = 0; i < fitxers.length; i++) {
				String file = fitxers[i];

				try {
					FTPFile list[] = ftp.list(file);
					if (list.length != 1)
						return false;
				} catch (IllegalStateException e) {
					return false;
				} catch (FTPListParseException e) {
					return false;
				}

			}

		} catch (FTPAbortedException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv", "FTP Abort", e,
					null, true);
			return false;

		} catch (FTPDataTransferException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv",
					"FTP Transfer Exception", e, null, true);
			return false;
		} catch (FTPException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv", "FTP Exception",
					e, null, true);
			return false;

		}

		catch (FTPIllegalReplyException ex) {
			Toast.makeText(act.getApplicationContext(),
					ex.getMessage() + " Codi:" + ex.getCause(),
					Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
			return false;
		} catch (IOException e) {
			Errors.appendLog(act, Errors.ERROR, "ExportCsv",
					"FTP Illegal Reply", e, null, true);

			return false;

		}

		return true;
	}

	void marcar() {
		OrdersHelper helper = new OrdersHelper(act);
		helper.getWritableDatabase().execSQL(
				"update Clients set state='E' where state = 'F' ");		
		helper.getWritableDatabase().execSQL(
						"update Linia set state='E' where state = 'F' ");
		helper.getWritableDatabase().execSQL(
				"update Cap set state='E' where state = 'F' ");
		helper.getWritableDatabase().execSQL(
				"update Clients  set comandespendents = 0 where comandespendents <> 0 ");
		
		helper.close();
	}

	public String GeneraFitxerCsv(OrdersHelper helper, String taula,
			String prefix, String num) throws IOException {
		FileOutputStream fcap = null;
		BufferedOutputStream cap = null;
		String fitxer = prefix + "_" + num + ".dat";
		File wcap = new File(Utilitats.getWorkFolder(act, Utilitats.EXPORT)
				+ "/" + fitxer);
		wcap.createNewFile();
		fcap = new FileOutputStream(wcap);
		cap = new BufferedOutputStream(fcap);
		String sql = "select * from " + taula + "  where state = 'F' ";
		Cursor ctr = helper.execSQL(sql);

		DatabaseProperties dbProp = new DatabaseProperties(helper);
		TableProperties tbProp = dbProp.getTableProperties(taula);
		Iterator<TableFieldProperties> e1 = tbProp
				.getTableFieldProperties().iterator();
		while (e1.hasNext()) {
			TableFieldProperties st = (TableFieldProperties) e1.next();
			String nm = st.getName();
			cap.write( (nm + ";")
					.getBytes());

		}
		cap.write("\n".getBytes());

		if (ctr.getCount() > 0) {
			ctr.moveToFirst();
			do {

				Iterator<TableFieldProperties> e = tbProp
						.getTableFieldProperties().iterator();
				while (e.hasNext()) {
					TableFieldProperties st = (TableFieldProperties) e.next();
					String nm = st.getName();
					String vl = ctr.getString(ctr.getColumnIndex(nm));
					if (vl==null) vl="";
					vl.replace("\n", "");
					vl.replace("\r", "");

					cap.write((vl + ";")
							.getBytes());

				}
				cap.write("\n".getBytes());

			} while (ctr.moveToNext() == true);

		}
		cap.close();
		return fitxer;
	}

	public void run() {

		Prefs prefs = Prefs.getInstance(act);
		String serie = prefs.getString("serie", "CCA").trim();
		prefs.close();

		File wcap = null; // Capcalera
		File wdet = null; // Detall
		File wctd = null; // Comptadors
		FileOutputStream fcap = null;
		FileOutputStream fdet = null;
		FileOutputStream fctd = null;
		BufferedOutputStream cap = null;
		BufferedOutputStream det = null;
		BufferedOutputStream ctd = null;
		OrdersHelper helper = null;
		int numLin = 0;
		int progressPos = 0;
		String taula[] = new String[4];
		String fitxer[] = new String[3];

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = formatter.format(date);
		String fitxerClients = null;

		comandesAEnviar = 0;

		try {

			helper = new OrdersHelper(act);
			fitxer[0] = "cap_" + s + ".dat";
			fitxer[1] = "det_" + s + ".dat";
			fitxer[2] = "ctd_" + s + ".dat";

			taula[0] = Utilitats.getWorkFolder(act, Utilitats.EXPORT) + "/"
					+ fitxer[0];
			taula[1] = Utilitats.getWorkFolder(act, Utilitats.EXPORT) + "/"
					+ fitxer[1];
			taula[2] = Utilitats.getWorkFolder(act, Utilitats.EXPORT) + "/"
					+ fitxer[2];

			wcap = new File(taula[0]);
			wdet = new File(taula[1]);
			wctd = new File(taula[2]);

			wcap.createNewFile();
			wdet.createNewFile();
			wctd.createNewFile();

			fcap = new FileOutputStream(wcap);
			cap = new BufferedOutputStream(fcap);

			fdet = new FileOutputStream(wdet);
			det = new BufferedOutputStream(fdet);

			fctd = new FileOutputStream(wctd);
			ctd = new BufferedOutputStream(fctd);

			setProgres(0, ExportCsv.INI_EXPORTACIO); // Activat barra de
														// progress
			String newDate = "xxx";
			String sql = "select * from cap where state = 'F' ";
			Cursor ctr = helper.execSQL(sql);
			if ((comandesAEnviar = ctr.getCount()) > 0) {
				progressPos = 10000 / (ctr.getCount());
				ctr.moveToFirst();
				do {
					setProgres(progressPos * numLin++, 0);
					cap.write("10;A;".getBytes());
					cap.write(serie.getBytes());
					cap.write((";" + ctr.getString(ctr.getColumnIndex("_id")) + ";") // Document
							.getBytes());
					String data_entrega = "";

					try {
						String str_date = ctr.getString(ctr
								.getColumnIndex("data"));
						data_entrega = ctr.getString(ctr
								.getColumnIndex("entrega"));
						DateFormat formatter2;
						Date date2;
						formatter = new SimpleDateFormat("yyyy-MM-dd");
						date = (Date) formatter.parse(str_date);
						SimpleDateFormat dateformat = new SimpleDateFormat(
								"ddMMyyyy");
						newDate = dateformat.format(date);
						date = (Date) formatter.parse(data_entrega);
						data_entrega = dateformat.format(date);
					}

					catch (java.text.ParseException e)

					{
						Errors.appendLog(act, Errors.ERROR, "ExportCsv",
								"Error parsing data", e, null, true);
						return;

					}

					catch (Throwable t) {
						Errors.appendLog(act, Errors.ERROR, "ExportCsv",
								"Error parsing data", t, null, true);
						return;

					}

					cap.write((newDate + ";").getBytes());
					// cap.write("N;;".getBytes());
					cap.write((ctr.getString(ctr.getColumnIndex("notes")) + ";")
							.getBytes());
					cap.write((data_entrega + ";").getBytes());

					cap.write((ctr.getString(ctr.getColumnIndex("client")) + ";") // Subjecte
							.getBytes());
					cap.write((ctr.getString(ctr.getColumnIndex("comentari")) + ";")
							.getBytes());
					cap.write((ctr.getString(ctr.getColumnIndex("entrega_mati")) + ";")
							.getBytes());
					cap.write((ctr.getString(ctr.getColumnIndex("recullen")) + ";")
							.getBytes());
					cap.write((ctr.getString(ctr.getColumnIndex("dtecomercial")) + ";")
							.getBytes());

					cap.write("2;N;56.700001;0;0;0;0;0;0;0;0;0;0;0;56.7;N;N;11:17;0;0;;-1;\n"
							.getBytes());

				} while (ctr.moveToNext() == true);
			}
			ctr.close();

			numLin = 0;
			String doc = "";
			sql = "select Linia.* from Linia LEFT OUTER JOIN Cap on Cap._id = Linia.docum where Cap.state =  'F' ";
			ctr = helper.execSQL(sql);
			if (ctr.getCount() > 0) {
				progressPos = ctr.getCount();
				ctr.moveToFirst();
				do {

					String doc1 = ctr.getString(ctr.getColumnIndex("docum"));
					if (doc == null || doc.length() < 1)
						doc = doc1;
					if (doc.equals(doc1) == false) {
						doc = doc1;
						numLin = 0;
					}

					setProgres(progressPos * numLin++, 0);
					det.write("10;A;".getBytes());
					det.write((serie + ";").getBytes());
					det.write((ctr.getString(ctr.getColumnIndex("docum")) + ";") // Document
							.getBytes());
					det.write((newDate + ";").getBytes());
					det.write((Integer.toString(+numLin) + ";").getBytes()); // 5-Numero
																				// de
																				// Linia
					det.write((ctr.getString(ctr.getColumnIndex("article")) + ";")
							.getBytes());
					String quant = ctr.getString(ctr.getColumnIndex("quant"));
					if (quant != null)
						quant.replace(',', '.');
					String preu = ctr.getString(ctr.getColumnIndex("preu"));
					if (preu != null)
						preu.replace(',', '.');
					String dte = ctr.getString(ctr.getColumnIndex("dte"));
					if (dte != null)
						dte.replace(',', '.');

					det.write((ctr.getString(ctr.getColumnIndex("tipdte")) + ";")
							.getBytes());
					det.write((quant + ";").getBytes());
					det.write((dte + ";").getBytes());

					// double p = ctr.getFloat(ctr.getColumnIndex("preu"));
					det.write((preu + ";;'X$$';") // 7-Quantitat
							.getBytes());
					det.write((ctr.getString(ctr
							.getColumnIndex("article_regal")) + ";").getBytes());
					det.write((ctr.getString(ctr.getColumnIndex("preu_regal")) + ";")
							.getBytes());
					det.write((ctr.getString(ctr
							.getColumnIndex("quantitat_regal")) + ";")
							.getBytes());

					String obs = ctr.getString(ctr.getColumnIndex("notes"));

					// Compte!!!. Preu sense dte.
					det.write((preu + ";") // 7-preu
							.getBytes());
					det.write("N;N;".getBytes());
					det.write((obs + ";") // Observacions
							.getBytes());
					det.write((ctr.getString(ctr.getColumnIndex("codi_obs")) + ";")
							.getBytes());

					det.write("0;14.34;UM;30;0;\n".getBytes());

				} while (ctr.moveToNext() == true);
			}
			ctr.close();

			ctr = helper.execSQL("select * from Comptadors");
			if (ctr.getCount() > 0) {
				ctr.moveToFirst();
				do {
					ctd.write("10;A;;;;;;".getBytes());
					ctd.write((ctr.getString(ctr.getColumnIndex("cca")).trim() + ";;;") // Document
							.getBytes());
					ctd.write((ctr.getInt(ctr.getColumnIndex("cca")) + ";;;") // Document
							.getBytes());
					ctd.write(";;;;;;;;;;\n".getBytes());

				} while (ctr.moveToNext() == true);
			} else
				ctd.write("10;A;;;;;;0;;;0;;;;;;;\n".getBytes());

			ctr.close();

			// Ara gravem el fitxer de control un cop ja em gravat tots els
			// altres.

			String fitx = Utilitats.getWorkFolder(act, Utilitats.EXPORT)
					.getAbsolutePath() + "/oks_" + s;
			FileOutputStream foks = new FileOutputStream(fitx);
			foks.write("oks".getBytes());
			foks.close();
			taula[3] = fitx;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			helper.close();
			if (cap != null)
				cap.close();
			if (det != null)
				det.close();
			if (ctd != null)
				ctd.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			fitxerClients = GeneraFitxerCsv(helper, "Clients", "cli", s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setProgres(0, ExportCsv.FI_EXPORTACIO); // Tancar progress bar
		if (perSD != "S") {
			enviar(taula,fitxerClients);
			comprovar(fitxer);
		}
		marcar();
		Utilitats.ShowModal(act,"Procés Finalitzat");

		act.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(act,
						"S'ha exportat " + comandesAEnviar + " comandes",
						Toast.LENGTH_LONG).show();
			}
		});

	}
}
