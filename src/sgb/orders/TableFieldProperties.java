package sgb.orders;

public class TableFieldProperties {
	int id;
	String name; // Nom del camp dintre de la Taula
	String type;
	int notnull;
	int pk;
	String dfltValue;
	
	TableProperties tb;

	TableFieldProperties(TableProperties tb) {
		this.tb = tb;

	}

	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getNotnull() {
		return notnull;
	}

	public void setNotnull(int notnull) {
		this.notnull = notnull;
	}

	public int getPk() {
		return pk;
	}

	public void setPk(int pk) {
		this.pk = pk;
	}

	public String getDfltValue() {
		return dfltValue;
	}

	public void setDfltValue(String dfltValue) {
		this.dfltValue = dfltValue;
	}

	public TableProperties getTb() {
		return tb;
	}

	public void setTb(TableProperties tb) {
		this.tb = tb;
	}



}
