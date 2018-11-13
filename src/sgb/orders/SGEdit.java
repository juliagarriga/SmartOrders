package sgb.orders;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import sgb.orders.RsEditText.Listener;

/***************************
 * 
 * 
 * @author Salvador
 * 
 * 
 *         Gestió del Temporitzador
 * 
 * 
 *         Quan s'activa el timer i es supera el temps sense modificar el camp
 *         que te el focus, es dispara l'event validate, el mateix que es
 *         dispara quan el camp perd el focus. EL problema rau en dos punts :
 * 
 *         Si toques el boto Gravar (o Fet del SoftKey) no perd el focus i per
 *         tant no es dispara l'event. L'android només distingueix l'event
 *         TextChange i no te en compte si el text s'ha canviat desde teclat o
 *         per programa.
 * 
 */




public class SGEdit extends EditText implements
/* KeyListener, */OnTouchListener, OnFocusChangeListener

{
	// private Rect mRect;
	// private Paint mPaint;
	private Drawable xD;
	private Listener listener;

	private OrdersHelper helper;
	private String sqlCommand;
	private OnCanvia resultCall;
	private Cursor cursor;
	private Boolean validate;
	private DatePickerDialog dpk;
	private Timer timer = null;
	private long mseg = 0;
	private long timerCont = 0;
	private Boolean swTimer = false;
	private OnTimerEvent onTimerEvent = null;
	private OnValidateEvent onValidateEvent = null;
	private Boolean fireOnTextChanged = true;
    private Boolean rt;  


	public void setTextNotFocus(CharSequence txt) {
		if (!this.isFocused())
			this.setText(txt);
	}

	public void setFireOnTextChanged(Boolean enabled) {
		this.fireOnTextChanged = enabled;
	}

	public Boolean getFireOnTextChanged() {
		return fireOnTextChanged;
	}

	double toDouble() {
		double tmp = 0;

		try {
			tmp = Double.parseDouble(getText().toString());

		} catch (NumberFormatException e) {
		}
		return tmp;
	}

	/********************************************************************
	 * 
	 * funció validar - comprova que el contingut del camp és correcte.
	 * 
	 * Aquesta funció es pot cridar quan surt el focus, per l'activació d'un
	 * timer .setTimer() ... o de forma externa.
	 * 
	 *******************************************************************/

	public Boolean validar() {
		if (onValidateEvent != null && this.getFireOnTextChanged() == true) {
		        return onValidateEvent.validate(SGEdit.this);
		}
		return true;
	}

	void setTimer(long amseg) {
		mseg = amseg;
		timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			public void run() {
				if (swTimer == true) {
					timerCont++;
					if (timerCont > mseg) {
						swTimer = false;
						validar();
					}
					/*
					 * if (onTimerEvent != null && timerCont > mseg) {
					 * onTimerEvent.runTimerTask(timerCont); timerCont = 0;
					 * swTimer = false; }
					 */
				}
			}
		};

		timer.schedule(timerTask, 500, 500);
	}

	void setOnTimerEvent(OnTimerEvent event) {
		onTimerEvent = event;
	}

	void setOnValidateEvent(OnValidateEvent event) {
		onValidateEvent = event;
	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Calendar c = Calendar.getInstance();
			c.set(year, monthOfYear, dayOfMonth);
			Date fecha = c.getTime();
			SGEdit.this.setText(dateFormat.format(fecha));
		}
	};

	/*
	 * Stack Overflow
	 * 
	 * @Override public Editable getText() { Editable txt = super.getText();
	 * String st = txt.toString(); st.replaceAll(".",","); txt.clear();
	 * txt.insert(0,st); Editable ne =
	 * Editable.Factory.getInstance().newEditable(txt); return ne; }
	 */

	Boolean getValidate() {
		return validate;

	}

	void setValidate(Boolean value) {
		validate = value;
	}

	Cursor getCursor() {
		return cursor;
	}

	public void setSQLValidation(OrdersHelper helper, String sqlCommand,
			OnCanvia resultCall) {
		this.helper = helper;
		this.sqlCommand = sqlCommand;
		this.resultCall = resultCall;
	}

	public int getInputType() {
		return super.getInputType();
	}

	public SGEdit(Context context, AttributeSet attrs) {
		super(context, attrs);

		InputFilter filter = new InputFilter() {
			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {
				if ((SGEdit.this.getInputType() & InputType.TYPE_CLASS_NUMBER) != 0) {
					if (start == 0 && end > 1 && dstart == 0 && dend == 0) {
						String rt = source.toString();
						String s = rt.replace(",", ".");
						return s;
					}
				}
				if (SGEdit.this.isFocused() && end - start == 1)
					for (int i = start; i < end; i++) {
						// if (Character.isLetterOrDigit(source.charAt(i))) {
						// if (source.charAt(i) == '.')
						// return ",";
						// }
					}
				return null;
			}
		};

		this.setFilters(new InputFilter[] { filter });
		setOnTouchListener(this);
		setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					// do something here
					return true;
				}
				return false;
			}
		});
		setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus == true) {
				} else {
					swTimer = false;
					if (validar() == false) {
						requestFocus();
					}
				}
			}
		});

		setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| (event != null && (event.getAction() == KeyEvent.ACTION_DOWN && event
								.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
					lookUp();
					return true;
				}
				return false;
			}
		});

		addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (isFocused()) {
					setClearIconVisible(s.length() > 0);
				}

				
				/*
				 * No hi ha manera de capturar el SoftKey. Llavors per
				 * distinguir si s'esta modificant un camp desde el teclat o no
				 * pot ser comprovant si te el focus... ?? No se...
				 */

				if (isFocused() && SGEdit.this.getFireOnTextChanged() == true) {
					timerCont = 0;
					swTimer = true;
				}

				setValidate(false);

			}
		});

		// Creates a Rect and a Paint object, and sets the style and color of
		// the Paint object.
		/*
		 * mRect = new Rect(); mPaint = new Paint();
		 * mPaint.setStyle(Paint.Style.STROKE); mPaint.setColor(0x800000FF);
		 */
		init();
	}

	

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener f) {
		this.f = f;
	}

	private OnTouchListener l;
	private OnFocusChangeListener f;



	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			setClearIconVisible(getText().length() > 0);
		} else {
			setClearIconVisible(false);
		}
		if (f != null) {
			f.onFocusChange(v, hasFocus);
		}
	}

	
	private void init() {
		xD = getCompoundDrawables()[2];
		if (xD == null) {
			xD = getResources()
					.getDrawable(android.R.drawable.btn_dialog);
		}
		xD.setBounds(0, 0, xD.getIntrinsicWidth(), xD.getIntrinsicHeight());
		setClearIconVisible(false);
		super.setOnTouchListener(this);
		super.setOnFocusChangeListener(this);
		addTextChangedListener(new TextWatcherAdapter());
	}

	protected void setClearIconVisible(boolean visible) {
		Drawable x = visible ? xD : null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return super.dispatchKeyEvent(e);
	};

	/*
	 * 
	 * 
	 * Aquests events NO FUNCIONEN almenys en SOFTKEYBOARD
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @Override public void clearMetaKeyState(View view, Editable content, int
	 * states) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * 
	 * @Override public boolean onKeyDown(View view, Editable text, int keyCode,
	 * KeyEvent event) {
	 * 
	 * timerCont = 0; swTimer = true;
	 * 
	 * switch (keyCode) { case KeyEvent.KEYCODE_ENTER: lookUp(); } return
	 * super.onKeyDown(keyCode, event); }
	 * 
	 * @Override public boolean onKeyOther(View view, Editable text, KeyEvent
	 * event) { timerCont = 0; swTimer = true; return false; }
	 * 
	 * @Override public boolean onKeyUp(View view, Editable text, int keyCode,
	 * KeyEvent event) { timerCont = 0; swTimer = true; return
	 * super.onKeyUp(keyCode, event); }
	 */

	Boolean lookUp() {
		if (sqlCommand == null)
			return true;
		String param[] = { this.getText().toString() };
		cursor = helper.getReadableDatabase().rawQuery(sqlCommand, param);
		setValidate(cursor.getCount() > 0);
		if (getValidate() == false) {
			Toast.makeText(getContext(),
					" Codi " + this.getText() + " inexistent",
					Toast.LENGTH_SHORT).show();
		}
		if (resultCall != null)
			if (cursor.moveToNext() == true) {
				resultCall.haCanviat(true);
				setBackgroundColor(Color.GREEN);
				selectAll();

			} else {
				resultCall.haCanviat(false);
				setBackgroundColor(Color.RED);

			}
		return getValidate();

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int i = getCompoundPaddingRight();
		int i1 = getWidth();
		float i2 = event.getX();
		if (getCompoundDrawables()[2] != null) {
			boolean tappedX = event.getX() > (getWidth() - getPaddingRight() - xD
					.getIntrinsicWidth());
			if (tappedX) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					setText("");
					if (listener != null) {
						listener.didClearText();
					}
				}
				return true;
			}
		}
		if (l != null) {
			return l.onTouch(v, event);
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			if ((this.getInputType() & InputType.TYPE_DATETIME_VARIATION_DATE) != 0) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
				Date dt = new Date();
				try {
					String wdt = getText().toString();
					dt = dateFormat.parse(wdt);
				} catch (ParseException e) {
					Toast.makeText(getContext(),
							" Data Incorrecta " + this.getText(),
							Toast.LENGTH_SHORT).show();
				}
				Calendar c = Calendar.getInstance();
				c.setTime(dt);
				dpk = new DatePickerDialog(getContext(), mDateSetListener,
						c.get(Calendar.YEAR), c.get(Calendar.MONTH),
						c.get(Calendar.DAY_OF_MONTH));

				dpk.show();

			} else if (event.getAction() == MotionEvent.ACTION_UP
					&& event.getX() >= getWidth() - getCompoundPaddingRight()) {

				lookUp();
			}
		}
		return false;
	}

	public void close() {
		if (timer != null) {
			timer.purge();
		}
	}

}

/**
 * This is called to draw the LinedEditText object
 * 
 * @param canvas
 *            The canvas on which the background is drawn.
 * @Override protected void onDraw(Canvas canvas) {
 * 
 *           // Gets the number of lines of text in the View. int count =
 *           getLineCount();
 * 
 *           // Gets the global Rect and Paint objects Rect r = mRect; Paint
 *           paint = mPaint;
 * 
 *           for (int i = 0; i < count; i++) {
 * 
 *           // Gets the baseline coordinates for the current line of text int
 *           baseline = getLineBounds(i, r);
 * 
 *           canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1,
 *           paint); }
 * 
 *           // Finishes up by calling the parent method super.onDraw(canvas); }
 *           }
 */
