package sgb.orders;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ZoomButton;

import java.util.ArrayList;


class Linia  {
	
	Linia(String article) {
		this.article = article;
	}

	public String getArticle() {
		return article;
	}

	public void setArticle(String article) {
		this.article = article;
	}

	public double getQuantitat() {
		return this.quantitat;
	}

	public void setQuantitat(double quantitat) {
		this.quantitat = quantitat;
	}

	public double getPreu() {
		return preu;
	}

	public void setPreu(double preu) {
		this.preu = preu;
	}

	public double getDte() {
		return dte;
	}

	public void setDte(double dte) {
		this.dte = dte;
	}

	String article;
	double quantitat;
	double preu;
	double dte;
	
	public boolean equals(Object obj) {
		Linia l = (Linia)obj;
		boolean rt = l.getArticle().equals(getArticle()); 
		return rt;
	}
}

public class ConsultaArticles extends LinearLayout {
	ListView listView;
	private DetallAdapter adapter=null;
	private OrdersHelper helper = null;
	private Cursor cursor;
	private ListView list;
	private String groupId;
	private String sql;
	private ArrayList<Linia> linies= MenuOrders.linies;
	private static Linia linKey = new Linia(""); // La farem servir per fer la cerca.

	
	
	class DetallAdapter extends CursorAdapter {
		DetallAdapter(Activity activity,Cursor c) {
			super(activity, c);

		}
	@Override
	public void bindView(View row, Context ctxt, Cursor c) {

		DetailsHolder holder = (DetailsHolder) row.getTag();
		holder.populateFrom(c,helper );
	}
		
		@Override
		public View newView(Context ctxt, Cursor c,
												 ViewGroup parent) {
			LayoutInflater inflater=((Activity) ctxt).getLayoutInflater();
			View row=inflater.inflate(R.layout.products_row, parent, false);
			DetailsHolder holder=new DetailsHolder(row);
			
			row.setTag(holder);
			
			return(row);
		}
	}

	static class DetailsHolder {
		private TextView name=null;
		private TextView group=null;
		private ImageView icon=null;
		private View row=null;
		private ZoomButton zin;
		private ZoomButton zon;
		private EditText quant; 
		private int position;
		private TextView posi;
		static private ConsultaArticles pare=null;
		static private OrdersHelper helper = null;
		static private Cursor cursor = null;
		static private DetallAdapter adapter=null;
		static private String sql=null;
		static private ArrayList<Linia>linies=null;
		private String article;
		
		DetailsHolder(View row) {
			this.row=row;
			
			name=(TextView)row.findViewById(R.id.product_list_name);
			icon=(ImageView)row.findViewById(R.id.subgroup_list_icon);
			zin =(ZoomButton)row.findViewById(R.id.zoomOut);
			zin.setLongClickable(true);
			quant=(EditText)row.findViewById(R.id.quant_text);
			quant.setRawInputType(InputType.TYPE_CLASS_NUMBER);
			quant.setInputType(InputType.TYPE_CLASS_NUMBER);
			posi = (TextView)row.findViewById(R.id.product_list_code);


			zin.setOnLongClickListener(new ZoomButton.OnLongClickListener() {
			   					@Override
					public boolean onLongClick(View arg0) {
//						zin.setVisibility(INVISIBLE);
						zin.setBackgroundResource(R.drawable.ball_red);
						Context context = arg0.getContext();
						Vibrator v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
						v.vibrate(300);
//						zon.setVisibility(VISIBLE);// TODO Auto-generated method stub
						return false;
					}
			    });
//			zon =(ZoomButton)row.findViewById(R.id.zoomOut);
//			zon.setVisibility(INVISIBLE);
//			zon.setLongClickable(true);

			
			zin.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					double  i = Double.parseDouble(quant.getText().toString());
					i++;
//					setLinia(article,i);
					quant.setText(Double.toString(i));
//					Toast.makeText(arg0.getContext(), "Selecció "+position+ " Posicio ", Toast.LENGTH_SHORT).show();
					
				} 				
			});
			
/*			zon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					double  i = Double.parseDouble(quant.getText().toString());
					i--;
					setLinia(article,i);
					quant.setText(Double.toString(i));
					adapter.notifyDataSetChanged ();
					
				} 				
			});
			zon.setOnLongClickListener(new ZoomButton.OnLongClickListener() {
			   					@Override
					public boolean onLongClick(View arg0) {
									zon.setVisibility(INVISIBLE);// TODO Auto-generated method stub
									Context context = arg0.getContext();
									Vibrator v = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
									v.vibrate(300);

									zin.setVisibility(VISIBLE);// TODO Auto-generated method stub
						return false;
					}
			    }); */

		}	
		
		
		Linia getArticle(String article) {
			linKey.setArticle(article);
			int s=pare.linies.size();
			int b = pare.linies.indexOf(ConsultaArticles.linKey);
			if (b == -1) {
				return null;
			}
			else
				return pare.linies.get(b);
		}
		
		void addLinia(String article,double valor) {
			Linia lin = getArticle(article);
			if (lin == null) {
				lin = new Linia(article);
				pare.linies.add(lin);
				}
			double val = lin.getQuantitat();
			lin.setQuantitat(val+valor);
		}
		
		void setLinia(String article,double valor) {
			Linia lin = getArticle(article);
			if (lin == null) {
				lin = new Linia(article);
				pare.linies.add(lin);
				}
			lin.setQuantitat(valor);
			
		}
		
		double getLinia(String article) {
			Linia lin = getArticle(article);
			if (lin != null) {
				return lin.getQuantitat();
			}
			else 
				return 0;
		}
		
		void populateFrom(Cursor c, OrdersHelper helper) { 
//			position = c.getPosition();
			String article = c.getString(c.getColumnIndex("_id"));
			posi.setText(article);
			this.article = article;
			name.setText(c.getString(c.getColumnIndex("descripcio")));

			/* 
			 * Cada cop que ens demanen un article comprovem quina quantitat hi ha demanada
			 * dintre de linies que representa el document actual. Tots els canvis sobre 
			 * linies es tindrien que reflexar sobre la BBDD SqlLite 
			 */

			quant.setText(Double.toString(getLinia(article)));

		} 
	}

	
	

	public ConsultaArticles(Activity app, AttributeSet attrs,OrdersHelper helper) {
		super(app, attrs);
		this.helper = helper;
		init(app);
	}

	public ConsultaArticles(Activity app,OrdersHelper helper,String sql) {
		super(app);
		this.helper = helper;
		this.sql = sql;
		init(app);
	}

	private void init(Activity activity) {

		String sql = "select article _id,descripcio from articles";
		if (this.sql.compareTo(" ") > 0)
			sql = this.sql;
		cursor=helper.execSQL(sql);
		DetailsHolder.helper=helper;
		DetailsHolder.cursor=cursor;
		DetailsHolder.adapter=adapter;
		DetailsHolder.sql = sql;
		DetailsHolder.pare = this;
		int s = cursor.getCount();
		adapter=new DetallAdapter(activity,cursor);

		LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.find_product, null);

		listView = (ListView) view.findViewById(R.id.list_find);
		
		EditText textMessage = (EditText)view.findViewById(R.id.list_find_text);
		textMessage.addTextChangedListener(
				new TextWatcher() {
					@Override
		        public void afterTextChanged(Editable s) {
		        	listView.refreshDrawableState();
		        }
		        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		        public void onTextChanged(CharSequence s, int start, int before, int count){}


		    }); 		
		

		listView.setAdapter(adapter);
		addView(view);
/*		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		addView(listView, params); */
	}

}
