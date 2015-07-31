package com.equake.ident;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class IdentEquakeProjectActivity extends Activity implements LocationListener {

	private LocationManager locationManager;
	private String locationProvider = LocationManager.NETWORK_PROVIDER;
	private Criteria locationCriteria;

	private SensorManager senSensorManager;
	private Sensor senAccelerometer;

	public double magnitude = 0;

	public TextView statusText;

	private boolean quakeDetected = false;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ParseAnalytics.trackAppOpenedInBackground(getIntent());

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationCriteria = new Criteria();

		statusText = (TextView) findViewById(R.id.statusText);

		//Handle Buttons
		ImageButton buttonGreen = (ImageButton) findViewById(R.id.buttonGreen);
		ImageButton buttonYellow = (ImageButton) findViewById(R.id.buttonYellow);
		ImageButton buttonRed = (ImageButton) findViewById(R.id.buttonRed);

		buttonGreen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reportQuake(1.0, "a Tremor");
			}
		});
		buttonYellow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reportQuake(5.0, "a Bad Earthquake");
			}
		});
		buttonRed.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reportQuake(9.0, "an Absolute Disaster");
			}
		});

		//SensorCode
		SensorHandler sensorHandler = new SensorHandler(this);

		senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		senSensorManager.registerListener(sensorHandler, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void reportQuake(double magnitude, String quakeSeverity) {
		this.magnitude = magnitude;
		locationProvider = locationManager.getBestProvider(locationCriteria, true);

		Location lastLocation = locationManager.getLastKnownLocation(locationProvider);


		if (lastLocation != null) {
			System.out.println("Provider " + locationProvider + " has been selected.");
			quakeDetected = true;
			onLocationChanged(lastLocation);
		} else {
			System.out.println("No Signal");
			Toast.makeText(getApplicationContext(), "No location signal!", Toast.LENGTH_LONG).show();
		}

		statusText.setText("In " + quakeSeverity);
	}

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(locationProvider, 400, 1, this);
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		double lat = location.getLatitude();
		double lng = location.getLongitude();


		if (quakeDetected) {
			ParseGeoPoint currentLocation = new ParseGeoPoint(lat, lng);
			ParseACL acl = new ParseACL();
			acl.setPublicReadAccess(true);
			acl.setPublicWriteAccess(true);

			ParseObject reportClick = new ParseObject("location");
			reportClick.setACL(acl);
			reportClick.put("location", currentLocation);
			reportClick.put("Magnitude", magnitude);
			reportClick.saveInBackground();
			Toast.makeText(getApplicationContext(), "Quake reported to the government", Toast.LENGTH_LONG).show();
			quakeDetected = false;
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}
}
