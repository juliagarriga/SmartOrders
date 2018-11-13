package sgb.orders;

import java.lang.reflect.Method;

public class Device {
	
	static String getSystemInfo() {
	     String serial = null; 

	     try {
	         Class<?> c = Class.forName("android.os.SystemProperties");
	         Method get = c.getMethod("get", String.class);
	         serial = (String) get.invoke(c, "ro.serialno");
	         return serial;
	         
	     } catch (Exception ignored) {
	     }
		return serial;

	}

}
