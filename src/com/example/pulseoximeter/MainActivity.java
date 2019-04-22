package com.example.pulseoximeter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends IOIOActivity {

	//private static final SimpleDateFormat timesave = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
	
	//private GraphView mGraphView;
	//private LineShape mLineShape;
	 
	public File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/DataSpo2/");
	//android.os.Environment.getExternalStorageDirectory();
	public File dir = new File (root.getAbsolutePath());
	public Button saveControl;
	public Button start;
	public Button setup;
	private boolean isSave = false;
	
	private final int ANALOG_SENSOR_Red = 34;
	private final int ANALOG_SENSOR_IR = 33;
	private final int ANALOG_PWM_Red = 6;//PWM
	private final int ANALOG_PWM_IR = 7;//PWM 
	private final int ANALOG_OPEN_SENSOR_AT_RED_J2 = 15;
	private final int ANALOG_OPEN_SENSOR_AT_IR_J2 = 16;
	private final int ANALOG_CD4052B_A_J3 = 12;
	private final int ANALOG_CD4052B_B_J3 = 13;
	private final int ANALOG_CD4052B_A_J4 = 11;
	private final int ANALOG_CD4052B_B_J4 = 10;
	private final int ANALOG_CD4052B_A_J6 = 4;//*
	private final int ANALOG_CD4052B_B_J6 = 3;//*
	private final int ANALOG_CD4052B_C_J6 = 8;//*
	
	private final int Control_Compare = 5;//port out
	
	TextView textnamesave;
	TextView textCompareValue;
	TextView textStatusSaveText;
	TextView textSpo2Value;
	TextView textstatuscompare;
	TextView textStatusConnectText;
	
	String savename = "";
	String CompareSpo2 = "90";
	boolean low = false;
	boolean lowcheck = false;
	boolean lowcheckIR = false;
	boolean Start_Compare = false;
	boolean StartSave = false;
	boolean HeaderSave = false;
	int swap = 1;
	Timer myTimer;
	float DataRedVoltage;
	float DataIRVoltage;
	/*float OldDataRedVoltage = 0;
	float OldDataIRVoltage = 0;
	float OldDataRedLow = 0;
	float OldDataIRLow = 0;*/
	/*float PeakDataRedVoltage;
	float LowDataRedVoltage;
	float PeakDataIRVoltage;
	float LowDataIRVoltage;*/
	float rateSpo2;
	float[] Red_Buffered_Plots = new float[60];
	float[] IR_Buffered_Plots = new float[60];
	int pointplots = 0;
	int pointplotsIR = 0;
	float RATIO;
	float Red_Vpp;
	float IR_Vpp;
	float max;
	float min;
	float maxIR;
	float minIR;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		saveControl = (Button) findViewById(R.id.SaveData);
		start = (Button) findViewById(R.id.Start);
		setup = (Button) findViewById(R.id.setup);
		
		textnamesave = (TextView)findViewById(R.id.nameoffile);
		textCompareValue = (TextView)findViewById(R.id.CompareValue);
		textCompareValue.setText(CompareSpo2);
		textStatusSaveText = (TextView)findViewById(R.id.StatusSaveText);
		textSpo2Value = (TextView)findViewById(R.id.Spo2Value);
		textstatuscompare = (TextView)findViewById(R.id.statuscompare);
		textStatusConnectText = (TextView)findViewById(R.id.StatusConnectText);
		
		start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
            	start();
            	
            }
        });
		
		saveControl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
            	saveControl();
            	
            }
        });
		
		setup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
            	setup();
            	
            }
        });
		
		/*mGraphView = (GraphView) findViewById(R.id.graph);
		mGraphView.setMaxValue(50);*/
		
		//textRed = (TextView)findViewById(R.id.textRed);
		//txtIR = (TextView)findViewById(R.id.textIR);
		//textStatusConnect = (TextView)findViewById(R.id.textStatusConnect);
		 
		
	}
	
	public void saveData(){
		
		try {
			if(StartSave == true){
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    	File file = new File(root, savename + ".txt");
		    	if (!file.exists()) {
		    		root.mkdirs();
		    	}
		    	
		    	FileWriter pw = new FileWriter(file, true);
		    	if(HeaderSave == true){
		    		String bufferHeader = "Date/Time" + "     " + "SpO2%" + "     " + "CompareSpO2 less than" + "     " + "Red" + "     " + "IR" + "\n";
		    		pw.write(bufferHeader);
		    		HeaderSave = false;
		    	}
		    	
		    	String buffer = timestamp + "     " + (int)rateSpo2 + "     " + CompareSpo2 + "     " + DataRedVoltage + "     " + DataIRVoltage + "\n";
		    	pw.write(buffer);
		        pw.close();     
		        
		        //StartSave = false;
			}
			
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }  
	}
	
	public void setup(){
		//ใส่ชื่อเซฟ
    	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.setting, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit2);

        dialogBuilder.setTitle("setting SpO2");
        dialogBuilder.setMessage("Enter number SpO2");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
            	CompareSpo2 = edt.getText().toString();
            	textCompareValue.setText(CompareSpo2);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            	
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
	}
	
	public void start(){
		
		if(Start_Compare == false){
			Start_Compare = true;
			start.setText("Stop");
		}else{
			Start_Compare = false;
			start.setText("Start");
		}
	}
	
	public void saveControl(){
    	
    	if(isSave == false){
    		StartSave = true;
    		saveControl.setText("Cancel Save");
    		//ใส่ชื่อเซฟ
        	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.alert, null);
            dialogBuilder.setView(dialogView);

            final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);

            dialogBuilder.setTitle("Save As");
            dialogBuilder.setMessage("Enter name .txt");
            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //do something with edt.getText().toString();
                	savename = edt.getText().toString();
                	textnamesave.setText(savename + ".txt");
                	textStatusSaveText.setText("Status: Saving");
                	isSave = true;
                	HeaderSave = true;
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                	isSave = false;
            		saveControl.setText("SaveData");
            		textStatusSaveText.setText("Status: Not Save!");
                }
            });
            AlertDialog b = dialogBuilder.create();
            b.show();
    		
    	}else{
    		isSave = false; 
    		saveControl.setText("SaveData");
    		textStatusSaveText.setText("Status: Not Save!");
    	}
    	
    	
    }
	
	class Looper extends BaseIOIOLooper{
		
		
		AnalogInput DataRed,DataIR,test;
		DigitalOutput ControlDigitalRed,ControlDigitalIR;
		DigitalOutput ControlDigitalA_J3,ControlDigitalB_J3;
		DigitalOutput ControlDigitalA_J4,ControlDigitalB_J4;
		DigitalOutput ControlDigitalA_J6,ControlDigitalB_J6,ControlDigitalC_J6;
		
		PwmOutput PwmRed;
		PwmOutput PwmIR;
		
		DigitalOutput ControlSpO2;
		
		@Override
		protected void setup() throws ConnectionLostException, InterruptedException{
			
			
			
			DataRed = ioio_.openAnalogInput(ANALOG_SENSOR_Red);//6
			DataIR = ioio_.openAnalogInput(ANALOG_SENSOR_IR);//7
			
			ControlSpO2 = ioio_.openDigitalOutput(Control_Compare);
			
			ControlDigitalRed = ioio_.openDigitalOutput(ANALOG_OPEN_SENSOR_AT_RED_J2);//15
			ControlDigitalIR = ioio_.openDigitalOutput(ANALOG_OPEN_SENSOR_AT_IR_J2);//16
			ControlDigitalA_J3 = ioio_.openDigitalOutput(ANALOG_CD4052B_A_J3);//12
			ControlDigitalB_J3 = ioio_.openDigitalOutput(ANALOG_CD4052B_B_J3);//13
			ControlDigitalA_J4 = ioio_.openDigitalOutput(ANALOG_CD4052B_A_J4);//11
			ControlDigitalB_J4 = ioio_.openDigitalOutput(ANALOG_CD4052B_B_J4);//10
			ControlDigitalA_J6 = ioio_.openDigitalOutput(ANALOG_CD4052B_B_J6);//3
			ControlDigitalB_J6 = ioio_.openDigitalOutput(ANALOG_CD4052B_A_J6);//4
			ControlDigitalC_J6 = ioio_.openDigitalOutput(ANALOG_CD4052B_C_J6);//8 select RED,IR
			
			PwmRed = ioio_.openPwmOutput(ANALOG_PWM_Red, 1000);
			PwmIR = ioio_.openPwmOutput(ANALOG_PWM_IR, 1000);
			
			PwmRed.setDutyCycle(22);
			PwmIR.setDutyCycle(22);
			
			PwmRed.setPulseWidth(600);
			PwmIR.setPulseWidth(600);
		
			myTimer = new Timer();
			myTimer.schedule(new TimerTask() {
				  public void run() {
				    // Your database code here
					  
					  if(swap == 0){
						  swap = 1;
						  low = false;
						  /*OldDataRedVoltage = 0;
						  OldDataIRVoltage = 0;
						  OldDataIRLow = 0;
						  OldDataRedLow = 0;*/
						  lowcheck = false;
						  
						  findpeak(DataRedVoltage);
						  
					  }else{
						  swap = 0;
						  low = true;
						  /*OldDataRedVoltage = 0;
						  OldDataIRVoltage = 0;
						  OldDataIRLow = 0;
						  OldDataRedLow = 0;*/
						  lowcheck = false;
						  
						  findpeakIR(DataIRVoltage);
						  findSpo2(max, min, maxIR, minIR);
					  }
					  
					  /*if(StartSave == false){
						  StartSave = true;
					  }*/
					  
					   
				  }
				}, 0, 5000);
			
			
			
			runOnUiThread(new Runnable() {
                public void run() {
                	// Toast message "Connect"
                	// when android device connect with IOIO board
                	textStatusConnectText.setText("Status Board: is Connected!");
                    
                }        
            });
		}
		@Override
		public void loop() throws ConnectionLostException, InterruptedException{
			
                	// Toast message "Connect" 
                	// when android device connect with IOIO board
                	try{
                		
                		DataRed.setBuffer(60);
                		DataIR.setBuffer(60);
                		
                		if(low == true){
                			//read Red
                			
                        	ControlDigitalRed.write(true);//15 open Red
                        	ControlDigitalIR.write(false);//16 close IR
                        	ControlDigitalA_J6.write(false);//3 open PWM Red
                        	ControlDigitalB_J6.write(false);//4 open PWM Red
                        	ControlDigitalC_J6.write(low);//8 select Red true
                        	ControlDigitalA_J3.write(false);//12 open signal Red
                        	ControlDigitalB_J3.write(false);//13 open signal Red
                        	ControlDigitalA_J4.write(false);//11 open signal Red
                        	ControlDigitalB_J4.write(false);//10 open signal Red
                        		
                        	
                        	
                    		DataRedVoltage = DataRed.getVoltage();
                    		Red_Buffered_Plots[pointplots] = DataRedVoltage;
                    		pointplots++;
                    		
                    		
                		}else if(low == false){
                			
                			//read IR
                			
                			ControlDigitalRed.write(false);//15 close Red
                    		ControlDigitalIR.write(true);//16 open IR
                    		ControlDigitalA_J6.write(true);//3 open PWM IR
                    		ControlDigitalB_J6.write(false);//4 open PWM IR
                    		ControlDigitalC_J6.write(low);//8 select IR false
                    		ControlDigitalA_J3.write(true);//12 open signal IR
                    		ControlDigitalB_J3.write(false);//13 open signal IR
                    		ControlDigitalA_J4.write(true);//11 open signal IR
                    		ControlDigitalB_J4.write(false);//10 open signal IR
                    		
                    		
                    		
                			DataIRVoltage = DataIR.getVoltage();
                			IR_Buffered_Plots[pointplotsIR] = DataIRVoltage;
                    		pointplotsIR++;
                    		
                    		
                		}
                			
            			
            			if(pointplots == 60){
            				pointplots = 0;
            				
            			}
            			if(pointplotsIR == 60){
            				pointplotsIR = 0;
            			}
                		
            			
            			//addPoint(DataIRVoltage*20);
            			
                		runOnUiThread(new Runnable() {
                            public void run() {
                            	//addpoint
                            	//addPoint(DataIRVoltage+40);   
                            	//addPoint(IR_Buffered_Plots[pointplots]*30-20);
                            	
                            	
                            	
                            	if(Start_Compare == true){
                            		
                    				if(rateSpo2 < Integer.valueOf(CompareSpo2)){
                    					//open port
                    					try {
											ControlSpO2.write(true);
										} catch (ConnectionLostException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
                    					textstatuscompare.setText("Status: On Oxygen");
                    				}else{
                    					//close port
                    					try {
											ControlSpO2.write(false);
										} catch (ConnectionLostException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
                    					textstatuscompare.setText("Status: Off Oxygen");
                    				}
                    			}else{
                    				try {
										ControlSpO2.write(false);
									} catch (ConnectionLostException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
                					textstatuscompare.setText("Status: Off Oxygen"); 
                    			}
                            	
                            	if(rateSpo2 <= 0){
                            		rateSpo2 = 0;
                            	}
		                		
                            	if(rateSpo2 >= 100){
            						rateSpo2 = 100;
                              	}
                              	textSpo2Value.setText(String.format("%.0f",rateSpo2));
                            	
                              	if(isSave == true){
            						  saveData();
            					}
                            }        
                        });
                		
                		
                		Thread.sleep(30);
        			} catch (InterruptedException e) { 
        				e.printStackTrace();
        			} catch (ConnectionLostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	
                	
                
			
				
		}
		
		public void disconnected(){
			runOnUiThread(new Runnable() {
                public void run() {
                	// Toast message "Connect" 
                	// when android device connect with IOIO board
                	max = 0;
                    min = 0;
                    maxIR = 0;
                    minIR = 0;
                    textStatusConnectText.setText("Status Board: is Connected!");
                    
                }        
            });
		}
		
		public void incompatible(){
			
		}
		
	}
	
	protected IOIOLooper createIOIOLooper(){
		return new Looper();
	}
	

	
	private void addPoint(final float point) {
		runOnUiThread(new Runnable() {
			public void run(){
				
		    	//mGraphView.addDataPoint(point);
			}
		});
	}
	
	private void findpeakIR(float DataIRVoltage){
		
		maxIR = IR_Buffered_Plots[0];
		minIR = IR_Buffered_Plots[0];
		for (int i = 1; i < IR_Buffered_Plots.length; i++) {
		    if (IR_Buffered_Plots[i] > maxIR) {
		        maxIR = IR_Buffered_Plots[i];
		    }
		    if (IR_Buffered_Plots[i] < minIR) {
		        minIR = IR_Buffered_Plots[i];
		    }
		}
		
		/*if(lowcheckIR == false){
			if(DataIRVoltage > OldDataIRVoltage){
				OldDataIRVoltage = DataIRVoltage;
			}else{
				PeakDataIRVoltage = OldDataIRVoltage;
				OldDataIRLow = OldDataIRVoltage;
				lowcheckIR = true;
			}
		}
		
		
		if(lowcheckIR == true){//low edit
			if(DataIRVoltage < OldDataIRLow){ 
				OldDataIRLow = DataIRVoltage;
			}else{
				LowDataIRVoltage = OldDataIRLow;
				lowcheckIR = false;
			}
			
		}*/
	}
	
	private void findpeak(float DataRedVoltage){
		
		//datapeakRed
		max = Red_Buffered_Plots[0];
		min = Red_Buffered_Plots[0];
		for (int i = 1; i < Red_Buffered_Plots.length; i++) {
		    if (Red_Buffered_Plots[i] > max) {
		        max = Red_Buffered_Plots[i];
		    }
		    if (Red_Buffered_Plots[i] < min) {
		        min = Red_Buffered_Plots[i];
		    }
		}
		
		
		/*if(lowcheck == false){
			if(DataRedVoltage > OldDataRedVoltage){
				OldDataRedVoltage = DataRedVoltage;
			}else{
				PeakDataRedVoltage = OldDataRedVoltage;
				addPoint(5);
				OldDataRedLow = OldDataRedVoltage;
				lowcheck = true;
			}
		}
		
		
		if(lowcheck == true){//low edit
			if(DataRedVoltage < OldDataRedLow){ 
				OldDataRedLow = DataRedVoltage;
			}else{
				LowDataRedVoltage = OldDataRedLow;
				lowcheck = false;
			}
			
		}*/
		//OldDataRedLow = OldDataRedVoltage;
		
		/*if(DataRedVoltage > OldDataRedVoltage){
			
		}else{
			PeakDataRedVoltage = OldDataRedVoltage;
			//low = true;
		}
		
		if(controlR_IR == true){
			if(DataRedVoltage < OldDataRedVoltage){
				
			}else{
				LowDataRedVoltage = OldDataRedVoltage;
				controlR_IR = false;
			}
			
		}
		OldDataRedVoltage = DataRedVoltage;*/
		
		//datapeakIR
		/*if(DataIRVoltage > OldDataIRVoltage){
					
		}else{
			PeakDataIRVoltage = OldDataIRVoltage;
			controlR_IR = true;
		}
				
		if(controlR_IR == true){
			if(DataIRVoltage < OldDataIRVoltage){
						
			}else{
				LowDataIRVoltage = OldDataIRVoltage;
				controlR_IR = false;
				findSpo2(PeakDataRedVoltage, LowDataRedVoltage, PeakDataIRVoltage, LowDataIRVoltage);
			}
					
		}
		OldDataIRVoltage = DataIRVoltage;*/
		
	}
	
	
	private float findSpo2(float PeakDataRed,float LowdataRed,float PeakDataIR,float LowdataIR){
		Red_Vpp = PeakDataRed - LowdataRed;
		float Red_AC_Vrms = (float) (0.707*Red_Vpp);
		IR_Vpp = PeakDataIR - LowdataIR;
		float IR_AC_Vrms = (float) (0.707*IR_Vpp);
		//RATIO = (Red_AC_Vrms/Red_Vpp)/(IR_AC_Vrms/IR_Vpp);
		float RatioRed =  Red_Vpp / (Red_AC_Vrms + LowdataRed);
		float RatioIR =  IR_Vpp / (IR_AC_Vrms + LowdataIR);
		float Ratio = RatioRed/RatioIR;
		rateSpo2 = (float) (110 - (Ratio*25));
		return rateSpo2;
		
	}

}
