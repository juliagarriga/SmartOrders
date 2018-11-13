package sgb.orders;

import android.app.Activity;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SendData {
		private Socket socket;
		Activity act;

		private static final int SERVERPORT = 27016;
//		private static final String SERVER_IP = "192.168.1.44";
		private static final String SERVER_IP = "192.168.2.34";


		public void send(Activity act,String data) {
			this.act = act;
		
		    new Thread(new ClientThread()).start();
			try {
				SystemClock.sleep(100);
				String str = data;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		class ClientThread implements Runnable {

			@Override
			public void run() {

				try {
					String networkSSID = "SGB-PRIMARY";
					String networkPass = "a1020203030304040404050505";

/*					WifiConfiguration conf = new WifiConfiguration();
					WifiManager wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
					List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
					for( WifiConfiguration i : list ) {
					    if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
					         wifiManager.disconnect();
					         wifiManager.enableNetwork(i.networkId, true);
					         
					         wifiManager.reconnect();    
					         }
					    }
					
	*/				
					
					InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

					socket = new Socket(serverAddr, SERVERPORT);
					PrintWriter out = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
					out.println("<SAMPLE ERROR SOCKET>");
			        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			        try {
			            String message = "";
			            int charsRead = 0;
			            char[] buffer = new char[1000];

			            while ((charsRead = in.read(buffer)) != -1) {
			                message += new String(buffer).substring(0, charsRead);
			                if (message.equals("Ok")) break;
			            }
						for (int i=0; i< 1000; i++)
							out.println("**"+i+" SENDING DATA A TOPE\n");
							
						out.println("<END>");

			                if (socket != null) {
			                    if (socket.isConnected()) {
			                        try {
			                            in.close();
			                            out.close();
			                            socket.close();
			                        } catch (IOException e) {
			                            e.printStackTrace();
			                        }
			                    }
			                }
			        } catch (IOException e) {
//			            n "Error receiving response:  " + e.getMessage();
			        }			        
			        
			        

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					
					act.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(act,
									"No es pot connectar Socket ",
									Toast.LENGTH_LONG).show();
						}
					});

					
				}

			}

		}
	}
	


