package sgb.orders;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.IOException;




public abstract class TPlantillaMant extends TPlantilla  implements OnClickListener {

	protected Cursor cursor;
	TFormFields camps;
	enum 	 BdStatus {ALTA,BAIXA,MODIFICACIO }; 
	BdStatus bdStatus;


	void postRead() {
	}	

	void postNotRead() {
	}	

	
	abstract void build() throws Exception;
	abstract Boolean read();
	

	public void onClick(View v) {
		super.onClick(v);
	}	
	
	
	public void activate() {
	}
	
	public Activity getAct() {
		return act;
	}



	public void setAct(Activity act) {
		this.act = act;
	}



	public OrdersHelper getHelper() {
		return helper;
	}



	public void setHelper(OrdersHelper helper) {
		this.helper = helper;
	}



	public TPlantillaMant(Activity act,OrdersHelper helper) {
		super(act,helper);
	}


	/******************************************************************
	 * Llegueix el Registre segons la clau donada i el traspassa a la UI
	/******************************************************************/
	
/*	public Boolean read(String key) {
		String clau = camps.getKeys().get(0);
		String sql = "select * from "+camps.getTable()+" where "+clau+ "="+key;
		cursor = helper.execSQL(sql);
		int regs = cursor.getCount();
		return regs != 0;
	} */

	abstract int getMantViewId();
	
	
	public Boolean init() {
		int id = getMantViewId();
		
//		helper = MenuOrders.helper;
//	    helper=new OrdersHelper(act);


		LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(id, null);
		setButtons();
		addView(view);

		camps = new TFormFields(helper,view); 
		try {
			build();
		} catch (Exception e) {
			Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}  // Generada per la superclasse
		if (camps.validate())
			if (start())
				postRead();
			else
				postNotRead();
		return null;
	}

	public Boolean start() {
       if (read())
			try {
				cursor.moveToNext();
				camps.SqlToUI(cursor);
				return true;
			} catch (IOException e) {
				Errors.appendLog(act, Errors.ERROR, "TPlantillaMant", "Error Read", e,
						null, true);

			}
 		return false;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	public TFormFields getCamps() {
		return camps;
	}

	public void setCamps(TFormFields camps) {
		this.camps = camps;
	}

	public BdStatus getBdStatus() {
		return bdStatus;
	}

	public void setBdStatus(BdStatus bdStatus) {
		this.bdStatus = bdStatus;
	}


} 


