package sgb.orders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

public class Utilitats {
	static Cursor curEsp;
	static Cursor curArt;
	static Cursor curCli;
	static Cursor tmpCur;
	static double preu = 0;
	static double preutarifa = 0;
	static double dte = 0;
	static String tipdte;

	static boolean mExternalStorageAvailable = false;
	static boolean mExternalStorageWriteable = false;

	private static int sTheme;

	public final static int ACT_FAM_RETURN = 123;
	public final static int RETURN_ARTICLE = 124;
	public final static int REQUEST_ARTICLE = 125;

	public final static int THEME_DEFAULT = 0;
	public final static int THEME_WHITE = 1;
	public final static int THEME_BLUE = 2;

	public final static int SQL_TEXT = 0;
	public final static int SQL_REAL = 1;
	public final static int SQL_INT = 2;

	public final static String CONFIG = "config";
	public final static String BACKUP = "backup";
	public final static String WORK = "work";
	public final static String IMPORTED = "imported";
	public final static String EXPORTED = "exported";
	public final static String IMPORT = "import";
	public final static String EXPORT = "export";
	public final static String LOGS = "logs";
	public final static String IMAGES = "pictures";

	static public class TPreus {
		public String article;
		public String familia;
		public String linia;
		public String descripcio;

		public String tipDte; // Tipus de descompte a aplicar
								// '=','-','+',' ','%'
		public double quantitat;
		public double dte; // Valor del dte o import
		public double preuBase; // Preu base sobre el que s'aplica el dte
		public double preuTarifa; // Preu seguin la tarifa del client
		public double preuNet;
		public double quantitatRegal;
		public String modeRegal; // Si '*' dividim la quantitat per la
									// quantitatRegal
		public String articleRegal;
	};

	static public Cursor Query(OrdersHelper helper, String sql) {
		tmpCur = helper.getWritableDatabase().rawQuery(sql, new String[] {});
		if (tmpCur.getCount() > 0) {
			tmpCur.moveToFirst();
			return tmpCur;
		} else
			return null;

	}

	public static Boolean InicialitzaBBDD(final OrdersHelper helper) {
		String db[] = new String[] { "contactes", "comentaris", "taules",
				"families", "linies", "rutes", "efectes", "tarifes",
				"PreusEsp", "Locations", "Precomanda", "Articles", "GrupCli",
				"Clients", "CliRuta" };
		for (int i = 0; i < db.length; i++) {
			helper.getWritableDatabase().delete(db[i], null, null);

		}
		return true;
	}

	public static String Format(String pattern, double s) {
		DecimalFormat myFormatter = new DecimalFormat(pattern);
		return myFormatter.format(s);
	}

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	static public String QueryField(OrdersHelper helper, String sql,
			String field) {
		if (Query(helper, sql) == null)
			return null;
		else
			return tmpCur.getString(tmpCur.getColumnIndex(field));

	}

	static public String QueryField(OrdersHelper helper, String sql) {
		if (Query(helper, sql) == null)
			return null;
		else
			return tmpCur.getString(0);

	}

	static public String CursorFloatField(Cursor c, String field,
			String format, char fillChar, int mult) {

		String num = c.getString(c.getColumnIndex(field));
		Double fnum = 0.0;
		if (num != null) {
			try {
				fnum = Double.parseDouble(num);
			} catch (NumberFormatException e) {
			}
			;
			fnum *= mult;
			Long n = new Long(fnum.longValue());
			num = n.toString();
		} else
			num = " ";
		String out = String.format(format, num).replace(' ', fillChar);

		return out;
	}

	static public String CursorField(Cursor c, String field, String format,
			char fillChar) {

		String out = String
				.format(format, c.getString(c.getColumnIndex(field))).replace(
						' ', fillChar);
		return out;
	}

	static public String String2Date(String input, String inputFormat,
			String outputFormat) {
		String rt = "";
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(inputFormat);
			Date date = formatter.parse(input);
			SimpleDateFormat formatOut = new SimpleDateFormat(outputFormat);
			rt = formatOut.format(date);
		} catch (java.text.ParseException e)

		{
			Errors.appendLog(null, Errors.ERROR, "ExportCsv",
					"Data incorrecta", e, null, true);

		}
		return rt;
	}

	static String getProfile(Activity act, Properties propInp, String file,
			String text) {
		String ret = "_";
		Properties prop = propInp;
		if (prop == null)
			prop = new Properties();
		InputStream input = null;

		try {
			String dir = Utilitats.getWorkFolder(act, Utilitats.CONFIG)
					.getAbsolutePath() + "/" + file;

			input = new FileInputStream(dir);
			prop.load(input);
			ret = prop.getProperty(text);

		} catch (IOException ex) {
			Errors.appendLog(act, Errors.ERROR, "getProfile",
					"Error Carregant properties", ex, null, true);
		}
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ret;
	}

	void setProfile(String file, String text, String value) {
		Properties prop = new Properties();
		OutputStream output = null;

		try {

			output = new FileOutputStream(file);
			prop.setProperty(text, value);
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	static void Toast(final Activity act, final String txt, boolean so) {
		Toast(act, txt, R.raw.capella);

	}

	static void Toast(final Activity act, final String txt, int so) {
		if (so > 0)
			so(act, so);
		Toast(act, txt);

	}

	static void Toast(final Activity act, final String txt) {
		act.runOnUiThread(new Runnable() {
			public void run() {

				Toast toast = Toast.makeText(act, txt, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, -100);
				toast.show();

			}
		});

	}

	static public void so(Context ct, int i) {
		final MediaPlayer mp = MediaPlayer.create(ct, i);
		mp.start();

	}

	static public void callPhone(Activity act, String number) {
		try {
			Intent intent = new Intent(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:" + number));
			act.startActivity(intent);
		} catch (Exception e) {
			Errors.appendLog(act, Errors.ERROR, "CallPhone", e.getMessage(),
					true);
		}
	}

	static public boolean isOnline(Activity act) {
		ConnectivityManager cm = (ConnectivityManager) act
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			MediaPlayer mp = MediaPlayer.create(act.getApplicationContext(),
					R.raw.color);
			mp.start();

			return true;
		}
		Toast.makeText(
				act.getApplicationContext(),
				"No hi ha connexió Internet. Revisi xarxa i torni a intentar-ho",
				Toast.LENGTH_LONG).show();
		MediaPlayer mp = MediaPlayer.create(act.getApplicationContext(),
				R.raw.capella);
		mp.start();
		return false;
	}

	static File comprovaFolder(String dir) {
		File folder = new File(Environment.getExternalStorageDirectory() + dir);

		if (!folder.exists())
			if (folder.mkdirs() == false)
				return null;
		return folder;
	}

	static public void checkSD() {

		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	public static File getWorkFolder(Activity act, String fold) {
		if (comprovaFolder("/sgb.orders") == null) {
			Errors.appendLog(act, Errors.ERROR, "ComprovaSD",
					"No s'ha pogut crear directori sdcard/sgb.orders", null,
					null, true);
			return null;
		} else {
			if (fold.equals(Utilitats.CONFIG))
				return comprovaFolder("/sgb.orders/" + Utilitats.CONFIG);
			if (fold.equals(Utilitats.WORK))
				return comprovaFolder("/sgb.orders/" + Utilitats.WORK);
			if (fold.equals(Utilitats.BACKUP))
				return comprovaFolder("/sgb.orders/" + Utilitats.BACKUP);
			if (fold.equals(Utilitats.IMPORT))
				return comprovaFolder("/sgb.orders/" + Utilitats.IMPORT);
			if (fold.equals(Utilitats.EXPORT))
				return comprovaFolder("/sgb.orders/" + Utilitats.EXPORT);
			if (fold.equals(Utilitats.IMPORTED))
				return comprovaFolder("/sgb.orders/" + Utilitats.IMPORTED);
			if (fold.equals(Utilitats.EXPORTED))
				return comprovaFolder("/sgb.orders/" + Utilitats.EXPORTED);
			if (fold.equals(Utilitats.LOGS))
				return comprovaFolder("/sgb.orders/" + Utilitats.LOGS);
			if (fold.equals(Utilitats.IMAGES))
				return comprovaFolder("/sgb.orders/" + Utilitats.IMAGES);
		}
		return null;
	}

	static public void enviarComandaPerMail(Activity act, OrdersHelper helper,
			long docum) {
		Boolean error = false;
		StringBuffer bf = new StringBuffer();
		String sql = "select * from Linia where docum = " + docum;
		Cursor cur = helper.execSQL(sql);
		Cursor curCap = helper.getWritableDatabase().rawQuery(
				"select * from Cap where _id=?",
				new String[] { Long.toString(docum) });
		if (curCap.getCount() < 0) {
			Errors.appendLog(act, Errors.ERROR, "EnviarPerMail", docum
					+ ": No s'ha trobat Document", null, null, true);
			error = true;
		}
		curCap.moveToFirst();
		String client = "";
		Cursor curCli = null;
		if (error == false) {
			client = curCap.getString(curCap.getColumnIndex("client"));
			curCli = helper.getWritableDatabase().rawQuery(
					"select * from Clients where subjecte=?",
					new String[] { client });
			if (curCli.getCount() < 0) {
				Errors.appendLog(act, Errors.ERROR, "EnviarPerMail", client
						+ ": No s'ha trobat Client", null, null, true);
				error = true;
			}
		}

		if (error == false) {
			curCli.moveToFirst();
			String mail = curCli.getString(curCli.getColumnIndex("mail"));

			mail = "salvador@reset.es";

			double total = 0;
			bf.append(String.format("Document : %d\n", docum));
			bf.append(String.format("Client   : %s - %s\n", client,
					curCli.getString(curCli.getColumnIndex("nom"))));
			bf.append(String.format("Data     : %s\n",
					curCap.getShort(curCap.getColumnIndex("entrega"))));
			bf.append("________________________________________________________________________\n");
			bf.append("Quantitat Article/Servei                                    			   \n");
			bf.append("________________________________________________________________________\n");
			if (cur.getCount() > 0) {
				int iPreu = cur.getColumnIndex("preu");
				int iQuant = cur.getColumnIndex("quant");
				int iDte = cur.getColumnIndex("dte");
				int iArt = cur.getColumnIndex("article");

				while (cur.moveToNext()) {
					double p = cur.getDouble(iPreu);
					double q = cur.getDouble(iQuant);
					String article = cur.getString(iArt);
					Cursor curArt = helper.getWritableDatabase().rawQuery(
							"select descripcio from Articles where article=?",
							new String[] { article });
					if (curArt.getCount() < 0) {
						Errors.appendLog(act, Errors.ERROR, "EnviarPerMail",
								article + ": No s'ha trobat Article", null,
								null, true);
						error = true;
					}
					curArt.moveToFirst();

					bf.append(String.format("| %3.2f | %-70.70s\n", q, curArt
							.getString(curArt.getColumnIndex("descripcio")), p));
					String obs = cur.getString(cur.getColumnIndex("notes"));
					if (obs != null)
						bf.append(String.format("| %3.2f | %-70.70s\n", q, obs));
				}
			}
			bf.append("________________________________________________________________________\n");
			bf.append("							 Si-us-plau \n");
			bf.append("Respongui a aquest mail afegint el seu nom al text de l'assumpte com a\n");
			bf.append("conformitat dels serveis realitzats o productes entregats \n");
			String s = bf.toString();
			if (mail != null) {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						new String[] { mail + ";salvador@reset.cat" });
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Document : " + docum
								+ " Conforme de Persona autoritzada : [Nom] ");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						bf.toString());

				// emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file));
				try {
					act.startActivity(Intent.createChooser(emailIntent,
							"Enviar correu mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(act,
							"There are no email clients installed.",
							Toast.LENGTH_SHORT).show();
				}

			}
		}
		curCap.close();
		curCli.close();
		cur.close();
	}

	static public void enviarFitxerPerMail(Activity act, OrdersHelper helper,
			String fitxer, String mail) {

		if (mail != null) {
			Intent emailIntent = new Intent(
					android.content.Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("plain/text");
			emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { mail });
			// new String[] { mail + ";salvador@reset.cat" });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"Tramesa Informe de Vendes ");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					"Cos del missatge");
			String fitxer2 = Utilitats.getWorkFolder(act, Utilitats.WORK)
					+ "/llista.html";
			ArrayList<Uri> uris = new ArrayList<Uri>();
			Uri uri = Uri.parse("file://" + fitxer);
			// emailIntent.putExtra(Intent.EXTRA_STREAM,uri);
			Uri uri2 = Uri.parse("file://" + fitxer2);
			uris.add(uri);
			uris.add(uri2);
			emailIntent.putExtra(Intent.EXTRA_STREAM, uris);

			// emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file));
			try {
				act.startActivity(Intent.createChooser(emailIntent,
						"Enviar correu mail..."));
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(act, "There are no email clients installed.",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */

	public static void changeToTheme(Activity activity, int theme) {
		sTheme = theme;
		// activity.finish();

		// activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity) {
		switch (sTheme) {
		default:
		case THEME_DEFAULT:
			break;
		case THEME_WHITE:
			activity.setTheme(R.style.Theme_White);
			break;
		case THEME_BLUE:
			activity.setTheme(R.style.Theme_Blue);
			break;
		}
	}

	static double getPreu() {
		return preu;

	}

	static double getPreuTarifa() {
		return preutarifa;

	}

	static Cursor getCursorArt() {
		return curArt;
	}

	static void inicialitzaPrecomandes(OrdersHelper helper, String article) {
		if (article == null) {
			helper.getWritableDatabase().execSQL(
					"update Articles set servit = 0 where servit <> 0");
			helper.getWritableDatabase().execSQL(
					"update Precomanda set servit = 0 where servit <> 0");
			helper.getWritableDatabase()
					.execSQL(
							"update PreusEsp set servit = 0 where servit <> 0 and subjecte='OFERTES' ");
		} else {
			helper.getWritableDatabase().execSQL(
					"update Articles set servit = 0 where servit <> 0 and article ='"
							+ article + "' ");
			helper.getWritableDatabase().execSQL(
					"update Precomanda set servit = 0 where servit <> 0  and article ='"
							+ article + "' ");
			helper.getWritableDatabase()
					.execSQL(
							"update PreusEsp set servit = 0 where servit <> 0 and subjecte='OFERTES'  and article ='"
									+ article + "' ");
		}

	}

	static Boolean readArticles(OrdersHelper helper, String article, TPreus art) {
		String sql = "select * from articles where article = '" + article
				+ "' ";
		curArt = helper.execSQL(sql);
		if (curArt.getCount() > 0) {
			curArt.moveToFirst();
			if (art != null) {
				art.article = curArt.getString(curArt
						.getColumnIndexOrThrow("article"));
				art.descripcio = curArt.getString(curArt
						.getColumnIndexOrThrow("descripcio"));
				art.familia = curArt.getString(curArt
						.getColumnIndexOrThrow("familia"));
				art.linia = curArt.getString(curArt
						.getColumnIndexOrThrow("linia"));
			}
			return true;
		}
		curArt.close();
		return false;
	}

	static String calculaTotals(OrdersHelper helper, long docum) {
		double total = calculTotals(helper, docum);
		ContentValues cv = new ContentValues();
		cv.put("_id", docum);
		NumberFormat formatter = new DecimalFormat("#0.00");
		String total1 = formatter.format(total);
		cv.put("value", total);
		helper.update("Cap", "_id", cv);

		// TODO Falta validar la gravació
		return total1;
	}

	static double getDouble(Cursor cur, int pos) {
		return cur.getDouble(pos);
	}

	static double calculTotals(OrdersHelper helper, long docum) {

		String sql = "select A.iva,C.dtecomercial,C.dtepp,C.dtegrup,tipus_factura,numeric1,numeric2,L.quant,L.preu,preunet,dte,tipdte from Linia L "
				+ " LEFT OUTER JOIN Cap C ON C._id  = L.docum "
				+ " LEFT OUTER JOIN Articles A ON A.article = L.article "
				+ " LEFT OUTER JOIN Taules T ON T.claugest    = A.iva "
				+ " LEFT OUTER JOIN Clients S ON S.subjecte = C.client "
				+ " where docum = " + docum;

		double totfac = 0;
		Cursor cur = helper.execSQL(sql);
		int items = 0;
		int lines = 0;
		if (cur.getCount() > 0) {
			int iTipFac = cur.getColumnIndex("tipus_factura");
			int iPreu = cur.getColumnIndex("preu");
			int iDtePP = cur.getColumnIndex("dtepp");
			int iDteCom = cur.getColumnIndex("dtecomercial");
			int iDteGrup = cur.getColumnIndex("dtegrup");
			int iQuant = cur.getColumnIndex("quant");
			int iTipIva = cur.getColumnIndex("iva");
			int iDte = cur.getColumnIndex("dte");
			int iTipDte = cur.getColumnIndex("tipdte");
			int iPreuNet = cur.getColumnIndex("preunet");
			int iIva = cur.getColumnIndex("numeric1");
			int iRe = cur.getColumnIndex("numeric2");
			TPreus pr = new TPreus();

			while (cur.moveToNext()) {
				lines++;
				pr.preuBase = getDouble(cur, iPreu);
				pr.dte = getDouble(cur, iDte);
				pr.tipDte = cur.getString(iTipDte);
				pr.quantitat = getDouble(cur, iQuant);
				pr.preuNet = getDouble(cur, iPreuNet);

				Utilitats.reCalculaPreus(pr);
				items += pr.quantitat;
				double dtepp = getDouble(cur, iDtePP);
				double dtecom = getDouble(cur, iDteCom);
				double dtegrup = getDouble(cur, iDteGrup);

				pr.preuNet -= pr.preuNet * dtecom / 100;
				pr.preuNet -= pr.preuNet * dtepp / 100;
				pr.preuNet -= pr.preuNet * dtegrup / 100;

				String tipfac = cur.getString(iTipFac);
				String tipiva = cur.getString(iTipIva);
				double re = getDouble(cur, iRe);
				double iva = getDouble(cur, iIva);
				if (tipfac.equals("W0")) {
					re = 0;
					iva = 0;
				}
				if (tipfac.equals("W1"))
					re = 0;
				double impIva = pr.quantitat * pr.preuNet * (iva + re) / 100;
				totfac += pr.quantitat * pr.preuNet + impIva;
			}
		}
		cur.close();
		return totfac;
	}

	static Cursor readSubjecte(OrdersHelper helper, String subjecte) {
		String sql = "select * from Clients where subjecte = '" + subjecte
				+ "' ";
		curCli = helper.execSQL(sql);
		if (curCli.getCount() > 0) {
			curCli.moveToFirst();
			return curCli;
		}
		curCli.close();
		return null;

	}

	static Cursor readGrupCli(OrdersHelper helper, String grupCli) {
		String sql = "select * from GrupCli where grupcli = '" + grupCli + "' ";
		curCli = helper.execSQL(sql);
		if (curCli.getCount() > 0) {
			curCli.moveToFirst();
			return curCli;
		}
		curCli.close();
		return null;

	}

	static String readState(OrdersHelper helper, long document) {
		String sql = "select state from Cap where _id = " + document;
		curCli = helper.execSQL(sql);
		if (curCli.getCount() > 0) {
			curCli.moveToFirst();
			return curCli.getString(0);
		}
		return " ";

	}

	static Boolean getDte(Activity act, OrdersHelper helper, String subjecte,
			String tarifa, String article, String linia, String familia,
			double quantitat, TPreus preus) {

		/*
		 * Llegim per Ordre : Subjecte -Primer el client i després el comodin
		 * amb codi 'z$$$' Tipus - Tipus (A-Article,F-Familia,L-Linia) Article -
		 * Pot correspondre a Un codi d'article,linia o familia
		 * 
		 * 
		 * Quan troba el primer registre que coincideix llavors salta amb true i
		 * omple TPreus
		 */

		preus.tipDte = " ";
		preus.preuBase = 0.0;
		preus.preuTarifa = 0;
		preus.preuNet = 0;

		String grupcli = subjecte.split("-")[0];

		String sql = "select * from PreusEsp where (subjecte = '~OFERTES' or subjecte = '"
				+ grupcli
				+ "') and "
				+ " ( (tipus = 'A' and objecte = '"
				+ article
				+ "') or (tipus = 'L' and objecte = '"
				+ familia
				+ "') or  (tipus = 'F' and objecte = '"
				+ linia
				+ "')) "
				+ " order by Subjecte,Tipus,Ordre";
		String param[] = {};

		curEsp = helper.getReadableDatabase().rawQuery(sql, param);
		if (curEsp.getCount() > 0) {
			while (curEsp.moveToNext() == true) {
				String tip =  curEsp.getString(curEsp.getColumnIndex("tipus"));
				String objecte =  curEsp.getString(curEsp.getColumnIndex("objecte"));
				String subj =  curEsp.getString(curEsp.getColumnIndex("subjecte"));
				double minim = curEsp.getFloat(curEsp.getColumnIndex("minim"));
				if (minim == 1)
					minim = 0;
				if (quantitat >= minim) {
					preus.tipDte = curEsp.getString(curEsp
							.getColumnIndex("tipdte"));
					preus.dte = curEsp.getFloat(curEsp.getColumnIndex("dte"));
					preus.quantitatRegal = curEsp.getFloat(curEsp
							.getColumnIndex("QRegal"));
					preus.articleRegal = curEsp.getString(curEsp
							.getColumnIndex("ARegal"));
					preus.modeRegal = curEsp.getString(curEsp
							.getColumnIndex("MRegal"));
					if (preus.modeRegal != null && preus.modeRegal.equals("*")) {
						preus.quantitatRegal = (int) (quantitat / preus.quantitatRegal);

					}
					return true;
				}
			}
		}

		return false;
	}

	static TPreus reCalculaPreus(TPreus preus) {
		if (preus.tipDte.equals("+")) {
			preus.preuNet = preus.preuBase + (preus.dte);
		} else if (preus.tipDte.equals("-")) {
			preus.preuNet = preus.preuBase - (preus.dte);
		} else if (preus.tipDte.equals("=")) {
			preus.preuNet = preus.dte;
			preus.preuBase = preus.dte;
		} else // "%"
		if (preus.preuBase * preus.dte != 0)
			preus.preuNet = preus.preuBase - (preus.preuBase * preus.dte / 100);
		else
			preus.preuNet = preus.preuBase;
		return preus;
	}

	static TPreus readPreus(Activity act, OrdersHelper helper, String subjecte,
			String tarifa, String article, String familia, String linia,
			double quantitat) {
		TPreus preus = new TPreus();

		/* Mirem si el client te descompte línia */
		Cursor cSub = Utilitats.readSubjecte( helper, subjecte);
		preus.dte = 0;
		if (cSub.getCount() > 0)
			preus.dte = cSub.getFloat( cSub.getColumnIndex("dtegrup"));
		cSub.close();

		if (getDte(act, helper, subjecte, tarifa, article, familia, linia,
				quantitat, preus)) {
			so(act, R.raw.insert);
		}
		if (tarifa == null || tarifa.compareTo("1") < 0
				|| tarifa.compareTo("6") > 0)
			tarifa = "1";
		String sql = "select * from tarifes where tarifa = ? and article = ?";
		String[] param2 = { tarifa, article };
		curEsp = helper.getReadableDatabase().rawQuery(sql, param2);
		if (curEsp.getCount() > 0) {
			curEsp.moveToNext();
			preus.preuTarifa = curEsp.getFloat(curEsp.getColumnIndex("preu"));
			preus.preuBase = preus.preuTarifa;
		} else {
			String txt = "tarifa" + tarifa;
			sql = "select * from articles where article = ?";
			String param3[] = { article };
			curEsp = helper.getReadableDatabase().rawQuery(sql, param3);
			if (curEsp.getCount() > 0) {
				curEsp.moveToNext();
				if (curEsp.getColumnIndex(txt) > 0) {
					preus.preuTarifa = curEsp.getFloat(curEsp
							.getColumnIndex(txt));
					preus.preuBase = preus.preuTarifa;
				} else
					Utilitats.Toast(act, "No es troba la tarifa " + txt);
			}

		}
		preus = reCalculaPreus(preus);
		return preus;
	}

	static Double calculaPreu(Double wpreu, Double wdte, String wtipdte) {

		Double preuFinal = 0.0;
		if (wtipdte.equals("%"))
			preuFinal = wpreu - (wpreu * wdte / 100);
		else if (wtipdte.equals("="))
			preuFinal = wdte;
		else if (wtipdte.equals("+"))
			preuFinal = wpreu + wdte;
		else if (wtipdte.equals("-"))
			preuFinal = wpreu - wdte;
		return preuFinal;
	}

	public static double getDte() {
		return dte;
	}

	public static void setDte(double dte) {
		Utilitats.dte = dte;
	}

	public static String getTipdte() {
		return tipdte;
	}

	public static void setTipdte(String tipdte) {
		Utilitats.tipdte = tipdte;
	}

	public static String getMac(Activity act) {
		WifiManager wimanager =(WifiManager) act.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		String macAddress = wimanager.getConnectionInfo().getMacAddress();
		if (macAddress == null) {
			Utilitats.Toast(act, "Dispositius no te MAC o wifi desactivada");
		}
		return macAddress;
	}

	/**
	 * Función que elimina acentos y caracteres especiales de una cadena de
	 * texto.
	 * 
	 * @param input
	 * @return cadena de texto limpia de acentos y caracteres especiales.
	 */
	public static String remove1(String input) {
		// Cadena de caracteres original a sustituir.
		String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
		// Cadena de caracteres ASCII que reemplazarán los originales.
		String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
		String output = input;
		for (int i = 0; i < original.length(); i++) {
			// Reemplazamos los caracteres especiales.
			output = output.replace(original.charAt(i), ascii.charAt(i));
		}// for i
		return output;
	}

	public static Boolean InicialitzaBaseDeDades(final Activity act) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// Yes button clicked
						// helper.close();
						if (act.deleteDatabase("andorders.db")) {
							Log.d("Avis", "deleteDatabase(): database deleted.");
							Toast.makeText(act.getApplicationContext(),
									"Base de dades eliminada",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(
									act.getApplicationContext(),
									"No ha estat possible d'esborrar la base de dades",
									Toast.LENGTH_SHORT).show();
						}
						break;

					case DialogInterface.BUTTON_NEGATIVE:
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setMessage("Vol esborrar la Base de Dades?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();
		return true;

	}

	static Boolean CopiaBBDD(Activity act) {
		Boolean Error = false;

		String Sd1 = Environment.getExternalStorageState();
		String Sd2 = Environment.MEDIA_MOUNTED;
		if(!Environment.MEDIA_MOUNTED.equals(Sd1))

		{
			Toast.makeText(act.getApplicationContext(),
					"External SD card not mounted", Toast.LENGTH_LONG)
					.show();
		}

		File sd = Utilitats.getWorkFolder(act, Utilitats.BACKUP);
		if (sd == null)
			return false;
		if (!sd.exists())
			sd.mkdirs();
		File data = Environment.getDataDirectory();

		try {
			String currentDBPath = "/data/sgb.orders/databases/andorders.db";
			String backupDBPath = "orders.db";
			File currentDB = new File(data, currentDBPath);
			File backupDB = new File(sd, backupDBPath);

			if (currentDB.exists()) {
				FileChannel src = new FileInputStream(currentDB)
						.getChannel();
				FileChannel dst = new FileOutputStream(backupDB)
						.getChannel();
				long copiats = dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
				Toast.makeText(act.getApplicationContext(),
						"COPIATS :" + copiats, Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(act.getApplicationContext(),
						"BBDD No existeix\n" + currentDB, Toast.LENGTH_LONG)
						.show();

		} catch (Exception e) {
			Error = true;
			Toast.makeText(act.getApplicationContext(),
					"BBDD NO COPIADA\n :" + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
		return Error;
	}


	static void inicialitzaBBDD(Activity act) {

		if (act.deleteDatabase("andorders.db")) {
			Log.d("Avis", "deleteDatabase(): database deleted.");
			Toast.makeText(act,
					"Base de dades eliminada",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(
					act,
					"No ha estat possible d'esborrar la base de dades",
					Toast.LENGTH_SHORT).show();
		}
	}



	public static String eliminaCometes(String input) {
		StringBuilder output = new StringBuilder();
		int pos = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) != '"')
				output.append(input.charAt(i));
		}
		return output.toString();
	}

	public static String rtrim(String s) {
		int i = s.length() - 1;
		while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
			i--;
		}
		return s.substring(0, i + 1);
	}

	public boolean isNumeric(String s) {
		return java.util.regex.Pattern.matches("\\d+", s);
	}

	static public List<Integer> getColumnTypes(OrdersHelper helper, String taula) {
		List<Integer> types
				= new ArrayList<Integer>();
		String sql = "pragma table_info(" + taula + ")";
		int posCamp = -1;
		Cursor c = helper.getReadableDatabase().rawQuery(sql, null);
		if (c.getCount() > 0) {
			while (c.moveToNext() == true) {
				int type = Utilitats.SQL_TEXT;
				if (posCamp < 0)
					posCamp = c.getColumnIndex("type");

				String s = c.getString(posCamp);
				if (s.equalsIgnoreCase("REAL"))
					type = Utilitats.SQL_REAL;
				else
					type = Utilitats.SQL_TEXT;
				types.add(type);

			}
		}
		return types;
	}

	static public Properties loadProperties(String file) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(file);
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}

	static public String getConfig(Activity act, String s) {
		return getProfile(act, null, "config.properties", s);
	}

	static public boolean ComprovaSeguretat(Activity act) {

		String Mac = getMac(act);
		Mac = "TB" + Mac.replace(":", "").toUpperCase() + ".dat";
		String st = getProfile(act, null, Mac, "Active");
		return !st.equalsIgnoreCase("N");
	}

	public static String getValue(String value) {
		if (value.equalsIgnoreCase("OrderCliRuta"))
			return " order By NOM ";
		else if (value.equalsIgnoreCase("ControlStock"))
			return "N";
		return "";
	}

	public static boolean DescarregaConfiguracio(Activity act) {
		if (isOnline(act) == false)
			return false;

		boolean ok = false;
		Ftp ftp = new Ftp(act, null);
		try {
			String dir = Utilitats.getWorkFolder(act, Utilitats.CONFIG)
					.getAbsolutePath();
			ftp.Connecta();
			ftp.DownLoadFile(true,"config", "config.properties", dir, null, true);
			ftp.DownLoadFile(true,"config", "import.properties", dir, null, true);
			ftp.Desconnecta();
			ok = true;
		} catch (IllegalStateException e) {
			ftp.setError(e.getMessage());
		} catch (IOException e) {
			ftp.setError(e.getMessage());
		} catch (FTPIllegalReplyException e) {
			ftp.setError(e.getMessage());
		} catch (FTPException e) {
			ftp.setError(e.getMessage());
		} catch (FTPDataTransferException e) {
			ftp.setError(e.getMessage());
		} catch (FTPAbortedException e) {
			ftp.setError(e.getMessage());
		} catch (FTPListParseException e) {
			ftp.setError(e.getMessage());
		} catch (Exception e) {
			ftp.setError(e.getMessage());
		}
		if (ok == false)
			Utilitats.Toast(act, ftp.getError(), true);
		return ok;
	}

	public static boolean DescarregaFitxerSeguretat(final Activity act) {

		if (isOnline(act) == false)
			return false;

		Boolean Ok = false;
		Ftp ftp = new Ftp(act, null);
		try {
			String dir = Utilitats.getWorkFolder(act, Utilitats.CONFIG)
					.getAbsolutePath();
			ftp.Connecta("ftp.reset.cat", "ftpseg", "Tablets2015_");
			String Mac = getMac(act);
			Mac = "TB" + Mac.replace(":", "").toUpperCase() + ".dat";
			if (ftp.DownLoadFile(false,"", Mac, dir, Mac, false) == 0) {
				PrintWriter writer = new PrintWriter(dir + "/" + Mac);
				writer.println("Active=P");
				
				Prefs prefs = Prefs.getInstance(act);
				String workFolder = prefs.getString("ftpFolder", "");
				String host = prefs.getString("ftpServer", "");
				String userName = prefs.getString("ftpUser", "");
				prefs.close();
				
				
				writer.println("Term=" + android.os.Build.PRODUCT + "|"
						+ android.os.Build.MANUFACTURER + "| ID:"
						+ Settings.Secure.ANDROID_ID + "|"
						+ android.os.Build.USER+ "|"
						+ host + "|"
						+ userName+ "|"
						+ workFolder);
						
				writer.close();
				ftp.Upload(dir + "/" + Mac, "");
			}
			ftp.Desconnecta();
			Ok = true;
		} catch (IllegalStateException e) {
			ftp.setError(e.getMessage());
		} catch (IOException e) {
			ftp.setError(e.getMessage());
		} catch (FTPIllegalReplyException e) {
			ftp.setError(e.getMessage());
		} catch (FTPException e) {
			ftp.setError(e.getMessage());
		} catch (FTPDataTransferException e) {
			ftp.setError(e.getMessage());
		} catch (FTPAbortedException e) {
			ftp.setError(e.getMessage());
		} catch (FTPListParseException e) {
			ftp.setError(e.getMessage());
		} catch (Exception e) {
			ftp.setError(e.getMessage());
		}
		if (Ok == false)
			Utilitats.Toast(act, ftp.getError(), true);

		return Ok;
	}

	/*
	 * public static void DescarregaImatges(Activity act) { Ftp ftp = new
	 * Ftp(act, null); File fileImg =
	 * Utilitats.getWorkFolder(actxp.this,Utilitats.IMAGES); if
	 * (fileImg.exists()) { ftp.changeDirectory(Utilitats.IMAGES); FTPFile[]
	 * listImgs = ftp.list("*.*"); progressFtp.setProgress(0); for (FTPFile
	 * listImg : listImgs) { String fl = listImg.getName(); Date date =
	 * listImg.getModifiedDate(); taula = fl; len = (int) ftp.fileSize(fl);
	 * 
	 * handler.post(new Runnable() { public void run() {
	 * progressText.setText(taula); } });
	 * 
	 * String fitxer = Utilitats .getWorkFolder(ImpExp.this, Utilitats.IMAGES)
	 * .getAbsolutePath() + "/" + fl; File fileImage = new File(fitxer);
	 * 
	 * Date p1 = new Date(fileImage.lastModified()); long l1 =
	 * fileImage.length() ; long l2 = ftp.fileSize(fl); Boolean exist =
	 * fileImage.exists(); if (!fileImage.exists() || fileImage.length() != ftp
	 * .fileSize(fl) ) ftp.download( fl, new File(fitxer), new
	 * FTPTransferListener( progressFtp, len, handler)); } }
	 * ftp.changeDirectory(".."); } }
	 */

	public static boolean Descarrega(Activity act, MapTables mapTables,
			FTPListener listener) {
		if (isOnline(act) == false)
			return false;
		boolean ok = false;
		Ftp ftp = new Ftp(act, listener);
		try {
			String dir = Utilitats.getWorkFolder(act, Utilitats.IMPORT)
					.getAbsolutePath();
			ftp.Connecta();
			ftp.DonwLoadFile(true,dir, mapTables);
			ftp.Desconnecta();
			ok = true;
		} catch (IllegalStateException e) {
			ftp.setError(e.getMessage());
		} catch (IOException e) {
			ftp.setError(e.getMessage());
		} catch (FTPIllegalReplyException e) {
			ftp.setError(e.getMessage());
		} catch (FTPException e) {
			ftp.setError(e.getMessage());
		} catch (FTPDataTransferException e) {
			ftp.setError(e.getMessage());
		} catch (FTPAbortedException e) {
			ftp.setError(e.getMessage());
		} catch (Exception e) {
			ftp.setError(e.getMessage());
		}
		if (ok == false)
			Utilitats.Toast(act, ftp.getError(), true);
		return ok;
	}

	public static boolean DescarregaImatges(Activity act,FTPListener listener) {
		if (isOnline(act) == false)
			return false;
		boolean ok = false;
		Ftp ftp = new Ftp(act,listener);
		try {
			String dir = Utilitats.getWorkFolder(act, Utilitats.IMAGES)
					.getAbsolutePath();
			ftp.Connecta();
			ftp.DownLoadFiles(dir, "pictures", "*.*", true);
			ftp.Desconnecta();
			ok = true;
		} catch (IllegalStateException e) {
			ftp.setError(e.getMessage());
		} catch (IOException e) {
			ftp.setError(e.getMessage());
		} catch (FTPIllegalReplyException e) {
			ftp.setError(e.getMessage());
		} catch (FTPException e) {
			ftp.setError(e.getMessage());
		} catch (FTPDataTransferException e) {
			ftp.setError(e.getMessage());
		} catch (FTPAbortedException e) {
			ftp.setError(e.getMessage());
		} catch (Exception e) {
			ftp.setError(e.getMessage());
		}
		if (ok == false)
			Utilitats.Toast(act, ftp.getError(), true);
		return ok;
	}
	
	static int ShowModal(Context ct,String Text)
	{
		so(ct,R.raw.capella);
		Intent it = new Intent(ct, DialogError.class);
		it.putExtra("Text", Text);
		ct.startActivity(it);
		return 0;
	}

}

