package sgb.orders;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public abstract class TPlantilla extends LinearLayout implements
		OnClickListener , OnCanvia {

	// Botons predefinits

	protected String PROGRAMA="PLANTILLA";
	
	Button ico_add = null;
	Button ico_agenda = null;
	Button ico_back = null;
	Button ico_compass = null;
	Button ico_delete = null;
	Button ico_help = null;
	Button ico_save = null;
	Button ico_showlist = null;
	Button ico_sort = null;
	Button ico_lock = null;
	Button ico_unlock = null;

	final int ICO_ADD = 1;
	final int ICO_AGENDA = 2;
	final int ICO_BACK = 4;
	final int ICO_COMPASS = 8;
	final int ICO_DELETE = 16;
	final int ICO_HELP = 32;
	final int ICO_SAVE = 64;
	final int ICO_SHOWLIST = 128;
	final int ICO_SORT = 256;
	final int ICO_LOCK = 512;
	final int ICO_UNLOCK = 1024;

	ImageView rowIco;
	// Components Row Standard

/*	ImageView row_icon = null;
	CheckBox row_checkBox = null;
	TextView row_text1 = null;
	TextView row_text2 = null;
	TextView row_text3 = null;
	TextView row_text4 = null;
	TextView row_text5 = null; */

	final int ROW_ICON = 1;
	final int ROW_CHECKBOX = 2;
	final int ROW_TEXT1 = 4;
	final int ROW_TEXT2 = 8;
	final int ROW_TEXT3 = 16;
	final int ROW_TEXT4 = 32;
	final int ROW_TEXT5 = 64;
	final int ROW_ZOOM = 128;
	final int ROW_EDITNUM = 256;
	final int ROW_BOTOINC = 512;

	// ---------------------

	protected View view;

	protected Activity act;
	protected OrdersHelper helper = null;




	public void onClick(View v) {
		if (v == this.ico_back)
			act.finish();
		if (v == this.ico_sort) {
			
			
			
			new DialogPreferences(act,act).show();
			
/*			DialogRegal p = new DialogRegal(getContext());
			p.requestWindowFeature(Window.FEATURE_NO_TITLE);
			p.Omple();
			p.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			p.show(); */ 
			
			
		}
		if (v == this.ico_showlist)
        	act.getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, new
        			KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
			act.invalidateOptionsMenu();
	}

	void setButtonsProp(long vs, long id, Button v) {
		if (v != null)
			if ((vs & id) == 0)
				v.setVisibility(GONE);
			else
				v.setOnClickListener(this);

	}

	void setRowLayoutProps(View v, long ls, int sw, int rId) {
		if ((ls & sw) == 0) {
			View vs = v.findViewById(rId);
			if (vs != null) {
				vs.setVisibility(GONE);
			}	

		}

	}


	void setButtons() {
		long vs = getButtons();

		
		ico_add = (Button) view.findViewById(R.id.tplant_list_add);
		ico_agenda = (Button) view.findViewById(R.id.tplant_list_modificar);
		ico_back = (Button) view.findViewById(R.id.tplant_list_back);
		ico_compass = (Button) view.findViewById(R.id.tplant_list_compass);
		ico_delete = (Button) view.findViewById(R.id.tplant_list_delete);
//		ico_help = (Button) view.findViewById(R.id.tplant_list_help);
		ico_showlist = (Button) view.findViewById(R.id.tplant_list_llista);
		ico_save = (Button) view.findViewById(R.id.tplant_list_save);
		ico_sort = (Button) view.findViewById(R.id.tplant_list_sort);
		ico_lock = (Button) view.findViewById(R.id.tplant_lock);
		ico_unlock = (Button) view.findViewById(R.id.tplant_unlock);
		
		

		setButtonsProp(vs, ICO_ADD, ico_add);
		setButtonsProp(vs, ICO_AGENDA, ico_agenda);
		setButtonsProp(vs, ICO_BACK, ico_back);
		setButtonsProp(vs, ICO_COMPASS, ico_compass);
		setButtonsProp(vs, ICO_DELETE, ico_delete);
		setButtonsProp(vs, ICO_HELP, ico_help);
		setButtonsProp(vs, ICO_SAVE, ico_save);
		setButtonsProp(vs, ICO_SHOWLIST, ico_showlist);
		setButtonsProp(vs, ICO_SORT, ico_sort);
		setButtonsProp(vs, ICO_LOCK, ico_lock);
		setButtonsProp(vs, ICO_UNLOCK, ico_unlock);

		setOnClickListener(this);  // Per si toquen la pantalla fora dels botons
	}

	public TPlantilla(Activity act, OrdersHelper helper) {
		super(act);
		this.act = act;
		this.helper = helper;
	}

	abstract long getButtons();

	abstract long getRowsLayout();
	abstract void activate();
	public void destroy() { }
	public void onBackPressed() { act.finish(); }
	}



