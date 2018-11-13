package sgb.orders;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class LlistaLinies extends TPlantillaList {
	long document;
	

	public LlistaLinies(Activity act, OrdersHelper helper, String document,Boolean alta) {
		super(act, helper,true);
		PROGRAMA = "LlistaLinies";
		

		this.document = Long.parseLong(document);
		run();
		
		if (alta) /* Ja no es para a línies ja que no n'hi ha cap */
			executaPrecomandes();


	}

	
	public void executaPrecomandes()
	{

		/* Grava Document actual a preferències */

		Prefs prefs = Prefs.getInstance(getContext());
		prefs.setString("document", Long.toString(document));
		prefs.close();

		Intent intent = new Intent(getAct().getBaseContext(),
				ExecTask.class);
		intent.putExtra("parametre1", Long.toString(document));
		intent.putExtra("parametre2", Long.toString(0));
		intent.putExtra("programa", "Linia");
		int requestCode = 0;
		getAct().startActivityForResult(intent, requestCode);
	}
	
	
	public void onClick(View v) {

		if (v == this || v.getId() == R.id.tplant_list_add)
		{
			executaPrecomandes();
			cursor.requery();
			adapter.notifyDataSetChanged();

		}
		super.onClick(v);
	}

	public void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("Linies");
		this.getCamps().setKey("_id");
		this.getCamps()
				.setSqlList(
						"select L._id _id,L.article,L.quant,L.preunet,A.descripcio from Linia L LEFT OUTER JOIN Articles A ON A.article = L.article where L.docum ="
								+ document + "");

		this.getCamps().getCamps()
				.add(new TFormField("article", R.id.listrow_text2));
		this.getCamps().getCamps()
				.add(new TFormField("descripcio", R.id.listrow_text1));
		this.getCamps().getCamps()
				.add(new TFormField("quant", R.id.listrow_text3));
		this.getCamps().getCamps()
				.add(new TFormField("preunet", R.id.listrow_text4));
		// this.getCamps().getCamps()
		// .add(new TFormField("quantitat", R.id.listrow_text4));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String taula="";

				Prefs prefs = Prefs.getInstance(getContext());
				prefs.setString("document", Long.toString(document));
				prefs.close();

				
				DialogLinia dlg = new DialogLinia(getContext(), 
						(ExecTask)act, taula, "",id,
						LlistaLinies.this.getHelper(),LlistaLinies.this.getCursor());
				dlg.setOnCanviaListener(LlistaLinies.this);
				dlg.show();
				/*
				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				Intent intent = new Intent(getAct().getBaseContext(),
						ExecTask.class);
				intent.putExtra("parametre1", Long.toString(document));
				intent.putExtra("parametre2", Long.toString(id));
				intent.putExtra("programa", "XLinia");
				int requestCode = 0;
				getAct().startActivityForResult(intent, requestCode);
				cursor.requery();
 				cursor.moveToPosition(position); */
			}
		});
	}

	@Override
	int getRowViewId() {
		return R.layout.tplantillalist_row;

	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP | ICO_ADD ;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1 | ROW_TEXT2 | ROW_TEXT4 | ROW_TEXT3;
	}


	@Override
	public void haCanviat(Boolean b) {
		cursor.requery();
		
	}


}
