	package sgb.orders;

    import android.app.Activity;

    import java.io.File;
    import java.io.FileNotFoundException;
    import java.io.IOException;
    import java.util.Date;

    import it.sauronsoftware.ftp4j.FTPAbortedException;
    import it.sauronsoftware.ftp4j.FTPClient;
    import it.sauronsoftware.ftp4j.FTPDataTransferException;
    import it.sauronsoftware.ftp4j.FTPDataTransferListener;
    import it.sauronsoftware.ftp4j.FTPException;
    import it.sauronsoftware.ftp4j.FTPFile;
    import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
    import it.sauronsoftware.ftp4j.FTPListParseException;




public class Ftp {
	FTPClient ftp = new FTPClient();
	Activity act;
	FTPListener listener;
	String ftpError;
	String workFolder;

	String getError() {
		return ftpError;
	}

	void setError(String err) {
		ftpError = err;
	}



	public Ftp(Activity act, FTPListener listener) {
		this.listener = listener;
		this.act = act;
	}

	int Desconnecta() throws IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException {
		ftp.disconnect(false);
		return 0;
	}

	int Connecta() throws IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException {
		Prefs prefs = Prefs.getInstance(act);
		workFolder = prefs.getString("ftpFolder", "");
		String host = prefs.getString("ftpServer", "");
		String userName = prefs.getString("ftpUser", "");
		String password = prefs.getString("ftpPwd", "");
		prefs.close();

		ftp.setPassive(true);
		ftp.connect(host);
		ftp.login(userName, password);
		ftp.setType(FTPClient.TYPE_BINARY);
		return 1;
	}

	int Connecta(String ftpServer, String ftpUser, String ftpPwd)
			throws IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException {
		String host = ftpServer;
		String userName = ftpUser;
		String password = ftpPwd;

		ftp.setPassive(true);
		ftp.connect(ftpServer);
		ftp.login(userName, password);
		ftp.setType(FTPClient.TYPE_BINARY);
		return 1;
	}

	Boolean CreateRemoteDir(String dirRemot) throws IllegalStateException,
			IOException, FTPIllegalReplyException, FTPException {
		ftp.createDirectory(dirRemot);
		return true;
	}

	long FileSize(String fl) throws IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException {
		return ftp.fileSize(fl);
	}

	void ChangeDirectory(String dirRemot) throws IllegalStateException,
			IOException, FTPIllegalReplyException, FTPException {
		if (!ExistsFolder(dirRemot))
			CreateRemoteDir(dirRemot);
		// ftp.changeDirectory(dirRemot); // Exist Canvia automativament
	}

	boolean ExistsFolder(String folder) {
		return ExistsFolder(folder, true);
	}

	boolean ExistsFolder(String folder, Boolean canvia) {
		boolean exist = false;
		try {
			ftp.changeDirectory(folder);
			if (!canvia)
				ftp.changeDirectoryUp();
			exist = true;
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		} catch (FTPIllegalReplyException e) {
		} catch (FTPException e) {
		}
		return exist;
	}

	int ExistsFile(String filRemot) throws IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException, FTPDataTransferException,
			FTPAbortedException, FTPListParseException {
		FTPFile[] listImgs = ftp.list(filRemot);
		int i = listImgs.length;
		return i;
	}

	Boolean Upload(String file, String dirRemote) throws IllegalStateException,
			FileNotFoundException, IOException, FTPIllegalReplyException,
			FTPException, FTPDataTransferException, FTPAbortedException {
		File fp = new java.io.File(file);
		if (fp.length() > 0)
			ftp.upload(fp, listener);
		return null;

	}

	int DonwLoadFile(Boolean inWorkFolder,String dir, MapTables mapTables)
			throws IllegalStateException, FileNotFoundException, IOException,
			FTPIllegalReplyException, FTPException, FTPDataTransferException,
			FTPAbortedException {
		int downloaded = 0;
		String dirRemot="/";
		if (inWorkFolder && !workFolder.isEmpty()) {
			dirRemot= "/"+workFolder+dirRemot;
		}
		ChangeDirectory(dirRemot);
		
		for (Taules tb : mapTables.getTaules()) {
			final String fitxerFtp = tb.getKey();
			String fileLocal = dir + "/" + fitxerFtp;
			int len = 0;
			try {
			len = (int) ftp.fileSize(fitxerFtp);
			if (listener != null)
				listener.init(fitxerFtp,len);
			
			ftp.download(fitxerFtp, new File(fileLocal), listener);
			} catch (FTPException e)
			{
				Utilitats.Toast(act,"No es troba el fitxer : "+fitxerFtp,true);
			}

			downloaded++;

		}
		ChangeDirectory("/");
		return downloaded;

	}

	int DownLoadFile(Boolean inWorkFolder,String dirRemot, String filRemot, String dirLocal,
			String filLocal, Boolean Throw) throws Exception {

		if (inWorkFolder && !dirRemot.isEmpty()) {
			dirRemot= "/"+workFolder+"/"+dirRemot;
			ChangeDirectory(dirRemot);
		}
		else
			ftp.changeDirectory("/");
		if (filLocal == null)
			filLocal = filRemot;
		if (dirLocal == null)
			dirLocal = ".";

		int downloaded = 0;
		String fileRemot = filRemot;
		String fileLocal = dirLocal + "/" + filLocal;
		if (ExistsFolder(dirRemot) == false) {
			CreateRemoteDir(dirRemot);
			if (ExistsFolder(dirRemot) == false) {
				if (!Throw)
					return 0;
				else
					throw new Exception("No es pot crear folder " + dirRemot);
			}

			else {
				if (!Throw)
					return 0;
				else
					throw new Exception(
							"No existeix el fitxer de configuració  "
									+ filRemot);
			}
		}
		FTPFile[] listImgs = ftp.list(filRemot);
		if (listImgs.length <= 0)
			if (!Throw)
				return 0;
			else
				throw new Exception("No existeix el fitxer de configuració  "
						+ filRemot);
		for (FTPFile listImg : listImgs) {
			String fl = listImg.getName();
			/*
			 * Date date = listImg.getModifiedDate(); long len =
			 * ftp.fileSize(fl);
			 */
			ftp.download(fl, new File(fileLocal), listener);
			downloaded++;

		}

		ftp.changeDirectory("/");
		return downloaded;

	}

	public void DownLoadFiles(String DirLocal, String DirRemot, String fitxers,
			Boolean comparar) throws IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException, FTPDataTransferException,
			FTPAbortedException, FTPListParseException {
		ChangeDirectory(DirRemot);
		String dir = ftp.currentDirectory();
		FTPFile[] listImgs = ftp.list("*.*");
		for (FTPFile listImg : listImgs) {
			String fl = listImg.getName();
			Date date = listImg.getModifiedDate();
			long len = (int) ftp.fileSize(fl);
			String fitxer = DirLocal + "/" + fl;
			File fileImage = new File(fitxer);
			Date p1 = new Date(fileImage.lastModified());
			long l1 = fileImage.length();
			long l2 = ftp.fileSize(fl);
			Boolean exist = fileImage.exists();
			if (listener != null) 
				listener.init(fl,l2);
			
			if (comparar == false ||  !fileImage.exists() || fileImage.length() != ftp.fileSize(fl)) {
				File miniatura = new File (DirLocal + "/_" + fl);
				if (comparar = true  && miniatura.exists())
					miniatura.delete();

				ftp.download(fl, new File(fitxer), listener);
			}

		}
		ftp.changeDirectoryUp();

	}
	/*
	 * 
	 * // progressFtp.setProgress(0);
	 * 
	 * handler.post(new Runnable() { public void run() {
	 * progressText.setText(taula); } });
	 * 
	 * 
	 * 
	 * } }
	 * 
	 * }
	 */

}

interface FTPListener extends FTPDataTransferListener {
	public void init(String fileName,long fileLength);
		
}