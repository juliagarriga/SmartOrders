package sgb.orders;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class Cataleg extends Activity {

	private OrdersHelper helper;
	private String sql;
	private Cursor cursor;
	private static final int REQUEST_CODE = 1;
	private Bitmap bitmap;
	private Zoom imageView;
	private Uri imageUri;
	private String codiArt;
	private File fileImg;
	MyPagerAdapter adapter;
	int paginaActual = 0;
	ViewPager myView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		fileImg = Utilitats.getWorkFolder(this, Utilitats.IMAGES);

		setContentView(R.layout.cataleg);
		helper = new OrdersHelper(this);
		Bundle extras = getIntent().getExtras();
		int position=0;
		if (extras != null) {
			sql = extras.getString("sql");
			position = extras.getInt("position");
			}
		cursor = helper.execSQL(sql);


		adapter = new MyPagerAdapter();
		myView = (ViewPager) findViewById(R.id.imatgeViewCataleg);
		myView.setAdapter(adapter);
		myView.setCurrentItem(position);
		myView.setOnPageChangeListener(new OnPageChangeListener() {

			public void onPageSelected(int page) {
				paginaActual = page;
			}

			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			public void onPageScrollStateChanged(int arg0) {

			}
		});

		// myPager.setCurrentItem(2);
	}

	public void farLeftButtonClick(View v) {
		Toast.makeText(this, "Far Left Button Clicked", Toast.LENGTH_SHORT)
				.show();

	}

	public void farRightButtonClick(View v) {
		int pos = myView.getCurrentItem();
		ViewGroup act = (ViewGroup)v.getRootView();
		TextView codi = (TextView) act.findViewById(R.id.codiArticle);
		imageView = (Zoom) act.findViewById(
				R.id.imatgeCataleg);
		if (codi != null) {
			Toast.makeText(this,
					"Activant Càmera Fotogràfica " + codi.getText().toString(),
					Toast.LENGTH_SHORT).show();
			String fileName = fileImg.getAbsolutePath() + "/"
					+ codi.getText().toString() + ".jpg";
			// create parameters for Intent with filename
			ContentValues values = new ContentValues();
			values.put(MediaStore.Images.Media.TITLE, fileName);
			values.put(MediaStore.Images.Media.DESCRIPTION,
					"Imatge capturada per la Càmera");
			imageUri = getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Uri uriSavedImage = Uri.fromFile(new File(fileName));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
			startActivityForResult(intent, REQUEST_CODE);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
			try { // We need to recyle unused bitmaps
				if (bitmap != null) {
					bitmap.recycle();
				}
				if (imageView != null) {
					InputStream stream = getContentResolver().openInputStream(
							data.getData());
					bitmap = BitmapFactory.decodeStream(stream);
					stream.close();
					imageView.setImageBitmap(bitmap);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private class MyPagerAdapter extends PagerAdapter {

		public int getCount() {
			return cursor.getCount();
		}

		
		public int calculateInSampleSize(
	            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {

	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);

	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }

	    return inSampleSize;
	}
		
		public Object instantiateItem(View collection, int position) {
			LayoutInflater inflater = (LayoutInflater) collection.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Zoom mImg = new Zoom(getBaseContext());
			View view = inflater.inflate(R.layout.farright, null);

			if (cursor.moveToPosition(position) == true) {
				imageView = (Zoom) view.findViewById(R.id.imatgeCataleg);
				TextView codi = (TextView) view.findViewById(R.id.codiArticle);
				TextView desc = (TextView) view
						.findViewById(R.id.descripcioArticle);
				codiArt = cursor.getString(cursor.getColumnIndex("article"));
				String bm = fileImg.getAbsolutePath() + "/" + codiArt + ".jpg";
				codi.setText(cursor.getString(cursor.getColumnIndex("article")));
				desc.setText(cursor.getString(cursor
						.getColumnIndex("descripcio")));

				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inPurgeable = true;
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(bm, options);
//				options.inSampleSize = 10;    
				
				int scrWidth  = getWindowManager().getDefaultDisplay().getWidth();
				int scrHeight = getWindowManager().getDefaultDisplay().getHeight();
				
				options.inSampleSize = calculateInSampleSize(options, scrWidth, scrHeight);
				options.inJustDecodeBounds = false;

				Bitmap mBitmap = BitmapFactory.decodeFile(bm, options);
				if (imageView != null && mBitmap != null)
					imageView.setImageBitmap(mBitmap);
				


 

				 imageView.setOnClickListener(new OnClickListener(){

				        @Override
			            public void onClick(View view) {
/*				        	String taula = "Linies"; 
							DialogLinia dlg = new DialogLinia(Cataleg.this.getApplicationContext(),
									(ExecTask)Cataleg.this.getApplicationContext(), taula, "", 0,
									helper,cursor);
//							dlg.getWindow().setBackgroundDrawable(new ColorDrawable(0));
							dlg.show(); */
				        	
				            }


				    });
				// mImg.setImageBitmap(mBitmap);
			}

			((ViewPager) collection).addView(view, 0);

			// ((ViewPager) collection).addView(mImg, 0);

			// return mImg;
			return view;

		}

		public Object instantiateItem3(View collection, int position) {

			LayoutInflater inflater = (LayoutInflater) collection.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			int resId = 0;
			switch (position) {
			case 0:
				break;
			}

			View view = inflater.inflate(resId, null);

			((ViewPager) collection).addView(view, 0);

			return view;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);

		}

		@Override
		public void finishUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == ((View) arg1);

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

	}

}