package sgb.orders;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;

import com.csvreader.CsvReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

interface NotifyCsv {
	void Avisa(int pos);
};

public class Csv2Sqlite {
	private OrdersHelper helper = null;
	private NotifyCsv ntf;
	private Taules mapTaula;

	Csv2Sqlite(Taules taula) {
		this.mapTaula = taula;
	}

	int ImportCount(String file, Activity act) {
		int i = 0;
		try {
			CsvReader reader = new CsvReader(new InputStreamReader(
					new FileInputStream(Utilitats.getWorkFolder(act,
							Utilitats.IMPORT).getAbsolutePath()
							+ "/" + file), Charset.forName("UTF-8")));

			reader.setDelimiter(';');

			while (reader.readRecord()) {
				i++;
				;
			}
			reader.close();

		} catch (IOException e) {
		}
		return i;

	}

	void ImportFile(String file, String tab, OrdersHelper helper, Activity act,
			NotifyCsv ntf) {
		this.ntf = ntf;
		try {
			ContentValues cv = new ContentValues();
			long result = 0;

			CsvReader reader;
			reader = new CsvReader(new InputStreamReader(new FileInputStream(
					Utilitats.getWorkFolder(act, Utilitats.IMPORT)
							.getAbsolutePath() + "/" + file),
					Charset.forName("ISO-8859-1")));
			reader.setDelimiter(';');
			boolean head=false;
			int numCamps = 0;
			
			Boolean getHeaders=mapTaula.getHeaders();
			if (getHeaders) {
				head = reader.readHeaders();
				numCamps = reader.getHeaderCount();
			}
			int pos = 0;

			String taula = mapTaula.getValue();
			Boolean esUpdate = mapTaula.getEsUpdate();

			List<Integer> types = Utilitats.getColumnTypes(helper, taula);
			String sql = "SELECT * FROM '" + taula + "' LIMIT 0";
			Cursor cur = helper.getWritableDatabase().rawQuery(sql, null);

			int registres = 0, gravats = 0;

			while (reader.readRecord() ) {
				String sts = reader.getRawRecord();
				String val1 = reader.get(1);
				String val2 = reader.get(2);

				List<String> Types = new ArrayList<String>();

				ContentValues cnt = new ContentValues();

				int error = 0;
				if (cur != null) {
					int num = cur.getColumnCount();
					for (int i = 0; i < num; ++i) {
						String fieldName = cur.getColumnName(i);
						List<CampTxt> cm = mapTaula.findKey(fieldName);
						if (cm != null)
							for (CampTxt s : cm) {

								// Descartem les constants Numeriques & Cadenes
								// (
								// comencen per "
								String csvFieldName = s.getCamp();
								Boolean esConstant = false;
								Boolean esIndex = csvFieldName != null
										&& (csvFieldName.charAt(0) >= '0' && csvFieldName
												.charAt(0) <= '9');
								if (csvFieldName != null
										&& csvFieldName.charAt(0) != '"'
										&& !esIndex) {
									int idx = 0;
									if ((idx = reader.getIndex(csvFieldName)) < 0) {
										Utilitats.Toast(act, file + ".("
												+ csvFieldName + "->"
												+ fieldName + ")"
												+ " : No es troba el Camp");
										error++;
									}
								}
							}
					}
				}

				if (error == 0 && cur != null) {
					int num = cur.getColumnCount();
					for (int i = 0; i < num; ++i) {
						String fieldName = cur.getColumnName(i);
						List<CampTxt> cm = mapTaula.findKey(fieldName);
						StringBuilder valCamp = new StringBuilder();
						int numCamp = 0;
						if (cm != null) {
							for (CampTxt s : cm) {
								String csvFieldName = s.getCamp();

								if (csvFieldName == null)
									; // Utilitats.Toast(act,file+"."+fieldName+
										// " : No es troba el Camp");
								else {
									int idx = 0;
									String valor = "";
									Boolean esConstant = csvFieldName.charAt(0) == '"';
									Boolean esIndexCamp = (csvFieldName
											.charAt(0) >= '0' && csvFieldName
											.charAt(0) <= '9');
									if (esConstant)
										valor = Utilitats
												.eliminaCometes(csvFieldName);
									else {
										if (esIndexCamp)
											idx = Integer
													.parseInt(csvFieldName);
										else if ((idx = reader
												.getIndex(csvFieldName)) < 0)
											Utilitats.Toast(act, file + ".("
													+ csvFieldName + "->"
													+ fieldName + ")"
													+ " : No es troba el Camp");
										if (idx >= 0)
											valor = reader.get(idx);
									}
									if (types.get(i) == Utilitats.SQL_REAL)
										valor = valor.replace(',', '.');
									valCamp.append(Utilitats.rtrim(valor));
									numCamp++;
									if (numCamp < cm.size())
										valCamp.append("&"); // Separador de
																// camps clau

								}

							}
							cnt.put(fieldName,
									Utilitats.rtrim(valCamp.toString()));							
						}
					}
				}
				registres++;
				if (cnt.size() > 0) {
					if (esUpdate)
						result = helper.update(mapTaula.getValue(),
								mapTaula.getKeyField(), cnt);
					else
						result = helper.replace(mapTaula.getValue(),
								mapTaula.getValue(), cnt);
					gravats++;
				}
				ntf.Avisa(++pos);

			}
//			Utilitats.Toast(act, tab + " Llegits: " + registres + " Gravats: "
//					+ gravats);

			reader.close();
		} catch (IOException e) {
			Errors.appendLog(act, Errors.ERROR, "PROGRAMA",
					"Error Reader " + e.getMessage());

		}
	}
}
