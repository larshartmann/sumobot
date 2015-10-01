package com.nordea.kronos.sumo;

import java.util.Observable;

import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

public class ColorSensor extends Observable implements Runnable
{
	private EV3ColorSensor color;
	private boolean running;
	private Thread readingThread;
	private boolean borderFound;

	public ColorSensor()
	{
		color = new EV3ColorSensor( SensorPort.S3 );
		running = true;
		readingThread = new Thread( this );
		borderFound = false;
	}
	
	public void begin()
	{
		readingThread.start();
	}
	
	public void stop()
	{
		running = false;
	}
	
	public boolean isBorder()
	{
		return borderFound;
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
			reading = color.getColorIDMode();

			reading.fetchSample( sample, offset );

			boolean prev = borderFound;
			
			LCD.clear( 6 );
			LCD.drawString( "value: " + sample[ offset ], 0, 6 );
			
			borderFound = sample[ offset ] == 7.0;

			if( prev != borderFound )
			{
				setChanged();
				notifyObservers();
			}
			
			Delay.msDelay( 100 );
		}
	}
}
