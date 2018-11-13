	package sgb.orders;

    import android.app.Activity;
    import android.app.Dialog;
    import android.content.Context;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.view.View;
    import android.view.View.OnClickListener;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.LinearLayout;

public class DialogPreferences extends Dialog implements OnClickListener {
	Button cancel;
	Button gravar;
	OrdersHelper helper;
	Comptadors ct;
	String 		PROGRAMA="DialogPreferences";


	EditText user, password,server,compta,serie,amaga,perFamilia,carpeta;

	public DialogPreferences(Context context,Activity act) {
		super(context);
		
		helper = new OrdersHelper(getContext());
		ct = new Comptadors(helper);

		this.setContentView(R.layout.dialogpreferences);
		
		long document = ct.getComptador();
		final LinearLayout  ly = (LinearLayout) findViewById(R.id.preferences_layout);

		ly.setVisibility(View.INVISIBLE);
		
		final EditText access = (EditText) findViewById(R.id.preferences_access);
  	    access.addTextChangedListener(new TextWatcher() {
			  
			   public void afterTextChanged(Editable s) {
			   }
			 
			   public void beforeTextChanged(CharSequence s, int start, 
			     int count, int after) {
			   }
			 
			   public void onTextChanged(CharSequence s, int start, 
			     int before, int count) {
				   String ss = access.getText().toString();
				   if (ss.equals("reset"))
							ly.setVisibility(View.VISIBLE);
				   		
			   }
			  });
		
		compta = (EditText) findViewById(R.id.comptador); 
		carpeta = (EditText) findViewById(R.id.carpeta); 
		user = (EditText) findViewById(R.id.usuari_ftp);
		password = (EditText) findViewById(R.id.password_ftp);
		server = (EditText) findViewById(R.id.server_ftp);
		serie = (EditText) findViewById(R.id.serie);
		amaga =  (EditText) findViewById(R.id.amaga);
		perFamilia =  (EditText) findViewById(R.id.perFamilia);
		EditText mac  =  (EditText) findViewById(R.id.mac);
		
		mac.setText(Utilitats.getMac(act));

		Prefs prefs = Prefs.getInstance(getContext());
		server.setText(prefs.getString("ftpServer", ""));
		user.setText(prefs.getString("ftpUser", ""));
		carpeta.setText(prefs.getString("ftpFolder", ""));
		password.setText(prefs.getString("ftpPwd", ""));
		compta.setText(Long.toString(document));
		amaga.setText(prefs.getString("amagar", ""));
		perFamilia.setText(prefs.getString("perFamilia", ""));
		serie.setText(prefs.getString("serie", "CCA"));
		prefs.close();

		gravar = (Button) findViewById(R.id.gravar);
		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
		gravar.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == gravar) {
			Prefs prefs = Prefs.getInstance(getContext());
			prefs.setString("ftpFolder", carpeta.getText().toString());
			prefs.setString("ftpUser", user.getText().toString());
			prefs.setString("ftpPwd", password.getText().toString());
			prefs.setString("ftpServer", server.getText().toString());
			ct.setComptador(Long.parseLong(compta.getText().toString()));
			prefs.setString("serie", serie.getText().toString());
			prefs.setString("amagar", amaga.getText().toString());
			prefs.setString("perFamilia", perFamilia.getText().toString());
			
			prefs.close();

		}
		dismiss();
	}
}