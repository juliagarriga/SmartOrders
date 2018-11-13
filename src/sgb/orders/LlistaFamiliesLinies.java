package sgb.orders;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class LlistaFamiliesLinies extends TPlantillaList {
	private Boolean onReturn = false;
	private String tip;

	public LlistaFamiliesLinies(Activity act, OrdersHelper helper,Boolean onReturn,String tip) {
		super(act, helper,true);
		this.onReturn = onReturn;
		PROGRAMA = "LlistaFamilies";
		this.tip = tip;
		run();

	}

	@Override
	void build() throws Exception {
		list = (ListView) view.findViewById(R.id.tplantillalist_list);
		this.getCamps().setTable("families");
		this.getCamps().setKey("_id");
		if (tip == "F")
		this.getCamps()
				.setSqlList(
				// "select *,L.subjecte from Cap  LEFT JOIN Clients L ON L.subjecte = subjecte where State <> 'E' ");
						"select familia _id,descripcio from Families ");
		else
			this.getCamps()
			.setSqlList(
			// "select *,L.subjecte from Cap  LEFT JOIN Clients L ON L.subjecte = subjecte where State <> 'E' ");
					"select linia _id,descripcio from Linies ");
						
						
		this.getCamps().getCamps()
				.add(new TFormField("_id", R.id.listrow_text2));
		this.getCamps().getCamps()
				.add(new TFormField("descripcio", R.id.listrow_text1));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				cursor.moveToPosition(position);
				String param = cursor.getString(cursor.getColumnIndex("_id"));
				String descripcio = cursor.getString(cursor.getColumnIndex("descripcio"));
				Intent it = new Intent(act,ExecTask.class);
				it.putExtra("tipus",tip);
				it.putExtra("programa", "Familia");
				it.putExtra("retorn",false);
				it.putExtra("parametre1", param);
				it.putExtra("parametre2", descripcio);
				if (onReturn) {
					act.startActivityForResult(it,2);
				}
				else
					act.startActivity(it);
/*				if (onReturn) {
					 Intent returnIntent = new Intent();
					 returnIntent.putExtra("article","nene");
					 act.setResult(Utilitats.RETURN_ARTICLE,returnIntent); 
	
					act.finish(); */
				

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
		return ROW_TEXT1 | ROW_TEXT2 ;
	}

	@Override
	public void haCanviat(Boolean b) {
		// TODO Auto-generated method stub
		
	}

	

}