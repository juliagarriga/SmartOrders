package sgb.orders;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class TFormFields {
	private String sqlList;
	private String table;
	private OrdersHelper helper = null;
	private ArrayList<TFormField> camps;
	private ArrayList<String> keys;
	private View view;
	private Activity act;

	public void setAct(Activity act) {
		this.act = act;
	}

	public Boolean validate() {
		if (getCamps().size() == 0) {

			// Errors.appendLog(view.getContext(),Errors.ERROR,"Cap","Falta definir els camps");
			return false;
		}

		/*
		 * if (sqlList.isEmpty()) { new TShowError().show(view.getContext(),
		 * "Falta definir els SqlList"); return false; }
		 */

		if (getKeys().size() == 0) {

			Errors.appendLog(act, Errors.ERROR, "Cap",
					"Falta definir camp clau");
			return false;
		}
		if (getTable() == null) {
			Errors.appendLog(act, Errors.ERROR, "Cap",
					"Falta definir nom de la taula");
			return false;
		}
		return true;

	}

	public void setCamps(ArrayList<TFormField> camps) {
		this.camps = camps;
	}

	public ArrayList<String> getKeys() {
		return keys;
	}

	public void setKeys(ArrayList<String> keys) {
		this.keys = keys;
	}

	TFormFields(OrdersHelper helper, View view) {
		this.helper = helper;
		this.view = view;
		camps = new ArrayList<TFormField>();
		keys = new ArrayList<String>();
	}

	public void add(TFormField f) throws Exception {
		if (f.getUIField() == null) {
			throw new Exception("?? ID del camp : " + f.getSqlName());
		}
		camps.add(f);

	}

	public void initialize() {
		Iterator<TFormField> e = camps.iterator();
		while (e.hasNext()) {
			TFormField s = (TFormField) e.next();
			if (!(s.getSqlName() == null))
				if (s.getUIField() instanceof Spinner) {
					s.LoadSpinner(view.getContext(), helper);
				}
		}
	}

	public void SqlToUI(Cursor c) throws IOException {
		Iterator<TFormField> e = camps.iterator();
		while (e.hasNext()) {
			TFormField s = (TFormField) e.next();
			View v = s.getUIField();
			String st = "";
			if (v instanceof TextView) {
				st = s.getSqlField(c);
				// st = c.getString(c.getColumnIndex(s.getSqlName()));
				((TextView) v).setText(st);
			}
			if (v instanceof Spinner) {
				st = s.getSqlField(c);
				s.setValue(st);
			}
		}
	}

	public void ClearUI() {
		Iterator<TFormField> e = camps.iterator();
		while (e.hasNext()) {
			TFormField s = (TFormField) e.next();
			View v = s.getUIField();
			String st = "";
			if (v instanceof EditText) {
				st = "";
				((EditText) v).setText(st);
			}
		}
	}

	/******************************************************************
	 * 
	 * Getters & Setters
	 * 
	 ******************************************************************/

	public ContentValues UItoCv() {
		ContentValues cv = new ContentValues();
		Iterator<TFormField> e = camps.iterator();
		while (e.hasNext()) {
			TFormField s = (TFormField) e.next();
			View v = s.getUIField();
			String st = "";
			if (s.getInputType() ==  InputType.TYPE_DATETIME_VARIATION_DATE)
			{
				st = ((TextView) v).getText().toString();
				Date date = null;
				try {
					date = new SimpleDateFormat("dd/MM/yyyy").parse(st);
					st = new SimpleDateFormat("yyyy-MM-dd").format(date);		
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

				cv.put(s.getSqlName(), st);
			}
			else
			
			
			if (v instanceof TextView) {
				st = ((TextView) v).getText().toString();
				cv.put(s.getSqlName(), st);
			} else if (v instanceof Spinner) {
				cv.put(s.getSqlName(), s.getValue());
			}

		}
		return cv;
	}

	public OrdersHelper getHelper() {
		return helper;
	}

	public void setHelper(OrdersHelper helper) {
		this.helper = helper;
	}

	public View getAct() {
		return view;
	}

	public void setAct(View view) {
		this.view = view;
	}

	public ArrayList<TFormField> getCamps() {
		return camps;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getSqlList() {
		return sqlList;
	}

	public void setSqlList(String sqlList) {
		this.sqlList = sqlList;
	}

	public void setKey(String s) {
		keys.add(s);
	}

};
