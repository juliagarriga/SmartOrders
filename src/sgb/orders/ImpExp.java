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

import java.io.File;
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

public class ImpExp extends Activity {
	
	MapTables mapTables = new MapTables(); 
	
	
	
	String perSD = null;
	String PROGRAMA = "ImpExp";

	final private String fitxers[] = { "precios.txt", "Contadores.txt",
			"DocPend.txt", "PromoEspe.txt", /*
											 * "Filtro.txt" ,
											 */
			"rutas.txt", "Clientes.txt", "Articulos.txt", "CliRuta.txt",
			"Familias.txt", "PrePedido.txt","Linies.txt" };
	final private String taules[] = { "Tarifes", "Comptadors", "Efectes",
			"PreusEsp",
			/* "Filtres", */
			"rutes", "clients", "articles", "CliRuta", "Families", "Precomanda","Linies"  };
	// final private String fitxers [] = { "rutas.txt" };
	// final private String taules [] = { "rutes" };

	private ProgressBar progressFtp = null;
	private ProgressBar progressImp = null;
	private OrdersHelper helper = null;
	private TextView progressText;
	private MediaPlayer mp;
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
		Rebre(perSD);

	}

	public void Importar() {

	}

	public void Rebre(final String perSD) {
		
		String File = Utilitats.getWorkFolder(this,Utilitats.WORK)+"/"+"config.properties";
		mapTables.Load(File);
		
		helper = new OrdersHelper(this);
		Thread proce2 = new Thread(new Runnable() {

			public void run() {

				try {
					if (perSD == null) {
						Prefs prefs = Prefs.getInstance(ImpExp.this);

						String host = prefs.getString("ftpServer", "");
						String userName = prefs.getString("ftpUser", "");
						String password = prefs.getString("ftpPwd", "");
						prefs.close();

						if (host.length() <= 0) {
							host = "ftp.reset.cat";
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
					 * progressBar.setMessage("File downloading ...");
					 * progressBar
					 * .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					 * 
					 * progressBar.show();
					 */

					progressFtp.setProgress(0);
					progressFtp.setMax((int) len);
					progressImp.setProgress(0);

					Thread proce = new Thread(new Runnable() {

						public void run() {
							try {
								if (perSD == null) {
									File fileImg = Utilitats.getWorkFolder(ImpExp.this,Utilitats.IMAGES);
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

											String fitxer = Utilitats
													.getWorkFolder(ImpExp.this,
															Utilitats.IMAGES)
													.getAbsolutePath()
													+ "/" + fl;
											File fileImage = new File(fitxer);
											
											Date p1 = new Date(fileImage.lastModified());
											long l1 = fileImage.length() ;
											long l2 = ftp.fileSize(fl);
											Boolean exist  =  fileImage.exists();
											if (!fileImage.exists()
													|| fileImage.length() != ftp
															.fileSize(fl) )
												ftp.download(
														fl,
														new File(fitxer),
														new FTPTransferListener(
																progressFtp,
																len, handler));
										}
									}
									ftp.changeDirectory("..");
								}
								for (int i = 0; i < fitxers.length; i++) {
									file = fitxers[i];
									taula = mapTables.getTable(taula,taules[i]);
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
										File ffitxer = Utilitats.getWorkFolder(
												ImpExp.this, Utilitats.IMPORT);
										if (ffitxer == null) {
											Toast.makeText(
													getApplicationContext(),
													" No es pot crear fitxer d'importació. Revisi accés a targeta SD",
													Toast.LENGTH_LONG);
											return;
										}

										String fitxer = Utilitats
												.getWorkFolder(ImpExp.this,
														Utilitats.IMPORT)
												.getAbsolutePath()
												+ "/" + file;
										if (perSD == null)
											ftp.download(file,
													new File(fitxer),
													new FTPTransferListener(
															progressFtp, len,
															handler));
										else {
											File sd = new File(fitxer);
											if (sd.exists() == false) {
												Toast.makeText(
														getApplicationContext(),
														" No es troba el fitxer "
																+ fitxer
																+ " a la targeta SD",
														Toast.LENGTH_LONG);
												return;
											}

										}

/*										Csv2Sqlite sq = new Csv2Sqlite(mapTables);
										progressImp.setMax(sq.ImportCount(file,
												ImpExp.this));
										sq.ImportFile(file, taules[i], helper,
												ImpExp.this, new NotifCsv()); */
									}
								}
							} catch (FTPListParseException e) {
								Errors.appendLog(ImpExp.this, Errors.ERROR,
										"Cap", "FTP Parse", e, null, true);
							} catch (FTPAbortedException e) {
								Errors.appendLog(ImpExp.this, Errors.ERROR,
										"Cap", "FTP Abort", e, null, true);
							} catch (FTPDataTransferException e) {
								Errors.appendLog(ImpExp.this, Errors.ERROR,
										"Cap", "FTP Transfer Exception", e,
										null, true);
							} catch (FTPException e) {
								Errors.appendLog(ImpExp.this, Errors.ERROR,
										"Cap", "FTP Exception", e, null, true);

							}

							catch (FTPIllegalReplyException ex) {
								Toast.makeText(
										getApplicationContext(),
										ex.getMessage()
												+ " Error de Connexió. Revisi configuració ftp :"
												+ ex.getCause(),
										Toast.LENGTH_LONG).show();
							} catch (IOException e) {
								Errors.appendLog(ImpExp.this, Errors.ERROR,
										"Cap", "FTP IO Exception", e, null,
										true);
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
									mp = MediaPlayer.create(
											getApplicationContext(),
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

						private void closeWindow() {
							// TODO Auto-generated method stub

						}
					});
					proce.start();

				} catch (FTPException ex) {
					Errors.appendLog(ImpExp.this, Errors.ERROR, "Cap",
							"FTP Exception", ex, null, true);
				} catch (FTPIllegalReplyException ex) {
					Errors.appendLog(ImpExp.this, Errors.ERROR, "Cap",
							"FTP Illegal reply Exception", ex, null, true);
				} catch (IOException ex) {
					Errors.appendLog(ImpExp.this, Errors.ERROR, "Cap",
							"Error Connexió", ex, null, true);
				}
				int i = 0;
				i++;
			}
		});

		proce2.start();
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
