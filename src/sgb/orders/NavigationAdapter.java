package sgb.orders;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NavigationAdapter extends BaseAdapter {
	private Activity activity;
	ArrayList<Item_objct> arrayitms;
	static Camera camera;
	static Boolean activa = false;

	public NavigationAdapter(Activity activity, ArrayList<Item_objct> listarry) {
		super();
		this.activity = activity;
		this.arrayitms = listarry;
	}

	// Retorna objeto Item_objct del array list
	@Override
	public Object getItem(int position) {
		return arrayitms.get(position);
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return arrayitms.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// Declaramos clase estatica la cual representa a la fila
	public static class Fila {
		TextView titulo_itm;
		ImageView icono;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Fila view;
		LayoutInflater inflator = activity.getLayoutInflater();
		if (convertView == null) {
			view = new Fila();
			// Creo objeto item y lo obtengo del array
			Item_objct itm = arrayitms.get(position);
			convertView = inflator.inflate(R.layout.itm, null);
			// Titulo
			view.titulo_itm = (TextView) convertView
					.findViewById(R.id.title_item);
			// Seteo en el campo titulo el nombre correspondiente obtenido del
			// objeto
			view.titulo_itm.setText(itm.getTitulo());
			// Icono
			view.icono = (ImageView) convertView.findViewById(R.id.icon);
			// Seteo el icono
			view.icono.setImageResource(itm.getIcono());
			view.titulo_itm.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// Utilitats.Toast(activity, ((TextView) v).getText()
					// .toString(), R.raw.insert);

					Boolean click = false;
					String Titol = ((TextView) v).getText().toString();

					if (Titol.equalsIgnoreCase("CLIENTS")) {
						Intent intent = new Intent(activity.getBaseContext(),
								ExecTask.class);

						intent.putExtra("parametre1", "");
						intent.putExtra("programa", "LlistaClientsRuta");
						activity.startActivity(intent);
						click = true;

					} else if (Titol.equalsIgnoreCase("ARTICLES")) {

						Intent intent = new Intent(activity.getBaseContext(),
								ExecTask.class);
						intent.putExtra("parametre1", 0);
						intent.putExtra("parametre2", 0);
						intent.putExtra("programa", "Linia");
						activity.startActivity(intent);
						click = true;
					}

					else if (Titol.startsWith("Exporta")) {
						click = true;
						Thread proc = new Thread(new Runnable() {
							public void run() {
								if (Utilitats
										.DescarregaFitxerSeguretat(activity) == true) {
									if (Utilitats.ComprovaSeguretat(activity) == false)
										Utilitats
												.Toast(activity,
														"Aplicació en modo demo. No es pot enviar",
														true);
									else {
										new ExportCsv(activity, "N").start();
									}
								}
							}
						}

						);
						proc.start();

					}

					else if (Titol.startsWith("Importa")) {
						click = true;

						Intent it = new Intent(activity.getBaseContext(),
								ImpMaps.class);
						it.putExtra("PerSD", "N");
						activity.startActivity(it);

					}

					else if (Titol.startsWith("Backup")) {
						Utilitats.CopiaBBDD(activity);
					}
					else if (Titol.startsWith("Configu")) {
						new DialogPreferences(activity, activity).show();

					}
					else if (Titol.startsWith("Inicialitza")) {
						Utilitats.InicialitzaBaseDeDades(activity);

					}
					if (click) {
						DrawerLayout NavDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
						if (NavDrawerLayout != null)
							NavDrawerLayout.closeDrawers();
						}
					
					return false;

				}
			});
			convertView.setTag(view);
		} else {
			view = (Fila) convertView.getTag();
		}

		return convertView;
	}
}
