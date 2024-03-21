package sgb.orders;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

public class ImpExp2014 extends Activity {
	String PROGRAMA = "ImpExp";
	MapTables mapTables = new MapTables(); 


	final private String fitxers[] = { "precios.txt", "Contadores.txt",
			"DocPend.txt", "PromoEspe.txt", /*
											 * "Filtro.txt" ,
											 */
			"rutas.txt", "Clientes.txt", "Articulos.txt", "CliRuta.txt",
			"Familias.txt", "PrePedido.txt", "Linies.txt" };
	final private String taules[] = { "Tarifes", "Comptadors", "Efectes",
			"PreusEsp",
			/* "Filtres", */
			"rutes", "clients", "articles", "CliRuta", "Families",
			"Precomanda", "Linies" };
	// final private String fitxers [] = { "rutas.txt" };
	// final private String taules [] = { "rutes" };

	private ProgressBar progressFtp = null;
	private ProgressBar progressImp = null;
	private OrdersHelper helper = null;
	private TextView progressText;
	private MediaPlayer mp;
	private List<String> arrayList;

	int len = 0;
	String perSD;
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

	
	
	public Boolean EsborrarFitxers() { 
	File folder = new File(Utilitats.getWorkFolder(ImpExp2014.this,
			Utilitats.WORK).getAbsolutePath());
	File[] listOfFiles = folder.listFiles();
	if (listOfFiles.length > 0)

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if (listOfFiles[i].delete() == false)
					return false;
			}
		}
	return true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.impexp);

		progressFtp = (ProgressBar) findViewById(R.id.progress_ftp);
		progressImp = (ProgressBar) findViewById(R.id.progress_imp);
		progressText = (TextView) findViewById(R.id.progress_text);

		Bundle extras = getIntent().getExtras();
		if (extras != null)
			perSD = extras.getString("PerSD");
		try {
			Rebre();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPIllegalReplyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPDataTransferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPAbortedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FTPListParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Boolean BaixarFitxersUnificats() throws IllegalStateException,
			FileNotFoundException, IOException, FTPIllegalReplyException,
			FTPException, FTPDataTransferException, FTPAbortedException,
			FTPListParseException {
		if (perSD == null) {
			Prefs prefs = Prefs.getInstance(ImpExp2014.this);
			String host = prefs.getString("ftpServer", "");
			String userName = prefs.getString("ftpUser", "");
			String password = prefs.getString("ftpPwd", "");
			prefs.close();

			if (host.length() <= 0) {
				host = "ftp.reset.es";
				userName = "dem01";
				password = "dem01pda";
			}

			if (host == null || host.compareTo(" ") < 0) {

				runOnUiThread(new Runnable() {
					public void run() {

						Toast.makeText(
								getApplicationContext(),
								"Cal informar dades de connexió a Preferències",
								Toast.LENGTH_LONG).show();
					}
				});
			} else {
				ftp = new FTPClient();
				ftp.setPassive(true);
				ftp.connect(host);
				ftp.login(userName, password);
				ftp.setType(FTPClient.TYPE_BINARY);

				FTPFile[] lists = ftp.list("*.EXP");
				progressFtp.setProgress(0);
				for (FTPFile list : lists) {
					String fl = list.getName();
					len = (int) ftp.fileSize(fl);

					handler.post(new Runnable() {
						public void run() {
							progressText.setText(taula);
						}
					});

					String fitxer = Utilitats.getWorkFolder(ImpExp2014.this,
							Utilitats.IMPORT).getAbsolutePath()
							+ "/" + fl;
					ftp.download(fl, new File(fitxer), new FTPTransferListener(
							progressFtp, len, handler));
					ftp.deleteFile(fl);
					return true;
				}
			}
		}
		return false;

	}

	
	
	
	


	

	public void Rebre() throws IllegalStateException, FileNotFoundException,
			IOException, FTPIllegalReplyException, FTPException,
			FTPDataTransferException, FTPAbortedException,
			FTPListParseException {

		Thread proce2 = new Thread(new Runnable() {

			public void run() {
				try {
					BaixarFitxersUnificats();

					File folder = new File(Utilitats.getWorkFolder(ImpExp2014.this,
							Utilitats.IMPORT).getAbsolutePath());
					File[] listOfFiles = folder.listFiles();
					if (listOfFiles.length > 0)

						for (int i = 0; i < listOfFiles.length; i++) {
							if (listOfFiles[i].isFile()) {
								String nm = listOfFiles[i].getName()
										.toUpperCase();
								if (nm.endsWith(".EXP")) {
									EsborrarFitxers();
									SeparaFitxer(nm);
									String OutFile = Utilitats.getWorkFolder(ImpExp2014.this,
											Utilitats.IMPORTED).getAbsolutePath()+"/"+nm;
									listOfFiles[i].renameTo(new File(OutFile));
									
									Rebre("S");
								}
							}
						}
					else
						Rebre(perSD);

				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FTPIllegalReplyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FTPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FTPDataTransferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FTPAbortedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FTPListParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		proce2.start();

	}

	/*
	 * A partir d'un fitxer unificat genera diversos fitxer per fer-ho
	 * compatible amb versiona anteriors
	 */

	public void SeparaFitxer(String fitxer) throws IOException {
		String NomFile = "";
		String Directori = Utilitats.getWorkFolder(ImpExp2014.this,
				Utilitats.IMPORT).getAbsolutePath();
		String DirectoriWork = Utilitats.getWorkFolder(ImpExp2014.this,
				Utilitats.WORK).getAbsolutePath();

		File file = new File(Directori + "/" + fitxer);
		BufferedWriter out = null;
		BufferedReader in = new BufferedReader(new FileReader(file));

		while (in.ready()) {
			String s = in.readLine();
			if (s.trim().length() <= 0)
				continue;
			String l = s.substring(0, 2);
			if (l.equals("<#")) {
				int pos = s.indexOf("#>");
				if (pos == -1) // Error de format
					;
				if (s.substring(0, 5).equals("<#END")) { // Fi de seqüencia
					out.close();
					NomFile = "";

				} else {
					NomFile = s.substring(2, pos);
					out = new BufferedWriter(new FileWriter(new File(
							DirectoriWork + "/" + NomFile + ".txt"), true));
				}
			} else {
				if (out == null)
					throw new IOException("Error de format ");
				out.write(s);
				out.newLine();
			}

		}

		in.close();

	}

	public void Rebre(final String perSD) {
		helper = new OrdersHelper(this);

		try {
			if (perSD == null) {
				Prefs prefs = Prefs.getInstance(ImpExp2014.this);

				String host = prefs.getString("ftpServer", "");
				String userName = prefs.getString("ftpUser", "");
				String password = prefs.getString("ftpPwd", "");
				prefs.close();

				if (host.length() <= 0) {
					host = "ftp.reset.es";
					userName = "dem01";
					password = "dem01pda";
				}

				if (host == null || host.compareTo(" ") < 0) {

					runOnUiThread(new Runnable() {
						public void run() {

							Toast.makeText(
									getApplicationContext(),
									"Cal informar dades de connexió a Preferències",
									Toast.LENGTH_LONG).show();
						}
					});
					return;
				}

				ftp = new FTPClient();
				ftp.setPassive(true);
				ftp.connect(host);
				ftp.login(userName, password);
				ftp.setType(FTPClient.TYPE_BINARY);
			}
			/*
			 * 
			 * 
			 * 
			 * progressBar = new ProgressDialog(ctx);
			 * progressBar.setCancelable(true);
			 * progressBar.setMessage("File downloading ..."); progressBar
			 * .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			 * 
			 * progressBar.show();
			 */

			progressFtp.setProgress(0);
			progressFtp.setMax((int) len);
			progressImp.setProgress(0);

			{
				try {
					if (perSD == null) {
						File fileImg = Utilitats.getWorkFolder(ImpExp2014.this,
								Utilitats.IMAGES);
						if (fileImg.exists()) {
							ftp.changeDirectory(Utilitats.IMAGES);
							FTPFile[] listImgs = ftp.list("*.*");
							progressFtp.setProgress(0);
							for (FTPFile listImg : listImgs) {
								String fl = listImg.getName();
								Date date = listImg.getModifiedDate();
								taula = fl;
								len = (int) ftp.fileSize(fl);

								handler.post(new Runnable() {
									public void run() {
										progressText.setText(taula);
									}
								});

								String fitxer = Utilitats.getWorkFolder(
										ImpExp2014.this, Utilitats.IMAGES)
										.getAbsolutePath()
										+ "/" + fl;
								File fileImage = new File(fitxer);

								Date p1 = new Date(fileImage.lastModified());
								long l1 = fileImage.length();
								long l2 = ftp.fileSize(fl);
								Boolean exist = fileImage.exists();
								if (!fileImage.exists()
										|| fileImage.length() != ftp
												.fileSize(fl))
									ftp.download(fl, new File(fitxer),
											new FTPTransferListener(
													progressFtp, len, handler));
							}
						}
						ftp.changeDirectory("..");
					}
					for (int i = 0; i < fitxers.length; i++) {
						file = fitxers[i];
						taula = taules[i];
						FTPFile[] list = null;
						if (perSD == null)
							list = ftp.list(file);
						progressFtp.setProgress(0);
						progressImp.setProgress(0);

						handler.post(new Runnable() {
							public void run() {
								progressText.setText(taula);
							}
						});

						if (perSD != null || list.length != 0) {
							if (perSD == null) {
								len = (int) ftp.fileSize(file);
								progressFtp.setMax(len);
							}
							File ffitxer = Utilitats.getWorkFolder(ImpExp2014.this,
									Utilitats.WORK);
							if (ffitxer == null) {
								Toast.makeText(
										getApplicationContext(),
										" No es pot crear fitxer d'importació. Revisi accés a targeta SD",
										Toast.LENGTH_LONG);
								return;
							}

							String fitxer = Utilitats.getWorkFolder(
									ImpExp2014.this, Utilitats.WORK)
									.getAbsolutePath()
									+ "/" + file;
							if (perSD == null)
								ftp.download(file, new File(fitxer),
										new FTPTransferListener(progressFtp,
												len, handler));
							else {
								File sd = new File(fitxer);
								if (sd.exists() == false) {
									Toast.makeText(getApplicationContext(),
											" No es troba el fitxer " + fitxer
													+ " a la targeta SD",
											Toast.LENGTH_LONG);
									return;
								}

							}

/*							Csv2Sqlite sq = new Csv2Sqlite(mapTables);
							progressImp.setMax(sq
									.ImportCount(file, ImpExp2014.this));
							sq.ImportFile(file, taules[i], helper, ImpExp2014.this,
									new NotifCsv()); */
						}
					}
				} catch (FTPListParseException e) {
					Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap",
							"FTP Parse", e, null, true);
				} catch (FTPAbortedException e) {
					Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap",
							"FTP Abort", e, null, true);
				} catch (FTPDataTransferException e) {
					Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap",
							"FTP Transfer Exception", e, null, true);
				} catch (FTPException e) {
					Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap",
							"FTP Exception", e, null, true);

				}

				catch (FTPIllegalReplyException ex) {
					Toast.makeText(
							getApplicationContext(),
							ex.getMessage()
									+ " Error de Connexió. Revisi configuració ftp :"
									+ ex.getCause(), Toast.LENGTH_LONG).show();
				} catch (IOException e) {
					Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap",
							"FTP IO Exception", e, null, true);
				}

				// sleep 2 seconds, so that you can see the 100%
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				// close the progress bar dialog
				runOnUiThread(new Runnable() {
					public void run() {
						mp = MediaPlayer.create(getApplicationContext(),
								R.raw.fi_transmissio);
						mp.start();
						progressText.setText("Procés Finalitzat");
						Toast.makeText(getApplicationContext(),
								"Procés d'importació finalitzat",
								Toast.LENGTH_LONG).show();
					}
				});

				helper.close();
			}

		} catch (FTPException ex) {
			Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap", "FTP Exception",
					ex, null, true);
		} catch (FTPIllegalReplyException ex) {
			Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap",
					"FTP Illegal reply Exception", ex, null, true);
		} catch (IOException ex) {
			Errors.appendLog(ImpExp2014.this, Errors.ERROR, "Cap",
					"Error Connexió", ex, null, true);
		}
		int i = 0;
		i++;

	}

	public void runProces(View arg0) {

		Rebre("tarifes.csv");

	}

	class FTPTransferListener implements FTPDataTransferListener {

		private int bytesTransferred = 0;
		Context cv;
		final ProgressBar progressBar;
		long lengthFile;
		int bytesReaded = 0;
		int calcul = 0;

		FTPTransferListener(ProgressBar progressBar, long lengthFile,
				Handler progressBarHandler) {
			this.progressBar = progressBar;
			this.lengthFile = lengthFile;

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
