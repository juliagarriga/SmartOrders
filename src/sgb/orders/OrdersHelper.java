package sgb.orders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class OrdersHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "andorders.db";
	private static final int SCHEMA_VERSION = 1;

	public OrdersHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL("CREATE TABLE Contactes  ( _id INTEGER PRIMARY KEY AUTOINCREMENT,subjecte TEXT,tipus TEXT,descripcio TEXT,telefon1 TEXT,telefon2 TEXT,fax TEXT,mail TEXT,url TEXT);");
		db.execSQL("CREATE TABLE Comentaris  ( codi TEXT PRIMARY KEY,comentari TEXT);");
		db.execSQL("CREATE TABLE Taules      ( clau TEXT PRIMARY KEY,claugest TEXT,taula TEXT,descripcio TEXT,numeric1 REAL,numeric2 REAL);");
		db.execSQL("CREATE TABLE TaulesSis   ( clau TEXT PRIMARY KEY,taula TEXT,descripcio TEXT,numeric1 REAL,numeric2 REAL);");
		db.execSQL("CREATE TABLE Families    ( familia TEXT PRIMARY KEY,descripcio TEXT);");
		db.execSQL("CREATE TABLE Linies      ( linia TEXT PRIMARY KEY,descripcio TEXT);");
		db.execSQL("CREATE TABLE Rutes       ( ruta TEXT PRIMARY KEY,ordre TEXT, descripcio TEXT);");

		db.execSQL("CREATE TABLE Efectes     ( 	numdoc INTEGER PRIMARY KEY ,subjecte TEXT,serie TEXT,factura INTEGER,data_venciment TEXT,data_fac TEXT,import REAL,saldo REAL,totfac REAL);");
		db.execSQL("CREATE TABLE Tarifes     ( _id INTEGER PRIMARY KEY ,article TEXT,tarifa TEXT,preu REAL);");
		db.execSQL("CREATE TABLE PreusEsp    ( _id INTEGER PRIMARY KEY AUTOINCREMENT ,subjecte TEXT,tipus TEXT,ordre INTEGER ,objecte TEXT,tipdte TEXT,dte REAL,datatini TEXT,datafi TEXT,minim TEXT,QRegal REAL,ARegal TEXT, MRegal TEXT, servit REAL);");
		db.execSQL("CREATE TABLE Locations   ( _id INTEGER PRIMARY KEY AUTOINCREMENT,user TEXT,session TEXT,datetime default (datetime(current_timestamp)),longitude LONG,latitude LONG);");
		db.execSQL("CREATE TABLE PreComanda  ( _id INTEGER PRIMARY KEY AUTOINCREMENT,subjecte TEXT,article TEXT,quantitat REAL,preu REAL, familia TEXT,dte REAL,servit REAL, data TEXT);");
		db.execSQL("CREATE UNIQUE INDEX i1 ON PreComanda(subjecte,article);");
		db.execSQL("CREATE TABLE Comptadors  ( _id INTEGER PRIMARY KEY ,acx TEXT,c_acx LONG,cca TEXT,c_cca LONG);");

		db.execSQL("CREATE TABLE Articles    ( article TEXT PRIMARY KEY , familia TEXT, linia TEXT, descripcio TEXT,servit  REAL,stock REAL,format TEXT,tarifa1 REAL"
				 +",tarifa2 REAL,tarifa3 REAL,tarifa4 REAL,tarifa5 REAL,tarifa6 REAL,tarifa7 REAL,tarifa8 REAL,tarifa9 REAL,tarifa10 REAL,iva TEXT"
					+");");

		db.execSQL("CREATE TABLE GrupCli     "
		+ "	( grupcli TEXT PRIMARY KEY ,nom TEXT,"
		+ "saldo REAL, risc REAL"
		+");");

		
		db.execSQL("CREATE TABLE Clients     "
		+ "	( subjecte TEXT PRIMARY KEY ,"
		+ "nif TEXT, nomfiscal TEXT,nom TEXT,mail TEXT,posicio_x REAL,posicio_y REAL,telf1 TEXT,mobil TEXT,observacions TEXT"
		+ ",telf3 TEXT,tarifa TEXT,adreca TEXT,codipostal TEXT, poblacio TEXT,provincia TEXT,telf2 TEXT, contacte TEXT"
		+ ",dtegrup REAL,dtecomercial REAL, dtepp REAL,repres TEXT,grup TEXT,fpag TEXT,fax TEXT,state TEXT,ruta TEXT,tipus_factura TEXT,banc TEXT,agencia TEXT,compte TEXT,comandespendents INTEGER DEFAULT 0"
		+");");

		
		db.execSQL("CREATE TABLE CliRuta     ( ruta TEXT, Subjecte TEXT,  ordre INTEGER , PRIMARY KEY(ruta,subjecte));");
		db.execSQL("CREATE TABLE Cap         (_id LONG PRIMARY KEY, client TEXT, data TEXT, entrega TEXT,state TEXT,quant REAL,value REAL,notes TEXT, dtegrup REAL,dtecomercial REAL,comentari TEXT,entrega_mati TEXT,recullen TEXT,dtepp REAL );");
		db.execSQL("CREATE TABLE Linia       (_id INTEGER PRIMARY KEY AUTOINCREMENT, docum LONG,article TEXT,nom TEXT,quant REAL,preu REAL,preunet REAL, tipdte TEXT	,dte REAL,notes TEXT,state TEXT," +
				"codi_obs TEXT,article_regal TEXT,quantitat_regal REAL,preu_regal REAL,canviPreu INTEGER );");
		db.execSQL("CREATE TABLE Filtres     (_id INTEGER PRIMARY KEY , descripcio TEXT,sql TEXT);");

		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// db.execSQL("CREATE TABLE Rutes       (_id INTEGER PRIMARY KEY AUTOINCREMENT, ruta TEXT, ordre TEXT, Descripcio TEXT);");
	}


	public Cursor execSQL(String sql) {
		return (getReadableDatabase().rawQuery(sql, null));
	}

	public Cursor execSQL(String sql, String id) {
		String[] args = { id };
		return (getReadableDatabase().rawQuery(sql, args));
	}

	public long insert(String table, String field, ContentValues cv) {
		return getWritableDatabase().insert(table, field, cv);

	}

	public long insertOrThrow(String table, String field, ContentValues cv) {
		return getWritableDatabase().insertOrThrow(table, field, cv);

	}

	public long replace(String table, String field, ContentValues cv) {
		return getWritableDatabase().replace(table, field, cv);

	}


			
	public long update(String table, String key, ContentValues cv) {
		if (cv.containsKey(key)) {
			String vl = cv.get(key).toString();
			String[] args = { vl };
			cv.remove(key); // Ja el gestiona ell mateix
			return getWritableDatabase().update(table, cv, key + "=?", args);
		} else
			return -100;
	}

	public boolean find(String table, String key, String value) {
		String sql = "select " + key + " from " + table + " where " + key
				+ "='" + value + "? ";
		String p[] = { value };
		Cursor c = getWritableDatabase().rawQuery(sql, p);
		return c.getCount() > 0;
	}

	public String getName(Cursor c) {
		return (c.getString(1));
	}

	public String getAddress(Cursor c) {
		return (c.getString(2));
	}

	public String getType(Cursor c) {
		return (c.getString(3));
	}

	public String getNotes(Cursor c) {
		return (c.getString(4));
	}

	public void dropDatabase() {

	}
}