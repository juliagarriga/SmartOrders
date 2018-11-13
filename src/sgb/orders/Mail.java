package sgb.orders;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Mail {
	public static void send(Context ctx,String logFile) {

		String wFolder = Utilitats.comprovaFolder(Utilitats.LOGS).getAbsolutePath();
				
		
		/* Primer el renombrem */
		
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		File from = new File(wFolder,logFile);
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyddHHmmss");
		logFile = "log_"+formatter.format(date)+".log";
		File to = new File(wFolder,logFile);
		from.renameTo(to);  
		
		
		
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { "salvador@reset.cat" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Error Check");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				"Sended by  "+android.os.Build.PRODUCT+"\n"+
						  android.os.Build.MANUFACTURER+"\n ID:"+Settings.Secure.ANDROID_ID+"\n"+
						  android.os.Build.USER+"\n"		);

		String file = "file://"+wFolder+"/"+logFile;
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(file));
		try {
		ctx.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(ctx, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}

		
	}
	
}
