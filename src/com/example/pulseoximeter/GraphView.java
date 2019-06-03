package com.example.pulseoximeter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {
	
	private Bitmap mBitmap;
	private Paint mPaint = new Paint();
	private Canvas mCanvas = new Canvas();

	private float mSpeed = 1.5f;
	//private float mSpeed = 0.5f;
	//private float mSpeed = 1f;
	private float mLastX;
	private float mScale;
	private float mLastValue;
	private float mYOffset;
	private int mColor;
	private float mWidth;
	private float maxValue = 1024f;//**
	private String[] horlabels = new String[] { "0:00", " ", "0:02", " ", "0:04", " ", "0:06", " ", "0:08", " ", "0:10", " ", "0:12", " ", "0:14", " ", "0:16", " ", "0:18", " ", "0:20", " ", "0:22", " ", "0:24", " ", "0:26", " ", "0:28", " ", "0:30"};
	
	
	public GraphView(Context context) {
		super(context);
		init();
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mColor = Color.argb(255, 0, 0, 0);
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

	public void addDataPoint(float value) {
		
		final Paint paint = mPaint;
		final float newX = mLastX + mSpeed;
		final float v = mYOffset + value * mScale + 170;//**edit
		
		paint.setColor(mColor);
		paint.setTextSize(10);
		mCanvas.drawLine(mLastX, mLastValue, newX, v, paint);//วาดกราฟ
		mCanvas.drawLine(mLastX, mLastValue, newX, v, paint);
		mCanvas.drawLine(mLastX, mLastValue, newX, v, paint);
	    mCanvas.drawLine(mLastX, mLastValue-1, newX, v-1, paint);//วาดกราฟ
	    mCanvas.drawLine(mLastX, mLastValue-2, newX, v-2, paint);//วาดกราฟ
		mLastValue = v;
		mLastX += mSpeed;
		invalidate();
		
		
	}

	public void setMaxValue(float max) {
		maxValue = max;
		mScale = -(mYOffset * (1.15f / maxValue));//**edit
	}

	public void setSpeed(float speed) {
		
		mSpeed = speed;
		
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		mCanvas.setBitmap(mBitmap);
		mCanvas.drawColor(0xFF1874CD);
		mYOffset = h;
		mScale = -(mYOffset * (1.15f / maxValue));//6.4**edit
		//j=h;//300
		mWidth = w;
		mLastX = mWidth;
		super.onSizeChanged(w, h, oldw, oldh);
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		float border = 10;
	    float horstart = border * 2;
	    float height = getHeight();
	    float width = getWidth() - 1;
	    float graphheight = height - (2 * border);
	    float graphwidth = width - (2 * border);
	    //j = height;//300
	    
	    int s1 = 0;
	    int s2 = 0;
	    boolean n = false;
	    
		synchronized (this) {
			if (mBitmap != null) {
				if (mLastX >= mWidth) {
					mLastX = 20;
					final Canvas cavas = mCanvas;
					cavas.drawColor(0xFFFFFFFF);
					mPaint.setColor(0xFFFFFFFF);
					cavas.drawLine(20, mYOffset, mWidth, mYOffset, mPaint);
					
					//Main.num = 0;
				}
				canvas.drawBitmap(mBitmap, 0, 0, null);
				
			}
			
			//ตารางพื้นหลังแกน y
			//String[] verlabels = new String[] { "70", "56", "42", "28", "14", " " };
			//String[] verlabels = new String[] { "70", "60", "50", "40", "30" };
			String[] verlabels = new String[] { "", "", "", "", "" };
			int vers = verlabels.length - 1;//x
		    for (int i = 0; i < verlabels.length; i++) {//เส้นประ
		        mPaint.setColor(Color.DKGRAY);//สีตารางแนวตั้ง
		        float y = ((graphheight / vers) * i) + border;
		        
		        //canvas.drawLine(horstart, y, width, y, mPaint);
		        Paint p = new Paint();
		        p.setStyle(Paint.Style.FILL);
		        p.setStrokeWidth(2);
		        p.setColor(Color.BLACK);
		        p.setPathEffect(new DashPathEffect(new float[] {2, 10}, 10));
	            canvas.drawLine(horstart, y, width, y, p);
		        if(i == 4)
		        {
		        	canvas.drawLine(horstart, y, width, y, mPaint);//เส้นทึบ
		        }
		        mPaint.setColor(Color.BLACK);//สี text แกน y
		        canvas.drawText(verlabels[i], 10, y, mPaint);
		    }
		    
		  //ตารางพื้นหลังแกน x

		    int hors = horlabels.length - 1;//y
		    for (int i = 0; i < 31; i++) {
		        mPaint.setColor(Color.DKGRAY);//สีตารางแนวนอน
		        float x = ((graphwidth / hors) * i) + horstart;
		        //Main.Read2.setText("n: " + x);
		        //canvas.drawLine(x, height - border, x, border, mPaint);
		        
		        Paint p2 = new Paint();
		        p2.setStyle(Paint.Style.FILL);
		        p2.setStrokeWidth(2);
		        p2.setColor(Color.BLACK);
		        p2.setPathEffect(new DashPathEffect(new float[] {2, 10}, 10));
	            canvas.drawLine(x, height - border, x, border, p2);
		        
		        
		        
		        mPaint.setColor(Color.BLACK);//สี text แกน x
		        mPaint.setTextAlign(Align.CENTER);
		        
		    }
		}
	}
}
