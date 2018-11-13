package sgb.orders;

import android.content.ContentValues;
import android.database.Cursor;

public class Comptadors {
	long id = 0;
	OrdersHelper helper;
	String 		PROGRAMA="Comptadors";
	
	
		Comptadors(OrdersHelper helper) {
			this.helper = helper;
	}

	long getComptador() {
		long comptador=1;
		String sql = "SELECT * FROM Comptadors "; // Només tindria que haber-hi
													// un registre
		Cursor cur = helper.getWritableDatabase().rawQuery(sql, null);
		String campsSql[] = cur.getColumnNames();
		int cont = cur.getCount();
		if (cur.getCount() > 0 && cur.moveToNext()) {
			id = cur.getInt(cur.getColumnIndex("_id"));
			comptador = cur.getInt(cur.getColumnIndex("cca"));
			cur.close();
			return comptador;
		}
		cur.close();
		return 1;

	}

	void setComptador(long doc) {
		ContentValues cv = new ContentValues();
		cv.put("_id", id);
		String s = Long.toString(doc);
		cv.put("cca", s);
		helper.replace("Comptadors", "_id", cv);
	}
}
