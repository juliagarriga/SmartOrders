package sgb.orders;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class Clients extends TPlantillaMant {
	private String client = "";
	String PROGRAMA = "Cap";
	private String ruta;
	Button gravar;

	@Override
	public void activate() {
	}

	public Clients(Activity act, OrdersHelper helper, String codi, String ruta) {
		super(act, helper);
		this.ruta = ruta;
		this.client = codi;
		init();
		InputMethodManager imm = (InputMethodManager) act
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getWindowToken(), 0);
	}

	public void onClick(View v) {
		super.onClick(v);
		if (v == this.ico_delete) {
		}

		if (v == gravar) {
			ContentValues cv = getCamps().UItoCv();
			String NouCodi = "##@0001";
			try {
				if (client == null || client.length() <= 0) {
					String sql = "select subjecte from Clients where  subjecte like '##@%' order by subjecte desc ";
					Cursor cur = helper.execSQL(sql);
					if (cur.getCount() > 0) {
						cur.moveToFirst();
						String camps[] = cur.getString(
								cur.getColumnIndex("subjecte")).split("@");
						if (camps.length == 2) {
							int p = Integer.parseInt(camps[1]);
							NouCodi = "##@" + Utilitats.Format("0000", p + 1);
							ContentValues cv1 = new ContentValues(); 
							cv1.clear();
							cv1.put("ruta", ruta);
							cv1.put("Subjecte", NouCodi);
							long numregs = helper.insert("CliRuta", "", cv1);
							
						}

					}
				} else
					NouCodi = client;
				cv.put("subjecte", NouCodi);
				cv.put("state", "F");						
				cv.put("tarifa", "1");				
				cv.put("tipus_factura", "W1");				
				cv.put("ruta", ruta);
				cv.remove("saldo");
				cv.remove("risc");
				String a = (String) cv.get("nom");
				String b = (String) cv.get("nomfiscal");

				long numregs = 0;
				if (client.length() <= 0)
					numregs = helper.insertOrThrow("Clients", "", cv);
				else
					numregs = helper.update("Clients", "subjecte", cv);
				
				long s = numregs;
				act.finish();
				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				ClientsPerRutaIntent.putExtra("parametre1", NouCodi);
				ClientsPerRutaIntent.putExtra("programa", "LlistaDocuments");
				getAct().startActivity(ClientsPerRutaIntent);


			} catch (SQLiteException e) {
				Errors.appendLog(act, Errors.ERROR, "Cap",
						"Error Gravaci� registre", e, cv, true);

			} finally {
//				Utilitats.Toast(act, " Client " + NouCodi + " creat");
			}
			
		}
	}

	@Override
	void postRead() {
	}

	Boolean read() {
		cursor = helper
				.getWritableDatabase()
				.rawQuery(
						"select P.*,G.risc,G.saldo from Clients P LEFT OUTER JOIN GrupCli G ON G.grupcli = P.grup where subjecte=? ",
						new String[] { client });

		return cursor.getCount() > 0;
	}

	@Override
	public void destroy() {

	}

	@Override
	void build() throws Exception {

		getCamps().setTable("Cap");
		getCamps().setKey("_id");
		getCamps().setSqlList(
				"select * from Clients where subjecte='" + client + "'");
		getCamps().getCamps().add(
				new TFormField("nom", view.findViewById(R.id.nom)));
		getCamps().getCamps().add(
				new TFormField("contacte", view.findViewById(R.id.contacte)));
		getCamps().getCamps().add(
				new TFormField("nomfiscal", view.findViewById(R.id.nomfiscal)));
		getCamps().getCamps().add(
				new TFormField("nif", view.findViewById(R.id.nif)));

		getCamps().getCamps().add(
				new TFormField("mail", view.findViewById(R.id.mail)));
		getCamps().getCamps().add(
				new TFormField("telf1", view.findViewById(R.id.telefon1)));

		gravar = (Button) findViewById(R.id.linies_gravar);
		gravar.setOnClickListener(this);

		OnClickListener clk = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Utilitats.callPhone(act, ((EditText) v).getText().toString());
			}

		};

		((EditText) findViewById(R.id.telefon1)).setOnClickListener(clk);
		((EditText) findViewById(R.id.telefon2)).setOnClickListener(clk);
		((EditText) findViewById(R.id.telefon3)).setOnClickListener(clk);
		((EditText) findViewById(R.id.mobil)).setOnClickListener(clk);

		getCamps().getCamps().add(
				new TFormField("telf2", view.findViewById(R.id.telefon2)));
		getCamps().getCamps().add(
				new TFormField("telf3", view.findViewById(R.id.telefon3)));
		getCamps().getCamps().add(
				new TFormField("mobil", view.findViewById(R.id.mobil)));
		getCamps().getCamps().add(
				new TFormField("adreca", view.findViewById(R.id.adreca)));
		getCamps().getCamps().add(
				new TFormField("poblacio", view.findViewById(R.id.poblacio)));
		getCamps().getCamps().add(
				new TFormField("provincia", view.findViewById(R.id.provincia)));
		getCamps().getCamps().add(
				new TFormField("banc", view.findViewById(R.id.banc)));
		getCamps().getCamps().add(
				new TFormField("agencia", view.findViewById(R.id.agencia)));
		getCamps().getCamps().add(
				new TFormField("compte", view.findViewById(R.id.compte)));
		getCamps().getCamps().add(
				new TFormField("observacions", view.findViewById(R.id.observacions)));
		getCamps().getCamps()
				.add(new TFormField("codipostal", view
						.findViewById(R.id.codipostal)));
		getCamps().getCamps().add(
				new TFormField("observacions", view
						.findViewById(R.id.observacions)));
		getCamps().ClearUI();
		getCamps().getCamps().add(
				new TFormField("risc", view.findViewById(R.id.risc)));
		getCamps().getCamps().add(
				new TFormField("saldo", view.findViewById(R.id.saldo)));
	}

	@Override
	int getMantViewId() {
		return R.layout.clients;
	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP | ICO_SAVE | ICO_DELETE;
	}

	@Override
	long getRowsLayout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void haCanviat(Boolean b) {
		// TODO Auto-generated method stub

	}

}
