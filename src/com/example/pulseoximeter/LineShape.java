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

public class LineShape extends View{
	
	private Bitmap mBitmap;
	private Paint mPaint = new Paint();
	private Canvas mCanvas = new Canvas();
	
	private String[] horlabels = new String[] { "0:00", " ", "0:02", " ", "0:04", " ", "0:06", " ", "0:08", " ", "0:10", " ", "0:12", " ", "0:14", " ", "0:16", " ", "0:18", " ", "0:20", " ", "0:22", " ", "0:24", " ", "0:26", " ", "0:28", " ", "0:30"};
	
	
	private int mColor;

	public LineShape(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LineShape(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mColor = Color.argb(255, 0, 0, 0);
		mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		

		float border = 0;
	    float horstart = border * 2;
	    float height = getHeight();
	    float width = getWidth() - 1;
	    float graphheight = height - (2 * border);
	    float graphwidth = width - (2 * border);
	    
	    
		synchronized (this) {
			
			//ตารางพื้นหลังแกน y
			//String[] verlabels = new String[] { "70", "56", "42", "28", "14", " " };
			//String[] verlabels = new String[] { "70", "60", "50", "40", "30" };
			String[] verlabels = new String[] { "", "", "", "", "" };
			int vers = verlabels.length - 1;//x
		    for (int i = 0; i < 1; i++) {//เส้นประ
		        mPaint.setColor(Color.DKGRAY);//สีตารางแนวตั้ง
		        float y = ((graphheight / vers) * i) + border;
		        
		        //canvas.drawLine(horstart, y, width, y, mPaint);
		        Paint p = new Paint();
		        p.setStyle(Paint.Style.FILL);
		        p.setStrokeWidth(10);
		        p.setColor(Color.BLACK);
		        p.setPathEffect(new DashPathEffect(new float[] {2, 10}, 10));
	            canvas.drawLine(horstart, y, width, y, p);
		        if(i == 1)
		        {
		        	canvas.drawLine(horstart, y, width, y, mPaint);//เส้นทึบ
		        }
		        mPaint.setColor(Color.BLACK);//สี text แกน y
		        canvas.drawText(verlabels[i], 10, y, mPaint);
		    }
		    
		  //ตารางพื้นหลังแกน x
		    int hors = horlabels.length - 1;//y
		    for (int i = 15; i < 16; i++) {
		        mPaint.setColor(Color.DKGRAY);//สีตารางแนวนอน
		        float x = ((graphwidth / hors) * i) + horstart;
		        
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
