package sgb.orders;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LlistaPreComandes extends TPlantillaList {
	Boolean onReturnValue;
	String client;
	String document;
	String idLinia;
	String sql;
	String taula;
	Button querybut;
	SGEdit queryCamp;
	String ultSql;
	Boolean swServit; // Si activatm només apareixen els que no s'han
						// seleccionat previament

	public void onBackPressed() {
		queryCamp.close();
	}

	OnEditorActionListener onEditor = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			runSQL();
			return true;
		}
	};

	/***********************************************************************
	 * Event enviat per TPlantilla List quan premen la foto
	 ***********************************************************************/

	void touchImage(int i) {
		Intent cataleg = new Intent(getAct().getBaseContext(), Cataleg.class);
		cataleg.putExtra("sql", ultSql);
		cataleg.putExtra("position", i);
		getAct().startActivity(cataleg);
	};

	public LlistaPreComandes(ExecTask act, OrdersHelper helper, String client,
			String document, String sql, String taula, Boolean autoShow,
			Boolean returnValue) {
		super(act, helper, autoShow);
		PROGRAMA = "LlistaPreComandes";
		this.sql = sql;
		this.taula = taula;
		this.client = client;
		this.document = document;
		this.onReturnValue = returnValue;
		swServit = true;
		run();

	}

	public void runSQL() {
		String command = "";
		String[] temp = queryCamp.getText().toString().split(" ");

		if (swServit && false)
			command += " and (P.servit is null or P.servit = 0)  ";

		if (temp.length > 0 && temp[0].length() > 0) {
			for (int i = 0; i < temp.length; i++) {
				command = command + " and ";

				if (i == 0)
					command = command + "(";
				command = command + " descripcio like '%" + temp[i] + "%' ";
				if (temp.length == 1) // Només hi ha un camp de cerca
					command = command + " or A.article = '" + temp[i] + "' ";
			}
			command = command + ")";
		}
		ultSql = sql + command + " order by descripcio ";
		this.getCamps().setSqlList(ultSql);
		super.runSQL();
		InputMethodManager imm = (InputMethodManager) act
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);

	}

	public void onClick(View v) {
		super.onClick(v);
		if (v == this.ico_agenda) {
			Prefs prefs = Prefs.getInstance(act.getApplicationContext());
			String perFamilies = prefs.getString("perFamilia", "??");
			if (perFamilies.equals("L"))
				perFamilies = "Linies";
			else
				perFamilies = "Families";
			Intent FamiliesIntent = new Intent(getAct().getBaseContext(),
					ExecTask.class);
			FamiliesIntent.putExtra("programa", perFamilies);
			getAct().startActivity(FamiliesIntent);
		} else // if (v == querybut)
		{
			runSQL();

		}

	}

	@Override
	void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("PreComanda");
		this.getCamps().setKey("_id");

		/*
		 * this.getCamps() .setSqlList(
		 * "select _id,P.article,A.descripcio,servit from PreComanda P LEFT OUTER JOIN Articles A ON A.article = P.article where P.subjecte='"
		 * + client + "' and (servit is null or servit = 0) ");
		 */
		queryCamp = (SGEdit) view.findViewById(R.id.tplant_querycamp);

		if (queryCamp != null) {
			queryCamp.setVisibility(VISIBLE);
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
			queryCamp.setOnEditorActionListener(onEditor);
			queryCamp.setOnValidateEvent(onValidate);
			queryCamp.setTimer(3);

		}

		querybut = (Button) view.findViewById(R.id.tplant_querybut);
		if (querybut != null) {
			querybut.setVisibility(VISIBLE);
			querybut.setOnClickListener(this);
		}

		this.getCamps().setSqlList(sql + " order by descripcio ");

		this.getCamps().getCamps()
				.add(new TFormField("descripcio", R.id.listrow_text1));
		this.getCamps().getCamps()
				.add(new TFormField("article", R.id.listrow_text2));
		TFormField pt;
		this.getCamps().getCamps()
				.add(pt = new TFormField("text3", R.id.listrow_text3));
		this.getCamps().getCamps()
		.add(pt = new TFormField("text4", R.id.listrow_text4));
		this.getCamps().getCamps()
		.add(pt = new TFormField("text5", R.id.listrow_text5));
		pt.setSufixe("€");
		this.getCamps().getCamps()
				.add(new TFormField("article", R.id.listrow_icon));
		/*
		 * this.getCamps().getCamps() .add(new TFormField("tarifa1",
		 * R.id.listrow_numeric1 ));
		 */

		ultSql = sql;

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				cursor.moveToPosition(position);
				String art = cursor.getString(cursor.getColumnIndex("article"));
				if (onReturnValue == true) // És una familia o una línia
				{
					Intent returnIntent = new Intent();
					returnIntent.putExtra("article", art);
					act.setResult(Utilitats.RETURN_ARTICLE, returnIntent);
					act.finish();

				} else {
					DialogLinia dlg = new DialogLinia(getContext(),
							(ExecTask) act, taula, "", 0,
							LlistaPreComandes.this.getHelper(),
							LlistaPreComandes.this.getCursor());
					// dlg.getWindow().setBackgroundDrawable(new
					// ColorDrawable(0));

					dlg.setOnCanviaListener(new OnCanvia() {
						public void haCanviat(Boolean b) {
							getAdapter().notifyDataSetChanged();
							getCursor().requery();

						}
					});
					dlg.show();
				}

			}

		});
	}

	@Override
	int getRowViewId() {
		return R.layout.tplantillalist_row;

	}

	@Override
	int getPaint(Cursor c) {
		int pos = c.getColumnIndex("servit");
		if (pos > 0 && c.getDouble(pos) != 0.0)
			return Paint.STRIKE_THRU_TEXT_FLAG;
		return 0;

	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP | ICO_AGENDA;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1 | ROW_TEXT2 | ROW_TEXT3 | ROW_TEXT4 | ROW_TEXT5  | ROW_ICON /*
																		 * |
																		 * this.
																		 * ROW_EDITNUM
																		 * |
																		 * this.
																		 * ROW_BOTOINC
																		 */;
	}

	@Override
	public void haCanviat(Boolean b) {

	}
}
