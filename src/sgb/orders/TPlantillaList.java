package sgb.orders;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/********************************************************
 * 
 * @author salvador
 * 
 * 
 * Basically I added this to my ListView:

android:descendantFocusability="beforeDescendants"

and added this to my activity:

android:windowSoftInputMode="adjustPan"
 * 
 * Els cursors Sqlite No son modificables pel que per poder fer un
 * listview on poguem modificar els EditText directament ha calgut
 * implementar un HasMap ja que el reQuery no reposiciona be el punt
 * d'edició.
 * Els HashMap updatedField registre la posició del cursor i el valor
 * modificat (de moment només un). La taula original cal modificar-la
 * manualment. (pendent d'implementar). 
 * 
 *
********************************************************/

class ImageViewClickListener implements OnClickListener {
	String position;

	public ImageViewClickListener(String pos) {
		this.position = pos;
	}

	public void onClick(View v) {// you can write the code what happens for the
									// that click and
									// you will get the selected row index in
									// position
									// Toast.makeText(v.getContext(),
									// " Has tocat imatge"+position,
		// Toast.LENGTH_SHORT).show();

	}
}

public abstract class TPlantillaList extends TPlantilla {
	protected DetailsAdapter adapter = null;
	protected Cursor cursor;
	protected ListView list;
	private TFormFields camps;
	protected View row;
	protected Boolean autoShow;
	protected Boolean swActivate;

	abstract int getRowViewId();


	public void OnPopulate(Cursor c,View v)
	{
		
	}

	
	void touchImage(int pos) {
	};

	int getPaint(Cursor f) {
		return 0;
	}

	int getPaint(Cursor f, View row) {
		return 0;
	}

	void setRowLayout(View v) {
		long ls = getRowsLayout();
		setRowLayoutProps(v, ls, ROW_CHECKBOX, R.id.listrow_checkbox);
		setRowLayoutProps(v, ls, ROW_ICON, R.id.listrow_icon);
		setRowLayoutProps(v, ls, ROW_TEXT1, R.id.listrow_text1);
		setRowLayoutProps(v, ls, ROW_TEXT2, R.id.listrow_text2);
		setRowLayoutProps(v, ls, ROW_TEXT3, R.id.listrow_text3);
		setRowLayoutProps(v, ls, ROW_TEXT4, R.id.listrow_text4);
		setRowLayoutProps(v, ls, ROW_TEXT5, R.id.listrow_text5);
		setRowLayoutProps(v, ls, ROW_ZOOM, R.id.listrow_zoom);
		setRowLayoutProps(v, ls, this.ROW_EDITNUM, R.id.listrow_numeric1);
		setRowLayoutProps(v, ls, this.ROW_BOTOINC, R.id.listrow_boto_comptador);
		// if ( (ls & ROW_ICON) != 0) {
		// rowIco = (ImageView)v.findViewById(R.id.listrow_icon);
		// rowIco.setOnClickListener(this);
		// }

		// setRowLayoutProps(v, ls, ROW_TEXT5, R.id.listrow_text5);
	}

	public void onClick(View v) {
		super.onClick(v);

	}

	public void Actualitza() {
		if (autoShow && swActivate == false) {
			swActivate = true;
			runSQL();
		}

	}

	public void Canvia() {
		if (cursor != null) {
			cursor.requery();
			adapter.notifyDataSetChanged();
		}
		/* Amagem el teclat */

		InputMethodManager imm = (InputMethodManager) act
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
	}

	public Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	abstract void build() throws Exception;

	public TPlantillaList(Activity act, OrdersHelper helper, Boolean autoShow) {
		super(act, helper);
		this.autoShow = autoShow;
		this.swActivate = false;

	}

	@Override
	public void activate() {
		Canvia();
	}

	public void run() {
		// helper = MenuOrders.helper;
		helper = this.helper;

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.tplantillalist, null);

		addView(view);
		camps = new TFormFields(helper, view);
		try {
			setButtons();
			build();
		} catch (Exception e) {
			Errors.appendLog(act, Errors.ERROR, "Cap",
					"Falta definir camp clau", e, null, true);

			return;
		}
	}

	public void runSQL() {
		String sql = camps.getSqlList();
		cursor = helper.execSQL(sql);
		int s = cursor.getCount();
		/*
		 * Esborrar !!! int r = cursor.getColumnCount();
		 * 
		 * if (s > 0) while (cursor.moveToNext()) { for (int i=0; i < r; i++) {
		 * String st = cursor.getString(i); String nm = cursor.getColumnName(i);
		 * String df = st; }
		 * 
		 * }
		 */

		// act.startManagingCursor(cursor);
		Context t = this.getContext();
		adapter = new DetailsAdapter(this, cursor);
		list.setAdapter(adapter);
	}

	class DetailsAdapter extends CursorAdapter {
		TPlantillaList pl;
		/* Guardem els valors del camp Modificat EditText */
		
		Map<Integer, String> updatedFields = new HashMap<Integer, String>();


		DetailsAdapter(TPlantillaList pl, Cursor c) {
			super(pl.getContext(), c);
			this.pl = pl;
		}

		@Override
		public void bindView(View row, Context ctxt, Cursor c) {
			DetailsHolder holder = (DetailsHolder) row.getTag();
			if (holder != null)
				try {
					holder.populateFrom(c, pl.getHelper());
					pl.OnPopulate(c,row);


					/* Personalitzar row */
					getPaint(c, row);

				} catch (IOException e) {
					Toast.makeText(pl.getContext(), e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

		}

		@Override
		public View newView(Context ctxt, Cursor c, ViewGroup parent) {
			DetailsHolder holder;
			View row = null;
			try {
				LayoutInflater inflater = ((Activity) ctxt).getLayoutInflater();
				int id = getRowViewId();
				row = inflater.inflate(id, parent, false);
				setRowLayout(row);
				holder = new DetailsHolder(row, pl, pl.getCamps(),updatedFields);
				row.setTag(holder);
			} catch (Exception e) {
				Toast.makeText(pl.getContext(), e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}
			return (row);
		}
	}

	class DetailsHolder {
		int position;
		TPlantillaList plantilla;
		ArrayList<String> camps;
		ArrayList<View> ids;
		Map<Integer,String> updatedFields ;
		
		
		ImageView zoom; // El posem peer controlar-ne els clicks

		DetailsHolder(View row, TPlantillaList plantilla, TFormFields cmp,Map<Integer,String> updatedFields)
				throws Exception {
			this.updatedFields = updatedFields;
			this.plantilla = plantilla;
			this.camps = new ArrayList<String>();
			this.ids = new ArrayList<View>();

			for (int i = 0; i < cmp.getCamps().size(); i++) {
				TFormField cField = cmp.getCamps().get(i);
				int id = cField.getrId();
				View vs = (View) row.findViewById(id);
				camps.add(cField.getSqlName());
				ids.add(vs);
			}
		}

		void populateFrom(Cursor c, final OrdersHelper helper)
				throws IOException {
			position = c.getPosition();
			if (zoom != null) {
				/*
				 * String sdcard =
				 * Environment.getExternalStorageDirectory().getAbsolutePath();
				 * String file = "file://"+sdcard+"/Galeria/000100.jpg"; file =
				 * "/sdcard/Galeria/000100.jpg"; BitmapFactory.Options options =
				 * new BitmapFactory.Options(); // will results in a much
				 * smaller image than the original options.inSampleSize = 8;
				 * 
				 * final Bitmap b = BitmapFactory.decodeFile(file, options);
				 * zoom.setImageBitmap(b); zoom.setOnClickListener(new
				 * ImageViewClickListener(c .getString(0)));
				 */
			}

			for (int i = 0; i < camps.size(); i++) {
				final int posi = i;
				View v = ids.get(i);

				// we need to update adapter once we finish with editing
				if (v instanceof EditText)
					v.setOnFocusChangeListener(new OnFocusChangeListener() {
						public void onFocusChange(View v, boolean hasFocus) {
							if (!hasFocus) {
								final EditText Caption = (EditText) v;
								Cursor cs = adapter.getCursor();
								cs.moveToPosition(position);
								String codiArt = cs.getString(cs.getColumnIndex("article"));
								String valCampAct2 = cs.getString(cs.getColumnIndex("tarifa1"));

								ContentValues cv = new ContentValues();
								String vl = Caption.getText().toString();

								cv.put("article", codiArt);
								cv.put("tarifa1", vl);
								updatedFields.put(position,vl);
								try {
									helper.update("articles", "article", cv);

								} catch (SQLiteConstraintException e) {
									Errors.appendLog(act, Errors.ERROR,
											PROGRAMA, "Error Insert linia", e,
											cv);
								}
//								cs.requery();

							}
						}
					});

				if (v instanceof ImageView) {
					String st = c.getString(c.getColumnIndex(camps.get(i)));
					ImageView vs = (ImageView) v;
					v.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Cursor cs = adapter.getCursor();
							cs.moveToPosition(position);
							plantilla.touchImage(position);
							// Toast.makeText(v.getContext(),
							// " Has tocat imatge : "+position+" : "+cs.getString(cs.getColumnIndex("descripcio")),
							// Toast.LENGTH_SHORT).show();

						}

					});

					/*
					 * Primer mirem si existexi la versió comprimida de la
					 * imatge que comença per _<Nom fitxer> . En cas contrari
					 * mirem si existeix la versio original, la comprimim i la
					 * gravem en versió reduida
					 */

					File file = Utilitats.getWorkFolder(act, Utilitats.IMAGES);
					if (file != null && file.exists()) {

						String smallFile = file.getAbsolutePath() + "/" + "_"
								+ st + ".jpg";

						BitmapFactory.Options options = new BitmapFactory.Options();
						// will results in a much smaller image than the
						// original

						final Bitmap b = BitmapFactory.decodeFile(smallFile,
								options);
						if (b == null) {
							String bigFile = file.getAbsolutePath() + "/" + st
									+ ".jpg";
							options.inSampleSize = 10;
							File f = new File(bigFile);
							if (f.exists()) { // Hi ha la versio gran de la
												// imatge
								final Bitmap btTmp = BitmapFactory.decodeFile(
										bigFile, options);
								try {
									FileOutputStream out = new FileOutputStream(
											smallFile);
									if (btTmp
											.compress(
													Bitmap.CompressFormat.JPEG,
													90, out))
										b.createBitmap(btTmp);
								} catch (Exception e) {
									e.printStackTrace();
								}

							}

						}
						vs.setImageBitmap(b);

						/*
						 * File f = new File(wFile); if (!f.exists()) { File Dir
						 * = Utilitats.getWorkFolder(act.getContext(),
						 * Utilitats.IMAGES); File[] files = Dir.listFiles(new
						 * FilenameFilter() { public boolean accept(File dir,
						 * String name) { name = name.toLowerCase(); return
						 * name.endsWith(".jpp") == true; } }); if (file != null
						 * && files.length > 0) { File fs = files[0]; String fsl
						 * = fs.getAbsolutePath(); if (files[0].renameTo(new
						 * File(wFile))) { String s = "Hola"; }
						 * 
						 * }
						 * 
						 * 
						 * 
						 * 
						 * 
						 * }
						 */

					}

					/*
					 * zoom.setOnClickListener(new ImageViewClickListener(c
					 * .getString(0)));
					 */

				}

				if (v instanceof TextView) {
					// TFormField f = act.getCamps().getCamps().get(i);
					/*
					 * Cursor cs = adapter.getCursor();
					 * cs.moveToPosition(position);
					 */
						
					String st = null;
					if (v instanceof EditText)
						st = updatedFields.get(position); 
					if (st == null)
					{
						String st1 = camps.get(i);
						int st2 = c.getColumnIndex(camps.get(i));
						if (st2 >= 0 )
							st = c.getString(st2);
						else
							st = "";
					}
					TextView vs = (TextView) v;
					
					vs.setText(st);
					vs.setPaintFlags(/* vs.getPaintFlags() | */getPaint(c));
				}

			}
		}
	}

	/************************************************************************
	 * 
	 * Getters & Setters
	 * 
	 ************************************************************************/

	public DetailsAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(DetailsAdapter adapter) {
		this.adapter = adapter;
	}

	public TFormFields getCamps() {
		return camps;
	}

	public void setCamps(TFormFields camps) {
		this.camps = camps;
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
}