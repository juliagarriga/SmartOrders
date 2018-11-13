package sgb.orders;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class LlistaDocumentEnviats extends TPlantillaList {

	public LlistaDocumentEnviats(Activity act, OrdersHelper helper) {
		super(act, helper, true);
		PROGRAMA = "LlistaDocumentPendents";
		run();

	}

	@Override
	void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("rutes");
		this.getCamps().setKey("_id");
		this.getCamps()
				.setSqlList(
				// "select *,L.subjecte from Cap  LEFT JOIN Clients L ON L.subjecte = subjecte where State <> 'E' ");
						"select strftime('%d-%m-%Y', data) dataf,* from Cap  left join Clients on (Clients.subjecte = Cap.client) where Cap.state = 'E' order by data desc ");

		this.getCamps().getCamps()
				.add(new TFormField("nom", R.id.listrow_text1));
		this.getCamps().getCamps()
				.add(new TFormField("dataf", R.id.listrow_text2));
		this.getCamps().getCamps()
				.add(new TFormField("value", R.id.listrow_text4));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				cursor.moveToPosition(position);
				String param = cursor.getString(cursor.getColumnIndex("_id"));
				String client = cursor.getString(cursor
						.getColumnIndex("client"));

				ClientsPerRutaIntent.putExtra("parametre1", client);
				ClientsPerRutaIntent.putExtra("parametre2", param);
				ClientsPerRutaIntent.putExtra("programa", "Cap");
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
