package sgb.orders;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DialogError extends Activity implements OnClickListener {
	Button cancel;
	Button gravar;
	Button proper;
	OrdersHelper helper;
	Comptadors ct;
	String PROGRAMA = "DialogError";
	Bundle extras;


	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		extras = getIntent().getExtras();
		String msg;
		if (extras != null) {
			msg = extras.getString("MsgError");
			if (msg != null) {
				TextView text = (TextView) findViewById(R.id.progress_text);
				text.setText(msg);
			}
				
		}
		
		setContentView(R.layout.dialogerror);
		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v == cancel)
			finish();
		
	}

	

}