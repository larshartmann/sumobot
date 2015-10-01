package com.nordea.kronos.sumo;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;
import lejos.utility.Delay;

/*
 * class for handling a continuously moving range sensor.
 * 
 * By default it will rotate the sensor from -90 to 90 degrees in half a second.
 * It will take as many reading is can and find the lowest value.
 * 
 * If this value is lower than the previous one we store it, if not we reset to default values
 * 
 * The turning is handled using the motor listener interface
 * The reading is handled by the run method that is started on a separate thread.
 */

public class RangeSensor extends Observable implements RegulatedMotorListener, Runnable {

	public RangeSensor()
	{
		write( "RangeSensor starting", 1 );
		motor = new EV3MediumRegulatedMotor( MotorPort.C );
		sensor = new EV3IRSensor( SensorPort.S1 );
		direction = -1;
		running = true;
		
		readings1 = new LinkedList< Pair< Integer, Float > >();
		readings2 = new LinkedList< Pair< Integer, Float > >();
		readings1selected = true;

		write( "RangeSensor thread start", 1 );
		readingThread = new Thread( this );			
	}
	
	public void begin()
	{
		readingThread.start();
		
		write( "RangeSensor motor start", 1 );
		motor.setSpeed( 720 );
		motor.rotateTo( direction * 90, true );
		motor.addListener( this );
	}
	
	public void run()
	{
		LCD.drawString( "RangeSensor running", 0, 1 );
		
		SensorMode reading = null;
		Integer angle = 0;
		
		while( running )
		{
			angle = motor.getTachoCount() % 360;
			reading = sensor.getDistanceMode();
			
			float[] sample = { (float) 0.0 };
			int offset = 0;
			
			reading.fetchSample( sample, offset );
			
			write( "Sensor: " + sample[ offset ], 2 );
			write( "SensorAngle: " + angle, 3);
			
			Pair<Integer, Float> p = new Pair< Integer, Float >( angle, sample[ offset ] );
			
			if( readings1selected )
			{
				readings1.add( p );
			}
			else
			{
				readings2.add( p );
			}
			
			Delay.msDelay( 10 );
		}
		
		LCD.drawString( "RangeSensor stopping", 0, 1 );

		motor.removeListener();
		motor.rotateTo( 0 );		
		motor.flt();
		
		LCD.drawString( "RangeSensor stopped", 0, 1 );
	}
	
	public void start()
	{
		running = true;
	}
	
	public void stop()
	{
		running = false;
	}
	
	public int getAngle()
	{
		return angleOld;
	}
	
	public float getDistance()
	{
		return distanceOld;
	}
	
	@Override
	public void rotationStarted(RegulatedMotor motor, int tachoCount,
			boolean stalled, long timeStamp)
	{
		// TODO Auto-generated method stub
		if( readings1selected )
		{
			readings2.clear();
		}
		else
		{
			
			readings1.clear();
		}
	}
	
	@Override
	public void rotationStopped(RegulatedMotor motor, int tachoCount,
			boolean stalled, long timeStamp) {

		List< Pair< Integer, Float > > readings = null;

		if( readings1selected )
		{
			readings1selected = false;
			//write( "Read values1: " + readings1.size(), 6 );
			readings = readings1;
		}
		else
		{
			readings1selected = true;
			//write( "Read values2: " + readings2.size(), 7 );		
			readings = readings2;
		}
		
		// Find lowest distance and associated angle and store in *Old members;
		// if lowest is lower than previous lowest old value.
		
		if( readings.size() > 0 )
		{
			int a = readings.get( 0 ).getLeft();
			Float distance = readings.get( 0 ).getRight();
		
			for( int i = 1; i < readings.size(); ++i )
			{
				if( readings.get( i ).getRight() < distance )
				{
					a = readings.get( i ).getLeft();
					distance = readings.get( i ).getRight();
				}
			}
			
			if( distance > 70 )
			{
				angleOld = 0;
				distanceOld = distance;
			}
			else
			{
				angleOld = a;
				distanceOld = distance;
			}

			write( "Sensor: " + distanceOld, 4 );
			write( "SensorAngle: " + angleOld, 5);
		}

		setChanged();
		notifyObservers();
		
		direction = direction * -1;
		motor.rotateTo( direction * 90, true );
	}

	private void write( String msg, int row )
	{
		LCD.clear( row );
		LCD.drawString( msg, 0, row );
	}
	
	private EV3MediumRegulatedMotor motor;
	private EV3IRSensor sensor;
	private int direction;
	private boolean running;
	private Thread readingThread;
	
	private List< Pair< Integer, Float > > readings1;
	private List< Pair< Integer, Float > > readings2;
	private boolean readings1selected;
	
	private int angleOld;
	private float distanceOld;
}
