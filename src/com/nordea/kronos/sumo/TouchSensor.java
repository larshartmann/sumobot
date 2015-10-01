package com.nordea.kronos.sumo;

import java.util.Observable;

import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

public class TouchSensor extends Observable implements Runnable
{
	private EV3TouchSensor touch;
	private boolean running;
	private Thread readingThread;
	private boolean touched;

	public TouchSensor()
	{
		touch = new EV3TouchSensor( SensorPort.S4 );
		running = true;
		readingThread = new Thread( this );
		touched = false;
	}
	
	public void begin()
	{
		readingThread.start();
	}
	
	public void stop()
	{
		running = false;
	}

	public boolean isTouched()
	{
		return touched;
	}
	
	@Override
	public void run()
	{
		SensorMode reading = null;

		float[] sample = { (float) 0.0 };
		int offset = 0;
	
		// TODO Auto-generated method stub
		while( running )
		{
			reading = touch.getTouchMode();

			reading.fetchSample( sample, offset );

			boolean prev = touched;
			
			touched = sample[ offset ] != 0.0;

			//LCD.clear( 6 );
			//LCD.drawString( "t: " + sample[ offset ], 0, 6 );
			
			if( prev != touched )
			{
				setChanged();
				notifyObservers();
			}
			
			Delay.msDelay( 100 );
		}
	}
}
