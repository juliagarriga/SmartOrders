package sgb.orders;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class LlistaRutes extends TPlantillaList {
	int backpress=0;

	public LlistaRutes(Activity act, OrdersHelper helper) {
		super(act, helper,true);
		PROGRAMA="LlistaRutes";

		run();
	}
	
	
	@Override
	void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("rutes");
		this.getCamps().setKey("_id");
		this.getCamps().setSqlList("select ruta _id,descripcio, descripcio text3,ruta text4 from rutes order by descripcio ");
		this.getCamps().getCamps()
				.add(new TFormField("_id", R.id.listrow_text2));
		this.getCamps().getCamps()
		.add(new TFormField("descripcio", R.id.listrow_text1));
		this.getCamps().getCamps()
		.add(new TFormField("text3", R.id.listrow_text3));
		this.getCamps().getCamps()
		.add(new TFormField("text4", R.id.listrow_text4));
		list.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
	        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
	        	act.getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new
	        			KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
	            return true;
	        }
	    });

		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				cursor.moveToPosition(position);
				
				/* Grava Ruta actual a preferències */
				
				Prefs prefs = Prefs.getInstance(getContext());
				prefs.setString("codi_ruta",cursor.getString(cursor.getColumnIndex("_id")));
				prefs.setString("desc_ruta",cursor.getString(cursor.getColumnIndex("descripcio")));
				prefs.close();

				
				String ruta = cursor.getString(cursor.getColumnIndex("_id"));
				ClientsPerRutaIntent.putExtra("parametre1", ruta);
				ClientsPerRutaIntent.putExtra("programa", "LlistaClientsRuta");
				getAct().startActivity(ClientsPerRutaIntent);
			}
		});
		
		
		setOnLongClickListener(new View.OnLongClickListener() {
	        public boolean onLongClick(View v) {
	        	
//				Intent it = new Intent(act, ViewFlipperSample.class);
//				act.startActivity(it);
				return true;
	        }
	    });
		
		Utilitats.checkSD();
		if (Utilitats.mExternalStorageAvailable == false) 
			Toast.makeText(getContext(), "No es pot accedir a la Targeta SD", Toast.LENGTH_LONG).show();
		else
		if (Utilitats.mExternalStorageWriteable == false) 
			Toast.makeText(getContext(), "No es poden gravar dades a la Targeta SD", Toast.LENGTH_LONG).show();


	}

	@Override
	int getRowViewId() {
		return 	R.layout.rutes;
//		return R.layout.rutes;
	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP |  ICO_SHOWLIST | ICO_COMPASS | ICO_SORT;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1;
	}



	@Override
	public void haCanviat(Boolean b) {
		// TODO Auto-generated method stub
		
	}





}
