package sgb.orders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class CampTxt {
	String camp;

	public String getCamp() {
		return camp;
	}

	int Posicio;

	CampTxt(String camp) {
		this.camp = camp;
	}
}

class Camps {
	String key;
	List<CampTxt> camps = new ArrayList<CampTxt>();

	Camps(String sql) {
		this.key = sql;
	}

	Camps(String sql, String txt) {
		this.key = sql;
		String valors[] = txt.trim().split("\\+");
		for (int i = 0; i < valors.length; i++)
			camps.add(new CampTxt(valors[i]));
	}

	public String getKey() {
		return key;
	}

	public void setKey(String sql) {
		this.key = sql;
	}

	public List<CampTxt> getCampsTxt() {
		return camps;
	}

}

class Taules extends Camps {
	Boolean esUpdate = false;
	String keyField = "";
	String value;
	
	Boolean headers = true;

	public Boolean getHeaders() {
		return headers;
	}

	public void setHeaders(Boolean headers) {
		this.headers = headers;
	}

	public String getKeyField() {
		return keyField;
	}

	public void setKeyField(String key) {
		this.keyField = key;
	}

	public Boolean getEsUpdate() {
		return esUpdate;
	}

	public void setEsUpdate(Boolean esUpdate) {
		this.esUpdate = esUpdate;
	}

	List<Camps> camps = new ArrayList<Camps>();

	List<Camps> getCamps() {
		return camps;
	}

	Taules(String sql, String txt) {
		super(sql);
		value = txt;

	}

	public List<CampTxt> findKey(String key) {
		for (Camps tb : camps) {
			String s = tb.getKey();
			if (tb.getKey().equalsIgnoreCase(key))
				return tb.getCampsTxt();

		}
		return null;
	}

	void addCamp(String sql, String txt) {
		camps.add(new Camps(sql, txt));

	}

	public String getValue() {
		return value;
	}
}

public class MapTables {
	Properties prop = new Properties();
	List<Taules> taules = new ArrayList<Taules>();


	public List<Taules> getTaules() {
		return taules;
	}

	String getField(String table, String cm) {
		String propName = table + "." + cm;
		String rt = prop.getProperty(propName);
		if (rt == null)
			return cm;
		else
			return rt;

	}

	String getTable(String cm, String def) {
		String rt = prop.getProperty(cm);
		if (prop != null && rt != null)
			return rt;
		else
			return def;
	}

	Boolean Load(String File) {
		InputStream input = null;

		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;
		Boolean headers=true;

		try {
			archivo = new File(File);
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);
			String linea;
			Taules act = null;
			while ((linea = br.readLine()) != null) {
				if (linea.length() > 3
						&& linea.substring(0, 1).equals("#") == false) {
					// Separem primer per si hi ha opcions <UPDATE>
					String op[] = linea.split("\\<");
					String st[] = op[0].split("=");
					String c1 = st[0];
					if (c1.substring(0, 1).equals("[")) {
						if (c1.indexOf("OPTIONS") > 0) {
							if (c1.indexOf("(NOHEADERS)") > 0)
								headers=false;
							if (c1.indexOf("(HEADERS)") > 0)
								headers=true;
						} else {
							String c2 = st[1];
							taules.add(act = new Taules(c1.substring(1), c2
									.substring(0, c2.length() - 1)));
							act.setHeaders(headers);
							if (op.length > 1) { // [SaldoClientes.txt=GRUPCLI]<UPDATE(grupcli>
								String opcions = op[1].substring(0,
										op[1].length() - 1);
								String key = "";
								String opkey[] = opcions.split("\\(");
								if (opkey.length > 1) {
									opcions = opkey[0];
									key = opkey[1].substring(0,
											opkey[1].length() - 1);
									;
								}
								act.setKeyField(key);
								act.setEsUpdate(opcions
										.equalsIgnoreCase("UPDATE"));
							}
						}

					} else {
						String c2 = st[1];
						act.addCamp(c1, c2);

					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta
			// una excepcion.
			try {
				if (null != fr) {
					fr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return null;

	}
}
