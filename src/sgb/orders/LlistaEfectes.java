package sgb.orders;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class LlistaEfectes extends TPlantillaList {
	String client;

	public LlistaEfectes(Activity act, OrdersHelper helper,String client) {
		super(act, helper,true);
		this.client = client;
		PROGRAMA = "Llista Efectes";
		run();

	}

	@Override
	void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("Efectes");
		this.getCamps().setKey("numdoc");
		
		String Op[] = client.split("-");
		String grupcli = Op[0];
		this.getCamps()
				.setSqlList(
				// "select *,L.subjecte from Cap  LEFT JOIN Clients L ON L.subjecte = subjecte where State <> 'E' ");
						"select numdoc _id,* from Efectes where subjecte = '"+grupcli+ "' order by data_venciment desc ");
						
						
		this.getCamps().getCamps()
				.add(new TFormField("_id", R.id.listrow_text2));
		this.getCamps().getCamps()
				.add(new TFormField("factura", R.id.listrow_text4));
		this.getCamps().getCamps()
				.add(new TFormField("import", R.id.listrow_text1));
		this.getCamps().getCamps()
				.add(new TFormField("data_venciment", R.id.listrow_text3));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

/*				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				cursor.moveToPosition(position);
				String param = cursor.getString(cursor.getColumnIndex("_id"));
				ClientsPerRutaIntent.putExtra("parametre1", 0);
				ClientsPerRutaIntent.putExtra("parametre2", param);
				ClientsPerRutaIntent.putExtra("programa", "Cap");
				getAct().startActivity(ClientsPerRutaIntent); */
	

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

	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1 | ROW_TEXT2 | ROW_TEXT3 | ROW_TEXT4;
	}

	@Override
	public void haCanviat(Boolean b) {
		// TODO Auto-generated method stub
		
	}



}
