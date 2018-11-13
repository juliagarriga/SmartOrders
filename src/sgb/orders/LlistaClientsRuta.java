package sgb.orders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LlistaClientsRuta extends TPlantillaList {
	String ruta;
	String sql;
	Button querybut;
	SGEdit queryCamp;
	Boolean swPrimer=false;
	
	
	public void OnPopulate(Cursor c, View v) {
		TextView boto = (TextView) v.findViewById(R.id.listrow_boto_comptador);
		if (swPrimer==false) {
			swPrimer = !swPrimer;
			boto.setBackgroundResource(R.drawable.tick); 
		}
			
			

		int dt = c.getInt(c.getColumnIndex("comandespendents"));
		if (dt > 0)
			boto.setVisibility(VISIBLE);
		else
		boto.setVisibility(INVISIBLE);

	}


	public LlistaClientsRuta(Activity act, OrdersHelper helper, String ruta) {
		super(act, helper, true);
		PROGRAMA = "LlistaClientsRuta";

		this.ruta = ruta;
		run();

	}

	@Override
	void build() throws Exception {
		querybut = (Button) view.findViewById(R.id.tplant_querybut);
		if (querybut != null) {
			querybut.setVisibility(VISIBLE);
			querybut.setOnClickListener(this);
		}

		queryCamp = (SGEdit) view.findViewById(R.id.tplant_querycamp);
		OnValidateEvent onValidate = new OnValidateEvent() {
			@Override
			public Boolean validate(View v) {
				act.runOnUiThread(new Runnable() {
					public void run() {
						onClick(querybut);
					}
				});

				return true;
			}

		};

		OnEditorActionListener onEditor = new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				onClick(querybut);
				return true;
			}
		};

		queryCamp.setOnEditorActionListener(onEditor);
		queryCamp.setOnValidateEvent(onValidate);
		queryCamp.setTimer(3);

		if (queryCamp != null)
			queryCamp.setVisibility(VISIBLE);
		
		
		String order = Utilitats.getValue("OrderCliRuta");

		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("rutes");
		this.getCamps().setKey("_id");
		if (ruta == null || ruta.length() <= 0) {
			sql = "select C.Subjecte _id,nom,tarifa,comandespendents from Clients C  where subjecte is not null ";
		} else {
			sql = "select C.Subjecte _id,nom,tarifa,comandespendents from Clients C LEFT OUTER JOIN CliRuta R ON C.subjecte = R.subjecte where R.Ruta='"
					+ ruta + "' ";

		}
		if (order != null)
		this.getCamps()
			.setSqlList(sql + order);
		else
		this.getCamps()
			.setSqlList(sql + " order by ORDRE");


		this.getCamps().getCamps()
				.add(new TFormField("_id", R.id.listrow_text2));
		this.getCamps().getCamps()
		.add(new TFormField("nom", R.id.listrow_text1));
		this.getCamps().getCamps()
		.add(new TFormField("comandespendents", R.id.listrow_text5));

		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int index, long arg3) {
				// String str=listView.getItemAtPosition(index).toString();
				// Toast.makeText(v.getContext(), " Has tocat LONG",
				// Toast.LENGTH_SHORT).show();

				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				cursor.moveToPosition(index);

				String client = cursor.getString(cursor.getColumnIndex("_id"));
				ClientsPerRutaIntent.putExtra("parametre1", client);
				ClientsPerRutaIntent.putExtra("programa", "Client");
				getAct().startActivity(ClientsPerRutaIntent);

				return true;
			}
		});

		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				cursor.moveToPosition(position);

				/* Grava Client actual a preferències */

				Prefs prefs = Prefs.getInstance(getContext());
				prefs.setString("codi_cli",
						cursor.getString(cursor.getColumnIndex("_id")));
				prefs.setString("desc_cli",
						cursor.getString(cursor.getColumnIndex("nom")));
				prefs.setString("tarifa_cli",
						cursor.getString(cursor.getColumnIndex("tarifa")));
				prefs.close();

				String client = cursor.getString(cursor.getColumnIndex("_id"));
				ClientsPerRutaIntent.putExtra("parametre1", client);
				ClientsPerRutaIntent.putExtra("programa", "LlistaDocuments");
				getAct().startActivity(ClientsPerRutaIntent);
			}
		});
	}

	@Override
	int getRowViewId() {
		return R.layout.tplantillalist_row;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (v == this.ico_add) {
			Intent clients = new Intent(getAct().getBaseContext(),
					ExecTask.class);
			clients.putExtra("programa", "Clients");
			clients.putExtra("parametre1", "");
			clients.putExtra("parametre2", ruta);
			getAct().startActivity(clients);

		}

		if (v == querybut) {
			String[] temp = queryCamp.getText().toString().split(" ");
			if (temp.length > 0) {
				String command = "";
				for (int i = 0; i < temp.length; i++) {
					command = command + " and ";

					if (i == 0)
						command = command + "(";
					command = command + " nom like '%" + temp[i] + "%' ";
					command = command + " or  C.subjecte like '%" + temp[i]
							+ "%' ";
				}
				command = command + ")";

				String newSql = sql + command;
				this.getCamps().setSqlList(newSql + " order by nom");
				runSQL();
				InputMethodManager imm = (InputMethodManager) act
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
			}
		}
	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP | ICO_ADD;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1 | ROW_TEXT2 | ROW_TEXT5;
	}

	@Override
	public void haCanviat(Boolean b) {
		// TODO Auto-generated method stub

	}

}
