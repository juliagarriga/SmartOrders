package sgb.orders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Errors {
	public static final String ERROR = "E";
	public static final String AVIS = "W";
	public static final String MSG = "M";

	static public void appendLog(Activity ctx, String severe, String program,
			String text) {
		appendLog(ctx, severe, program, text, null, null, false);

	}

	static public void appendLog(Activity ctx, String severe, String program,
			String text, Exception e) {
		appendLog(ctx, severe, program, text, e, null, false);
	}

	static public void appendLog(Activity ctx, String severe, String program,
			String text, Boolean msg) {
		appendLog(ctx, severe, program, text, null, null, null);
	}

	static public void appendLog(Activity ctx, String severe, String program,
			String text, Exception e, ContentValues cv) {
		appendLog(ctx, severe, program, text, e, cv, false);
	}

	static public void appendLog(Activity act, String severe, String program,
			String text, Throwable e, ContentValues cv, Boolean show) {
		String wFile = "log.txt";
		File fFolder = Utilitats.getWorkFolder(act,Utilitats.LOGS);
		if (fFolder==null)  {
			Toast.makeText(act, "Comprovi Targeta SD. No es pot gravar el fitxer de Log per error : "+text, Toast.LENGTH_LONG)
			.show();
			return;
			
		}
		String wFolder = fFolder.getAbsolutePath();

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"dd/MM/yyyy dd:HH:mm:ss");
		String txt = "   " + program + "-" + severe + "-"
				+ formatter.format(date) + " : " + text;
		StringBuffer bf = new StringBuffer();
		PrintStream printFile;

		try {

			bf.append("\n--------------------------------------------------------\n");
			bf.append(txt + "\n");

			if (e != null)
				bf.append(e.toString() + "\n");
			if (cv != null)
				bf.append(cv.toString() + "\n");
			
			printFile = new PrintStream(new BufferedOutputStream(
					new FileOutputStream(wFolder + "/" + wFile, true)));
			printFile.append(bf);
			printFile.close();
			/* No hi ha una manera més fàcil de fer-ho ??? */
			final Activity act2= act;
			final String  txts = wFile;
			final String txt2 = txt;
			final Throwable e2 = e;
			
			
			act.runOnUiThread(new Runnable() {
				  public void run() {
						AlertDialog alertDialog = new AlertDialog.Builder(act2).create();
						if (e2 != null)
							alertDialog.setTitle(e2.getMessage());
						alertDialog.setMessage(txt2);
						alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Mail.send(act2,txts);
							}
						});
						alertDialog.setIcon(R.drawable.icon);
						alertDialog.show();			
				  
				  
				  }
				});
			
			
		} catch (FileNotFoundException e1) {
			Toast.makeText(act, text + " " + e1.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}
}
