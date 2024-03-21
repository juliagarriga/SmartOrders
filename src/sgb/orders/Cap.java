package sgb.orders;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Cap extends TPlantillaMant {
	private String client = "";
	private long document = 0;
	SGEdit data;
	SGEdit entrega;
	TextView total;
	Comptadors ct;
	String PROGRAMA = "Cap";
	DatePickerDialog dpk;
	TFormField fDocument;
	Boolean swAlta;
	double saldo=0;
	double risc=0;
	double valor_total = 0;


	@Override
	public void activate() {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		total.setText(Utilitats.calculaTotals(helper, document));

		/*
		 * AlertDialog.Builder builder = new AlertDialog.Builder(act);
		 * builder.setMessage("Activant!!") .setCancelable(false)
		 * .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		 * public void onClick(DialogInterface dialog, int id) { DecimalFormat
		 * twoDForm = new DecimalFormat("#.##");
		 * total.setText(Utilitats.calculaTotals(helper, document));
		 * 
		 * } }); AlertDialog alert = builder.create(); alert.show();
		 */

	}

	public Cap(Activity act, OrdersHelper helper, String codi, String document) {
		super(act, helper);
		this.document = Long.parseLong(document);
		this.client = codi;
		ct = new Comptadors(helper);
		swAlta = false;
		init();
	}

	void postNotRead() {
		postRead();
	}

	void postRead() {
		if (client == null)
			client = cursor.getString(cursor.getColumnIndex("client"));
		Cursor c = Utilitats.readSubjecte(helper, client);
		TextView desc = (TextView) view.findViewById(R.id.client_desc);
		if (c != null) {
			String grup = c.getString(c.getColumnIndex("grup"));
			Cursor cGrup = Utilitats.readGrupCli(helper, grup);
			if (cGrup != null) {
				risc = cGrup.getDouble(cGrup.getColumnIndex("risc")); 
				saldo = cGrup.getDouble(cGrup.getColumnIndex("saldo"));
				}

			desc.setText(c.getString(c.getColumnIndex("nom")));
			if (document == 0) {
				SGEdit dtepp = (SGEdit) view.findViewById(R.id.client_dte_pp);
				dtepp.setText(c.getString(c.getColumnIndex("dtepp")));
				SGEdit dtecom = (SGEdit) view.findViewById(R.id.client_dte_com);
				dtecom.setText(c.getString(c.getColumnIndex("dtecomercial")));
				SGEdit dtegrup = (SGEdit) view.findViewById(R.id.client_dte_grup);
				dtegrup.setText(c.getString(c.getColumnIndex("dtegrup")));

			}

			c.close();
		} else
			desc.setText("???????");

	}

	Boolean read() {
		if (document > 0) {
			String param[] = { Long.toString(document) };
			cursor = helper.getWritableDatabase().rawQuery(
					"select * from Cap where _id=?", param);
			return cursor.getCount() > 0;
		}
		return false;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
		}
	};

	
	void Esborrar()
	{
		if (document == 0)
			return;
		getHelper().getWritableDatabase().delete("Linia",
				" docum=?",
				new String[] { Long.toString(document) });


		getHelper().getWritableDatabase().delete("Cap", " _id=?",
				new String[] { Long.toString(document) });
		helper.getReadableDatabase().execSQL(
				"update clients set comandespendents = comandespendents -1  "
						+ " where subjecte = '" + client + "' ");

	}

	
	void esborrarDocument() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					Esborrar();	
					Toast.makeText(getContext(),
							"Document esborrat " + Long.toString(document),
							Toast.LENGTH_SHORT).show();

					act.finish();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					Toast.makeText(
							getContext(),
							"NO s'ha esborrat el document "
									+ Long.toString(document),
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setMessage("Vol esborrar el document?")
				.setPositiveButton("Yes", dialogClickListener)
				.setNegativeButton("No", dialogClickListener).show();

	}

	
	public void gravar()
	{
		ContentValues cv = getCamps().UItoCv();
		try {
//			if (saldo <= 0)
//				Utilitats.Toast(act, "Atenció!! Client sense saldo",true);

			if (document == 0) {
				swAlta = true;
				document = ct.getComptador();
				for (;;) {
					String param[] = { Long.toString(document) };
					Cursor c = helper.getWritableDatabase().rawQuery(
							"select _id from Cap where _id=?", param);
					if (c.getCount() == 0)
						break;
					document++;

					Utilitats.inicialitzaPrecomandes(helper, null);
				}
				if (document < 0)
					Errors.appendLog(act, Errors.ERROR, "Cap",
							"No hi ha definits els comptadors");

				else {
					cv.put("s" + "tate", "A");
					cv.put("_id", document);
					long numregs = helper.insert("Cap", "", cv);
					ct.setComptador(document + 1);
					fDocument.setText(Long.toString(document));
					helper.getReadableDatabase()
					.execSQL(
							"update clients set comandespendents = comandespendents +1  "
									+ " where subjecte = '"
									+ client + "' ");						

				}
			} else {
				helper.update("Cap", "_id", cv);
			}
		} catch (SQLiteException e) {
			Errors.appendLog(act, Errors.ERROR, "Cap",
					"Error Gravació registre", e, cv, true);

		} finally {

		}
		
	}
	
	public void onClick(View v) {
		if (v == this.ico_delete) {
			esborrarDocument();
		}

		if (v == ico_back) {
			onBackPressed();
			return; // Perque no cridi al finish de la superclasse.
		}

		super.onClick(v);
		boolean cridaDetall = false;
		if (v == ico_help) {
			final Calendar c = Calendar.getInstance();

			dpk = new DatePickerDialog(act, mDateSetListener,
					c.get(Calendar.YEAR), c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH));
			dpk.show();

		}

		if (v == this || v == this.ico_save
				|| (v == this.ico_showlist && document == 0)) {
			cridaDetall = true;
			gravar();
		}
		if (cridaDetall || v.getId() == R.id.tplant_list_llista) {

			/*
			 * Si és una alta passem directament a Llistes de Selecció ja que no
			 * hi ha linies
			 */
			String programa = "Detall";
			if (swAlta) {
				programa = "DetallAlta";
				swAlta = false; // La propera vegada ha d'entrar a linies
			}
			Intent ClientsPerRutaIntent = new Intent(getAct().getBaseContext(),
					ExecTask.class);
			ClientsPerRutaIntent.putExtra("document", Long.toString(document));
			ClientsPerRutaIntent.putExtra("programa", programa);
			getAct().startActivity(ClientsPerRutaIntent);
		}
	}

	@Override
	public void destroy() {

	}

	@Override
	public void onBackPressed() {

		/* Si no hi ha linies sortim sense gravar */


		
		String param[] = { Long.toString(document) };
		cursor = helper.getWritableDatabase().rawQuery(
				"select * from Linia where docum=?", param);
		if (cursor.getCount() <= 0) { // Si no hi ha linies esborrar la comanda
			Esborrar();
			act.finish();
			return;
		}
		

		if (Utilitats.calculTotals(helper, document) > saldo)
			; //Utilitats.Toast(act,"Import supera Saldo",true);
			
		gravar();
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setMessage("Vol finalitzar la comanda?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finalitzarComanda();
								if (true) {
//									SendData p = new SendData();
//									p.send(act, "<AIXO ES UNA PROVA DE DADES>");
									Utilitats.enviarComandaPerMail(act, helper,
											document);
								}
								act.finish();

							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						act.finish();
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	void finalitzarComanda() {
		Cursor c = helper.getReadableDatabase().rawQuery(
				"select * from Cap where _id =" + document, null);
		int s = c.getCount();

		helper.getReadableDatabase().execSQL(
				"update linia set state='F' where docum =" + document);

		helper.getReadableDatabase().execSQL(
				"update Cap set state='F' where _id =" + document);

		Toast.makeText(getContext(),
				"Comanda Finalitzada en espera de tramesa : " + s,
				Toast.LENGTH_LONG).show();

	}

	@Override
	void build() throws Exception {

		getCamps().setTable("Cap");
		getCamps().setKey("_id");
		getCamps().setSqlList(
				"select _id,data from Cap where client='" + client + "'");
		getCamps().getCamps().add(
				(fDocument = new TFormField("_id", view
						.findViewById(R.id.client_doc))));
		getCamps().getCamps().add(
				new TFormField("client", view.findViewById(R.id.client_code)));
		getCamps().getCamps().add(
				new TFormField("entrega", view
						.findViewById(R.id.client_entrega),0,"","",InputType.TYPE_DATETIME_VARIATION_DATE));
		getCamps().getCamps().add(
				new TFormField("value", view.findViewById(R.id.client_total)));
		getCamps().getCamps().add(
				new TFormField("data", view.findViewById(R.id.client_date),0,"","",InputType.TYPE_DATETIME_VARIATION_DATE));
		getCamps().getCamps().add(
				new TFormField("dtecomercial", view
						.findViewById(R.id.client_dte_com)));
		getCamps().getCamps().add(
				new TFormField("dtepp", view.findViewById(R.id.client_dte_pp)));
		getCamps().getCamps()
				.add(new TFormField("dtegrup", view
						.findViewById(R.id.client_dte_grup)));
		getCamps().getCamps().add(
				new TFormField("state", view.findViewById(R.id.client_state)));
		getCamps().getCamps().add(
				new TFormField("notes", view.findViewById(R.id.client_obs)));

		TFormField pt = new TFormField("comentari",
				view.findViewById(R.id.spin_comentari));
		pt.setSqlLink("SELECT clau _id, descripcio FROM TAULES WHERE TAULA = 'XCA'");
		getCamps().getCamps().add(pt);

		pt = new TFormField("entrega_mati",
				view.findViewById(R.id.spin_entrega_mati));
		pt.setSqlLink("SELECT clau _id, descripcio FROM TAULES WHERE TAULA = 'CTRA'");
		getCamps().getCamps().add(pt);

		pt = new TFormField("recullen", view.findViewById(R.id.spin_recullen));
		pt.setSqlLink("SELECT clau _id, descripcio FROM TAULES WHERE TAULA = 'CENV'");
		getCamps().getCamps().add(pt);

		getCamps().ClearUI();
		getCamps().initialize();
		total = (SGEdit) view.findViewById(R.id.client_total);

		if (document == 0) {
			SGEdit clients = (SGEdit) view.findViewById(R.id.client_code);
			data = (SGEdit) view.findViewById(R.id.client_date);
			entrega = (SGEdit) view.findViewById(R.id.client_entrega);
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date date = new Date();
			data.setText(dateFormat.format(date));
			entrega.setText(dateFormat.format(date));

			postRead();

			clients.setText(client);
		}

	}

	// camps.add(new
	// FormField("familia",familia,R.id.articles_familia,"familia","select _id,descripcio from Filters"));

	/*
	 * wizard.setOnClickListener(new View.OnClickListener() { public void
	 * onClick(View v) { Intent ClientsPerRutaIntent = new Intent(getAct()
	 * .getBaseContext(), ExecTask.class);
	 * ClientsPerRutaIntent.putExtra("parametre1", client);
	 * ClientsPerRutaIntent.putExtra("programa", "Precomanda");
	 * getAct().startActivity(ClientsPerRutaIntent); } });
	 * 
	 * }
	 */

	@Override
	int getMantViewId() {
		return R.layout.cap;
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
