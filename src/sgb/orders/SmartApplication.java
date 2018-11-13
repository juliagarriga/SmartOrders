package sgb.orders;

import android.app.Application;
import android.content.res.Configuration;


public class SmartApplication extends Application {
	private static SmartApplication singleton;
 
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
 

	public SmartApplication getInstance(){
		return singleton;
	}
	
	public String getProperty() {
		String value="";
		return value;
	}
	
	
	public void setProperty(String value) {
		
	}
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
	}
 
	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
 
	@Override
	public void onTerminate() {
		super.onTerminate();
	}
 
}