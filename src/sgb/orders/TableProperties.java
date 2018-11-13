package sgb.orders;

import android.database.Cursor;

import java.util.ArrayList;

public class TableProperties {
	String name;
	ArrayList<TableFieldProperties> tableFieldProperties;

	TableProperties(DatabaseProperties database, String name) {
		this.name = name;

		tableFieldProperties = new ArrayList<TableFieldProperties>();
		OrdersHelper helper = database.getHelper();
		String sql = "PRAGMA TABLE_INFO(" + name + ")";
		Cursor cur = helper.execSQL(sql);
		while (cur.moveToNext() == true) {
			TableFieldProperties field = new TableFieldProperties(this);
			field.setId(cur.getInt(cur.getColumnIndex("cid")));
			field.setName(cur.getString(cur.getColumnIndex("name")));
			field.setType(cur.getString(cur.getColumnIndex("type")));
			field.setNotnull(cur.getInt(cur.getColumnIndex("notnull")));
			field.setDfltValue(cur.getString(cur.getColumnIndex("dflt_value")));
			field.setPk(cur.getInt(cur.getColumnIndex("pk")));
			tableFieldProperties.add(field);
		}

	}
	String getTableName()
	{
		return name;
	}
	public ArrayList<TableFieldProperties> getTableFieldProperties() {
		return tableFieldProperties;
	}
	}


