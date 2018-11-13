package sgb.orders;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import sgb.orders.Utilitats.TPreus;

public class XLinia extends TPlantillaMant implements OnCanvia {
	SGEdit quantitat;
	Spinner familia;
	Spinner articleSeleccionat;
	SGEdit preu;
	SGEdit article;
	TFormField flArticle;
	TextView descripcio;
	private int id;
	private int document;
	Boolean swActiveFamilia = false;

	String wtarifa;
	String wsubjecte;
	String wdescripcio;
	String wdocument;

	final String PROGRAMA = "XLinia";

	void readArticle(String art) {
		if (Utilitats.readArticles(helper, art,null) == true) {
			article.setText(Utilitats.getCursorArt().getString(
					Utilitats.getCursorArt().getColumnIndex("article")));
			descripcio.setText(Utilitats.getCursorArt().getString(
					Utilitats.getCursorArt().getColumnIndex("descripcio")));
			if (id == 0) {
				double wpreu = 0.0;
				String familia="",linia="";
				double quantitat = 0.0;
				
				TPreus preus = Utilitats.readPreus(act,helper, wsubjecte, wtarifa, art,familia,linia,quantitat);
//				wpreu = Utilitats.getPreu();
				preu.setText(new DecimalFormat("####0.00").format(wpreu));

			}
			Utilitats.getCursorArt().close();
		}

	}

	@Override
	void postRead() {
		if (id != 0)
			article.lookUp();
		// readArticle(article.getText().toString());
	}

	public void onClick(View v) {
		super.onClick(v);
		if (v == this.ico_delete) {
			getHelper().getWritableDatabase().delete("Linia"," _id=?",new  String[] { Integer.toString(id) });
			Toast.makeText(getContext(), "Resgistre esborrat " + Long.toString(id),
					Toast.LENGTH_SHORT).show();
			Utilitats.inicialitzaPrecomandes(helper, Integer.toString(id));
			
			act.finish();
		}
		if (v == this.ico_save) {

			if (article.getValidate() == false) {
				Toast.makeText(getContext(),
						"Article no validat. Gravació cancel.lada ",
						Toast.LENGTH_SHORT).show();
				return;

			}

			ContentValues cv = getCamps().UItoCv();
			cv.put("docum", document);
			try {
				long rt = 0;
				if (id == 0) {
					rt = getHelper().getWritableDatabase().insertOrThrow(
							"Linia", "", cv);
				} else {
					cv.put("_id", id);
					rt = getHelper().update("Linia", "_id", cv);
					if (rt < 0)
						Errors.appendLog(act, Errors.ERROR, PROGRAMA,
								"Error Update linia", null, cv);
				}

				InputMethodManager imm = (InputMethodManager) getAct()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindowToken(), 0);
				Toast.makeText(getContext(), "Gravat " + Long.toString(rt),
						Toast.LENGTH_SHORT).show();
				act.finish();

			} catch (SQLiteConstraintException e) {
				Errors.appendLog(act, Errors.ERROR, PROGRAMA,
						"Error inserint linia", e, cv);
				Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	public XLinia(Activity act, OrdersHelper helper, String document, int id) {
		super(act, helper);
		if (document == null) {
			return;
		}
		this.document = Integer.parseInt(document);
		this.id = id;

		init();
	}

	@Override
	void build() throws Exception {

		Prefs prefs = Prefs.getInstance(getContext());
		wtarifa = prefs.getString("tarifa_cli", "");
		wsubjecte = prefs.getString("codi_cli", "");
		wdescripcio = prefs.getString("desc_cli", "");
		wdocument = prefs.getString("document", "");
		prefs.close();

		articleSeleccionat = (Spinner) findViewById(R.id.linies_subgrup);
		articleSeleccionat
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						article.setText(Long.toString(id));
						article.lookUp();
						InputMethodManager imm = (InputMethodManager) getAct()
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(article, InputMethodManager.SHOW_IMPLICIT);						
						// readArticle(Long.toString(id));
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});

		familia = (Spinner) findViewById(R.id.linies_familia);
		familia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// String sql = "select sql from Filtres where _id = " + id;
				String sql = "select article _id,descripcio from Articles where familia = "
						+ id;

				/* Per evitar que entri al obrir el formulari */

				if (swActiveFamilia) {
					
					/* Amagem el teclat perqué no molesti  */
					InputMethodManager imm = (InputMethodManager) act
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);

					Cursor ctr = getHelper().execSQL(sql);
					if (ctr.getCount() > 0) {
						// ctr.moveToFirst();
						// sql = ctr.getString(0);
						// Cursor c = getHelper().execSQL(sql);
						// if (c.getCount() > 0)
						{
							SimpleCursorAdapter qc = new SimpleCursorAdapter(
									getContext(),
									android.R.layout.simple_spinner_item, ctr,
									new String[] { "descripcio" },
									new int[] { android.R.id.text1 },
									CursorAdapter.IGNORE_ITEM_VIEW_TYPE);

							qc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
							articleSeleccionat.setAdapter(qc);
						}
					}
				}
				swActiveFamilia = true;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		getCamps().setTable("Linia");
		getCamps().setKey("_id");

		descripcio = (TextView) findViewById(R.id.linies_descripcio);

		TFormField sub = new TFormField("article",
				findViewById(R.id.linies_article));
		SGEdit tx = (SGEdit) findViewById(R.id.linies_article);
		camps.add(flArticle = new TFormField("article",
				findViewById(R.id.linies_article)));
		article = (SGEdit) flArticle.getUIField();
		article.setSQLValidation(helper,
				"select article,descripcio from articles where article=?", this);
		camps.add(new TFormField("preu",
				preu = (SGEdit) findViewById(R.id.linies_preu)));
		camps.add(new TFormField("notes",
				 (SGEdit) findViewById(R.id.observacions)));
		camps.add(new TFormField("quant",
				quantitat = (SGEdit) findViewById(R.id.linies_quantitat)));
		camps.add(new TFormField("familia", familia, R.id.articles_familia,
				"familia", "select familia _id,descripcio from Families"));
		camps.add(sub);
		camps.initialize();
		getCamps().ClearUI();
		InputMethodManager imm = (InputMethodManager) getAct()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(article, InputMethodManager.SHOW_IMPLICIT);

		// camps.add(new
		// FormField("familia",familia,R.id.articles_familia,"familia","select _id,descripcio from Filters"));
	}

	@Override
	int getMantViewId() {
		return R.layout.linies;
	}

	@Override
	long getButtons() {
		return ICO_BACK | ICO_HELP | ICO_SAVE | ICO_DELETE;
	}

	@Override
	long getRowsLayout() {
		return 0;
	}

	@Override
	Boolean read() {
		if (id > 0) {
			String param[] = { Long.toString(id) };
			cursor = helper.getWritableDatabase().rawQuery(
					"select * from Linia where _id=?", param);
			return cursor.getCount() > 0;
		}
		return false;
	}

	/* Aixó es tindria que canviar. Els preus han de canviar si l'usuari ha canviat el codi d'article */
	
	@Override
	public void haCanviat(Boolean result) {
		if (result == true) {
			descripcio.setText(article.getCursor().getString(1));
			double wpreu = 0.0;
			if (id == 0) { // Només di és alta
				Double wQuantitat = 0.0;
				String wLinia="",wFamilia="";
				TPreus preus = Utilitats.readPreus(act,helper, wsubjecte, wtarifa, article.getCursor().getString(0),wFamilia,wLinia,wQuantitat);
//					wpreu = Utilitats.getPreu();
				preu.setText(new DecimalFormat("####0.00").format(wpreu));
			}
			quantitat.requestFocus();

		} else {
			descripcio.setText("???");
			preu.setText(new DecimalFormat("####0.00").format(0));
		}

	}

}
