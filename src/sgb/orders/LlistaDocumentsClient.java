package sgb.orders;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class LlistaDocumentsClient extends TPlantillaList {
	private String sql;

	private String client = "";

	public LlistaDocumentsClient(Activity act, OrdersHelper helper,
			String client, String sql) {
		super(act, helper, true);
		PROGRAMA = "LlistaDocumentClient";
		this.sql = sql;
		this.client = client;
		run();
	}

	public void onClick(View v) {
		super.onClick(v);
		if (v == this || v == this.ico_add) {
			Intent intent = new Intent(getAct().getBaseContext(),
					ExecTask.class);
			Utilitats.inicialitzaPrecomandes(helper, null);
			intent.putExtra("parametre1", client);
			intent.putExtra("programa", "NouDocument");
			int requestCode = 0;
			getAct().startActivityForResult(intent, requestCode);
			cursor.requery();
			adapter.notifyDataSetChanged();
		}
	}

	void RunCap()
	{
		
	}
	
	@Override
	void build() throws Exception {
		/*
		 * ImageView vl =
		 * (ImageView)view.findViewById(R.id.tplantillalist_list);
		 * gravar.setVisibility(GONE);
		 */

		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("Cap");
		this.getCamps().setKey("_id");
		String cSql = "select * from Cap LEFT OUTER JOIN Clients C ON C.subjecte = client where client='" + client + "' " + sql;
		
//		String cSql = "select * from Cap where client='" + client + "' " + sql;
		this.getCamps().setSqlList(cSql);
		this.getCamps().getCamps()
				.add(new TFormField("nom", R.id.listrow_text1));
		this.getCamps().getCamps()
				.add(new TFormField("data", R.id.listrow_text2));
		this.getCamps().getCamps()
				.add(new TFormField("value", R.id.listrow_text3));
		
		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent ClientsPerRutaIntent = new Intent(getAct()
						.getBaseContext(), ExecTask.class);
				cursor.moveToPosition(position);
				Utilitats.inicialitzaPrecomandes(helper, null);
				String param = cursor.getString(cursor.getColumnIndex("_id"));
				ClientsPerRutaIntent.putExtra("parametre1", client);
				ClientsPerRutaIntent.putExtra("parametre2", param);
				ClientsPerRutaIntent.putExtra("programa", "Cap");
				getAct().startActivity(ClientsPerRutaIntent);
			}
		});
	}

	@Override
	int getPaint(Cursor cur, View row) {
		ImageView v = (ImageView) row.findViewById(R.id.listrow_icon);
		if (v != null) {
			String state = cur.getString(cur.getColumnIndex("state"));
			if (state == null || state == "A")
				v.setImageResource(R.drawable.ball_green);
			else if (state.equals("F"))
				v.setImageResource(R.drawable.send);
			else if (state.equals("E"))
				v.setImageResource(R.drawable.tick);
			else
				v.setImageResource(R.drawable.tick);
		}
		return 0;
	}

	@Override
	int getRowViewId() {
		return R.layout.tplantillalist_row;
	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP | ICO_ADD;
	}

	@Override
	long getRowsLayout() {
		return ROW_TEXT1 | ROW_TEXT2 | ROW_TEXT3 | ROW_TEXT4 | ROW_ICON;

	}

	@Override
	public void haCanviat(Boolean b) {
		// TODO Auto-generated method stub

	}

}
