package sgb.orders;

import android.app.Activity;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

class TransferListener2 implements FTPDataTransferListener {
	ExportRos expcsv;

	int factor;

	TransferListener2(ExportRos expcsv, long fileLength) {
		this.expcsv = expcsv;
		factor = (int) (fileLength);
	}

	public void started() {

		expcsv.setProgres(0, ExportRos.INI_TRAMESA);
	}

	public void transferred(int length) {
		expcsv.setProgres(factor * length, 0);
	}

	public void completed() {
		expcsv.setProgres(0, ExportRos.FI_TRAMESA);
	}

	public void aborted() {
		expcsv.setProgres(0, ExportRos.FI_TRAMESA);
	}

	public void failed() {
		// Transfer failed
	}

}

public class ExportRos extends Thread {
	Activity act;
	FTPClient ftp;
	String PROGRAMA = "ExportCSV";
	int comandesAEnviar;
	String perSD = "";

	static final int INI_EXPORTACIO = -1;
	static final int INI_TRAMESA = -2;
	static final int FI_EXPORTACIO = -3;
	static final int FI_TRAMESA = -4;

	ExportRos(Activity act, String perSD) {
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
					case ExportRos.FI_TRAMESA:
						act.setProgressBarVisibility(false);
						MediaPlayer mp = MediaPlayer.create(
								act.getApplicationContext(),
								R.raw.fi_transmissio);
						mp.start();
						break;
					case ExportRos.INI_EXPORTACIO:
						act.setProgressBarVisibility(true);
						break;
					case ExportRos.INI_TRAMESA:
						break;
					case ExportRos.FI_EXPORTACIO:
						// Toast.makeText(act.getApplicationContext(),
						// "Fi exportacio", Toast.LENGTH_SHORT).show();
						break;
					}
			}

		});
	}

	void enviar(String fitxers[]) {

		Prefs prefs = Prefs.getInstance(act.getApplicationContext());
		String host = prefs.getString("ftpServer", "");
		String userName = prefs.getString("ftpUser", "??");
		String password = prefs.getString("ftpPwd", "??");
		prefs.close();

		if (host.length() <= 0) {
			host = "ftp.reset.es";
			userName = "dem01";
			password = "dem01pda";
		}

		try {
			ftp = new FTPClient();
			ftp.setPassive(true);
			ftp.connect(host);
			ftp.login(userName, password);
			ftp.setType(FTPClient.TYPE_BINARY);

			for (int i = 0; i < fitxers.length; i++) {
				String file = fitxers[i];
				if (file != null) {
					setProgres(0, 0);
					File fp = new java.io.File(file);
					if (fp.length() > 0)
						ftp.upload(fp, new TransferListener2(this, fp.length()));
				}

			}

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

		}

	}

	Boolean comprovar(String fitxers[]) {

		Prefs prefs = Prefs.getInstance(act.getApplicationContext());
		String host = prefs.getString("ftpServer", "");
		String userName = prefs.getString("ftpUser", "??");
		String password = prefs.getString("ftpPwd", "??");
		prefs.close();

		if (host.length() <= 0) {
			host = "ftp.reset.cat";
			userName = "dem01";
			password = "dem01pda";
		}

		try {
			ftp = new FTPClient();
			ftp.setPassive(true);
			ftp.connect(host);
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
				"update Linia set state='E' where state = 'F' ");
		helper.getWritableDatabase().execSQL(
				"update Cap set state='E' where state = 'F' ");
		helper.close();
	}

	public void run() {

		Prefs prefs = Prefs.getInstance(act);
		String serie = prefs.getString("serie", "CCA");
		prefs.close();

		File wcap = null; // Capcalera
		FileOutputStream fcap = null;
		BufferedOutputStream cap = null;
		OrdersHelper helper = null;
		int numLin = 0;
		int progressPos = 0;
		String fitxer = null;

		Date date = new Date();
		SimpleDateFormat dataCap = new SimpleDateFormat("yyyy-MM-ddHH-mm");
		SimpleDateFormat dataEnt = new SimpleDateFormat("yyyy-MM-dd");
		String dataAra = dataCap.format(date);

		comandesAEnviar = 0;

		try {

			helper = new OrdersHelper(act);
			ImprimirLLista.Imprimir(act, helper, "llista.html");

			fitxer = "Tpas400.txt";
			fitxer = Utilitats.getWorkFolder(act, Utilitats.EXPORT) + "/"
					+ fitxer;
			wcap = new File(fitxer);
			wcap.createNewFile();

			cap = new BufferedOutputStream(new FileOutputStream(wcap));

			setProgres(0, ExportRos.INI_EXPORTACIO); // Activat barra de
														// progress
			String newDate = "xxx";
			String sql = "select * from cap  left outer join clients cli  "
					+ " on cli.subjecte = cap.client   where cap.state = 'F'  ";
			Cursor ctr = helper.execSQL(sql);
			if (ctr.getColumnCount() <= 0) {
				Utilitats.Toast(act, "No hi ha comandes pendents d'enviar",
						R.raw.capella);
				return;
			}
			if ((comandesAEnviar = ctr.getCount()) > 0) {
				progressPos = 10000 / (ctr.getCount());
				int pos = 0;
				int count = ctr.getCount();
				while (ctr.moveToNext() == true) {
					pos++;
					setProgres(progressPos * numLin++, 0);
					String out = "C"
							+ String.format(
									"%4s",
									ctr.getString(ctr.getColumnIndex("repres"))
											.substring(1, 3)).replace(' ', '0');
					String client = ctr.getString(ctr.getColumnIndex("client"));

					sql = "select * from Clients where subjecte = '" + client
							+ "' ";
					String tarifa = "";
					Cursor cur = helper.getReadableDatabase().rawQuery(sql,
							null);
					if (cur.getCount() > 0) {
						cur.moveToFirst();
						tarifa = ctr.getString(ctr.getColumnIndex("tarifa"));
					}
					String pt[] = client.split("-");
					client = pt[0];
					out += String.format("%10s", client);
					if (pt.length > 1) {
						String cc = pt[1];
						if (cc.equals("###"))
							cc = "   ";
						out += String.format("%1$" + 3 + "s", cc);
					} else
						out += "   ";
					out += Utilitats.String2Date(
							ctr.getString(ctr.getColumnIndex("data")),
							"dd/MM/yyyy", "yyyy-MM-ddmm:ss");
					out += Utilitats.String2Date(
							ctr.getString(ctr.getColumnIndex("entrega")),
							"dd/MM/yyyy", "yyyy-MM-dd");
					String AmPm = ctr.getString(ctr
							.getColumnIndex("entrega_mati"));
					AmPm = AmPm.split("\\&")[1];
					String EnRe = ctr.getString(ctr.getColumnIndex("recullen"));
					EnRe = EnRe.split("\\&")[1];
					out += AmPm + EnRe + "\r\n";
					cap.write(out.getBytes());
					out = "";

					// Gravar comentaris

					String comentari = "";
					String clau = ctr
							.getString(ctr.getColumnIndex("comentari"));
					String notes = ctr.getString(ctr.getColumnIndex("notes"));
					String coment = "";
					if (clau != null && !clau.contains("?")) {
						comentari = Utilitats.QueryField(helper,
								"select descripcio from taules where clau='"
										+ clau + "'")
								+ " ";
					}
					coment = comentari;
					coment = coment + notes;
					if (coment.length() > 0) {
						if (coment.length() > 255)
							coment = coment.substring(1, 255);
						cap.write(("D" + coment + "\r\n").getBytes());
					}
					String id = ctr.getString(ctr.getColumnIndex("_id"));

					numLin = 0;
					String doc = "";
					// sql =
					// "select *,L.notes lin_notes from Linia L LEFT OUTER JOIN Cap C on C._id = L.docum where L.docum = "
					// + id;
					sql = "select * from Linia where docum = " + id;
					Cursor ctrLin = helper.execSQL(sql);
					if (ctrLin.getCount() > 0) {
						progressPos = ctrLin.getCount();
						ctrLin.moveToFirst();
						do {
							String article = ctrLin.getString(ctrLin
									.getColumnIndex("article"));
							setProgres(progressPos * numLin++, 0);

							String sq = "select * from Articles where article = '"
									+ article + "' ";

							String tar = "tarifa" + tarifa;
							float dte = ctrLin.getFloat(ctrLin
									.getColumnIndex("dte"));
							float preu = ctrLin.getFloat(ctrLin
									.getColumnIndex("preu"));
							float impDte = preu * dte / 100;
							
							int  swCanviPreu = ctrLin.getInt(ctrLin.getColumnIndex("canviPreu"));
							if (swCanviPreu == 0)
								dte=preu=impDte=0;
							
							
							Float tarif = (float) 0;
							Cursor curArt = helper.getReadableDatabase()
									.rawQuery(sq, null);
							if (curArt.getCount() > 0) {
								curArt.moveToFirst();
								tarif = curArt.getFloat(curArt
										.getColumnIndex(tar));
							}
							out = "L";
							out += String.format("%8s", article);
							out += Utilitats.CursorFloatField(ctrLin, "quant",
									"%9s", '0', 1000);
							tarif *= 1000;
							Long s = (long) Math.round(tarif);
							out += String.format("%7s", s.toString()).replace(
									' ', '0');
							impDte *= 1000;
							s = (long) Math.round(impDte);
							out += String.format("%7s", s.toString()).replace(
									' ', '0');
							s = (long)dte*100;
							out += String.format("%5s", s.toString()).replace(
									' ', '0');

/*							out += Utilitats.CursorFloatField(ctrLin, "dte",
									"%5s", '0', 100); */
							s = (long)preu*1000;
							out += String.format("%7s", s.toString()).replace(
									' ', '0');
/*							out += Utilitats.CursorFloatField(ctrLin, "preu",
									"%7s", '0', 1000); */
							out += "\r\n";
							cap.write(out.getBytes());
							out = "";

							clau = ctrLin.getString(ctrLin
									.getColumnIndex("codi_obs"));
							notes = ctrLin.getString(ctrLin
									.getColumnIndex("notes"));
							coment = "";
							comentari = "";
							if (clau != null && !clau.contains("?")) {
								comentari = Utilitats.QueryField(helper,
										"select descripcio from taules where clau='"
												+ clau + "'")
										+ " ";
							}
							coment = comentari;
							coment = coment + notes;
							coment = Utilitats.padRight(coment, 35 * 3 + 10);
							out = "";
							for (int i = 0; i < 3; i++) {
								String tm = coment.substring(i * 35,
										i * 35 + 35);
								tm = Utilitats.rtrim(tm);
								if (tm.length() > 0) {
									out = "O" + tm + "\r\n";
									cap.write(out.getBytes());
									out = "";

								}
							}

						} while (ctrLin.moveToNext() == true);
						ctrLin.close();
					}

				}
				ctr.close();
			}

			String s = " XXX";

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			helper.close();
			if (cap != null)
				cap.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setProgres(0, ExportRos.FI_EXPORTACIO); // Tancar progress bar
		if (perSD != "S") {
			String mail = Utilitats.getConfig(act, "email");
			if (mail == null)
				Errors.appendLog(act, Errors.AVIS, "ExportRos",
						"No s'ha definit un correu electrònic", true);
			else {
				Utilitats.enviarFitxerPerMail(act, helper, fitxer, mail);
				marcar();
				Utilitats.Toast(act, "S'ha exportat " + comandesAEnviar
						+ " comandes");
			}
			// enviar(taula);
			// comprovar(fitxer);
		}

	}
}
