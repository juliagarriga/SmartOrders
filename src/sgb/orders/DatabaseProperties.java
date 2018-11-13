package sgb.orders;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Iterator;

	public class DatabaseProperties {
	OrdersHelper helper;
	

	ArrayList <TableProperties>tableProperties;
	
	DatabaseProperties(OrdersHelper helper) {
		this.helper = helper;
		tableProperties = new ArrayList<TableProperties>();
		String sql = "SELECT * FROM sqlite_master WHERE type='table'";
		Cursor cur =helper.execSQL(sql);
		while (cur.moveToNext()==true) {
			tableProperties.add(new TableProperties(this,cur.getString(cur.getColumnIndex("name"))));
		}
	}
	
	TableProperties getTableProperties(String s) {
		Iterator<TableProperties> e = tableProperties.iterator();
		while (e.hasNext()) {
			TableProperties st = (TableProperties) e.next();
			String nm = st.getTableName();
			if (nm.equalsIgnoreCase(s)) 
				return st;
		}
		return null;
			
		
	}
	
	
	
	public OrdersHelper getHelper() {
		return helper;
	}

	public void setHelper(OrdersHelper helper) {
		this.helper = helper;
	}


}
