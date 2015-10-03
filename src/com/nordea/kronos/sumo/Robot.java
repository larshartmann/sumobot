package com.nordea.kronos.sumo;

import java.util.Observable;
import java.util.Observer;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.RangeFinder;
import lejos.robotics.RangeFinderAdapter;
import lejos.robotics.RangeReading;
import lejos.robotics.RangeReadings;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RotatingRangeScanner;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.utility.Delay;

public class Robot implements Observer, Runnable{

	public static void main(String[] args)
	{
		LCD.drawString( "Starting", 0, 0 );
		
		Robot robot = new Robot();

		while( Button.ESCAPE.isUp() )
		{
			Delay.msDelay( 500 );
		}
		
		LCD.drawString( "Stopping", 0, 0 );
		robot.stop();
		
		Delay.msDelay( 5000 );
	}

	public Robot()
	{
		wheelDiameter = 3.0; // cm
		trackWidth = 19.0; // cm		pilot.stop();
		moveMotor1 = new EV3LargeRegulatedMotor(MotorPort.D);
		moveMotor2 = new EV3LargeRegulatedMotor(MotorPort.B);		
		pilot = new DifferentialPilot(wheelDiameter, trackWidth, moveMotor1, moveMotor2);
		//pilot.setMinRadius( 15 );
		pilot.setTravelSpeed( standardSpeed );

		rangeSensor = new RangeSensor();
		rangeSensor.addObserver( this );

		touch = new TouchSensor();
		touch.addObserver( this );

		color = new ColorSensor();
		color.addObserver( this );
		
		runningThread = new Thread( this );
		
		running = true;
		stopped = false;
		
		begin();
		
		border = false;
	}
	
	public void begin()
	{
		LCD.drawString( "Press enter", 0, 4 );
		while( Button.ENTER.isUp() )
		{
			Delay.msDelay( 100 );
		}
		
		int count = 3;
		
		while( count > 0 )
		{
			LCD.clear( 4 );
			LCD.drawString( "Waiting " + count + " sec", 0, 4 );
			
			Delay.msDelay( 1000 );
			count = count - 1;
		}
				
		LCD.clear();
		LCD.drawString( "Running", 0, 4 );
		
		pilot.setTravelSpeed( maxSpeed );
		pilot.rotate( 135 );
		pilot.setTravelSpeed( standardSpeed );
		
		rangeSensor.begin();
		color.begin();
		touch.begin();
		
		runningThread.start();
	}
	
	public void stop()
	{
		running = false;
		while( ! stopped )
		{
			Delay.msDelay( 100 );
		}
		
		LCD.clear( 7 );
		LCD.drawString( "1 Stopping pilot", 0, 7 );
		pilot.stop();
		LCD.clear( 7 );
		LCD.drawString( "2 delete observers", 0, 7 );
		rangeSensor.deleteObservers();
		color.deleteObservers();
		touch.deleteObservers();
		LCD.clear( 7 );
		LCD.drawString( "3 Stopping pilot", 0, 7 );
		pilot.stop();
		LCD.clear( 7 );
		LCD.drawString( "4 Stopping sensor", 0, 7 );
		rangeSensor.stop();
		color.stop();
		touch.stop();
		LCD.clear( 7 );
		LCD.drawString( "5 Stopped", 0, 7 );
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		boolean beastModeStarted = false;
		
		while( running )
		{
			if( touched )
			{
				LCD.clear( 7 );
				LCD.drawString( "Beast", 0, 7 );
				pilot.setTravelSpeed( maxSpeed );
		
				if( ! beastModeStarted )
				{
					beastModeStarted = true;
					pilot.forward();
				}
			}
			else
			{
				if( beastModeStarted )
				{
					if( border )
					{	
						beastModeStarted = false;
						LCD.clear( 7 );
						pilot.setTravelSpeed( standardSpeed );
						pilot.stop();

						pilot.travel( -7.0 );
						pilot.rotate( 145, true );
					}
				}

				if( border )
				{	
					pilot.setTravelSpeed( maxSpeed );
					pilot.travel( -7.0 );
					pilot.rotate( 145, true );
					pilot.setTravelSpeed( standardSpeed );
				}
				else
				{
				//LCD.clear( 7 );
				//LCD.drawString( "Motor Run:", 0, 7 );
					if( angle > 25 )
					{
					// turn left
						pilot.rotate( angle / 2 );
					}
					else if( angle < -25 )
					{
						// turn right
						pilot.rotate( angle / 2 );
					}
				
				//LCD.clear( 7 );
				//LCD.drawString( "Motor forward:", 0, 7 );
				
					if( ! pilot.isMoving() )
					{
						pilot.forward();
					}
				}
			}
		}
		
		stopped = true;
	}

	@Override
	public void update(Observable o, Object arg)
	{
		if( o instanceof RangeSensor )
		{
			//LCD.clear( 7 );
			//LCD.drawString( "updating heading", 0, 7 );
			
			// Read new heading
			angle = rangeSensor.getAngle();
			distance = rangeSensor.getDistance();
		}
		else if( o instanceof ColorSensor )
		{
			border = color.isBorder();
		}
		else if( o instanceof TouchSensor )
		{
			touched = touch.isTouched();
		}
	}
	
	private RangeSensor rangeSensor;
	private double wheelDiameter; // cm
	private double trackWidth; // cm
	private RegulatedMotor moveMotor1;
	private RegulatedMotor moveMotor2;		
	private DifferentialPilot pilot;
	
	private TouchSensor touch;
	private ColorSensor color;
	
	private int angle;
	private float distance;
	private boolean running;
	private Thread runningThread;
	private boolean stopped;
	
	private boolean border;
	private boolean touched;
	
	private static int maxSpeed = 100;
	private static int standardSpeed = 12;
}
