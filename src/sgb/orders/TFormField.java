package sgb.orders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class PlantillaInvalidComponent extends Exception {

}

public class TFormField {
	private String Sufixe="";
	
	public String getSufixe() {
		return Sufixe;
	}


	public void setSufixe(String sufixe) {
		Sufixe = sufixe;
	}

	private String sqlName;
	private int id;
	View UIField;
	String sqlLink;
	String fieldsLink;
	int rId;
	int sqlType = -1;
	int paint;
	int inputType = 0;
	
	List<String> spinFields=null;

	public void LoadSpinner(Context context,OrdersHelper helper) {
		Spinner sp = (Spinner)UIField;
		spinFields = new ArrayList<String>();
		Cursor c = helper.execSQL(getSqlLink());
		SimpleCursorAdapter qc = new SimpleCursorAdapter(context,
				android.R.layout.simple_spinner_item, c,
				new String[] { "descripcio" },
				new int[] { android.R.id.text1 },
				CursorAdapter.IGNORE_ITEM_VIEW_TYPE);

		qc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp.setAdapter(qc);
		if (c.moveToFirst()) {
		    do {
				spinFields.add(c.getString(0));
		    } while (c.moveToNext());
		}
	}
	
	
	String getValue()
	{
		Spinner v = (Spinner)UIField;
		int position = v.getSelectedItemPosition();
		if (position != -1)
		{
			if (spinFields != null)
			{
				String borrar = spinFields.get(position);
				return  spinFields.get(position);
			}
		}
		return "";
	}

	
	void setValue(String valor)
	{
		if (spinFields != null)
		{
			int pos=0;
			if ( (pos=spinFields.indexOf(valor)) >= 0)
					{
					Spinner v = (Spinner)UIField;
					v.setSelection(pos);
					}
		}
	}

	
	int getPaint() {
		return paint;
	}

	void setPaint(int paint) {
		this.paint = paint;
	}

	void setText(String s) {
		if (UIField instanceof TextView)
			((TextView) UIField).setText(s);
	}

	public TFormField(String sqlName, View UIField, int rId, String fieldsLink,
			String sqlLink) throws Exception {
		this.sqlName = sqlName.toLowerCase();
		this.UIField = UIField;
		if (UIField == null)
			throw new Exception("Error : No es trova ID del camp : " + sqlName);
		
/*		if (UIField instanceof EditText)
			inputType = ((EditText)UIField).getInputType();
		else
			inputType = 0; */
		this.sqlLink = sqlLink;
		this.fieldsLink = fieldsLink;
		this.rId = rId;

	}

	public TFormField(String sqlName, View UIField, int rId, String fieldsLink,
			String sqlLink,int inputType) throws Exception {
		this(sqlName,UIField,rId,fieldsLink,sqlLink);
		this.inputType = inputType;
	}
	

	
	public String getFieldsLink() {
		return fieldsLink;
	}

	public void setFieldsLink(String fieldsLink) {
		this.fieldsLink = fieldsLink;
	}

	public int getrId() {
		return rId;
	}

	public void setrId(int rId) {
		this.rId = rId;
	}

	public String getSqlLink() {
		return sqlLink;
	}

	public void setSqlLink(String sqlLink) {
		this.sqlLink = sqlLink;
	}

	public String getSqlName() {
		return sqlName;
	}

	public void setSqlName(String sqlName) {
		this.sqlName = sqlName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public View getUIField() {
		return UIField;
	}

	public void setUIField(View uIField) {
		UIField = uIField;
	}

	int ValidateField(Cursor c) throws IOException {
		int pos = c.getColumnIndex(getSqlName());
		if (pos < 0)
			throw new IOException("?? Camp '" + getSqlName()
					+ "' a la Base de dades");
		return pos;

	}

	String getSqlField(Cursor c) throws IOException {
		int pos = ValidateField(c);
		if (inputType ==  InputType.TYPE_DATETIME_VARIATION_DATE)
		{
		String str = c.getString(pos);
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(str);
			str = new SimpleDateFormat("dd/MM/yyyy").format(date);		
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return str;
		
		}
			
		try {
			return c.getString(pos);
		} catch (Exception e) {
		}
		try {
			return Integer.toString(c.getInt(pos));
		} catch (Exception e) {
		}
		try {
			return Double.toString(c.getDouble(pos));
		} catch (Exception e) {
		}
		try {
			return Long.toString(c.getLong(pos));
		} catch (Exception e) {
		}
		return null;
	}

	public int getInputType() {
		return inputType;
	}


	public void setInputType(int inputType) {
		this.inputType = inputType;
	}


	public TFormField(String sqlName, View UIField) throws Exception {
		this(sqlName, UIField, 0, "", "");
	}

	public TFormField(String sqlName, int rId) throws Exception {
		this.sqlName = sqlName.toLowerCase();
		this.rId = rId;
	}

};