package sgb.orders;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import sgb.orders.Utilitats.TPreus;

public class DialogRegal extends Dialog implements OnClickListener {
	String PROGRAMA = "DialogRegal";
	String taula;
	Double preuFinal;

	String idLinia;
	Button cancel;
	Button gravar;
	Activity act;

	String wtarifa;
	String wsubjecte;
	String wdescripcio;
	String wdocument;
	String wLinia;
	long id;

	SGEdit quantitat, stock, preu, preutarifa, tipdte, dte, descripcio, total,
			article, notes;

	TPreus tmpPreus = new TPreus();

	OnEditorActionListener onEditor = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			// if (event != null && event.getKeyCode() == event.KEYCODE_ENTER)
			if (actionId == EditorInfo.IME_ACTION_DONE)
				gravar.requestFocus();
			return true;
		}
	};


	public DialogRegal(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.dialoglinia);

//		Omple();

	}
	
	public void Omple() {
		this.setContentView(R.layout.dialogregal);
	}


	
	
	@Override
	public void onClick(View v) {
		if (v == gravar) {
			// if (gravar.requestFocus())
		}
	}
}