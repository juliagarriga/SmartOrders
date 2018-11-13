package sgb.orders;


import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
 
public class WebViewActivity extends Activity {
 
	private WebView webView;
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    Bundle bundle = getIntent().getExtras();

		setContentView(R.layout.webviewactivity);
 
		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setJavaScriptEnabled(true);
		String fl = bundle.getString("fitxer"); 
        if(fl != null)		
        	webView.loadUrl(fl);
 
	}
 
}