package com.equake.ident;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Bhish on 29/07/2015.
 */
public class SensorHandler implements SensorEventListener {

    private IdentEquakeProjectActivity mainActivity;

    private final float UPPER_Y = 0.2f, LOWER_Y = -0.2f;
    private final int TIMER_THRESHOLD = 10000; //10 seconds

    private boolean hitUpper = false, hitLower = false;

    private LinkedList<Date> quakeQueue = new LinkedList<>();

    public SensorHandler(IdentEquakeProjectActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Handle the accelerometer change
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //detectEarthquake
            if (y >= UPPER_Y) {
                hitUpper = true;
                if (hitLower == true) {
                    quakeQueue.add(Calendar.getInstance().getTime());
                    hitUpper = false;
                    hitLower = false;
                }
            }

            if (y <= LOWER_Y) {
                hitLower = true;
                if (hitUpper == true) {
                    quakeQueue.add(Calendar.getInstance().getTime());
                    hitUpper = false;
                    hitLower = false;
                }
            }

            if (quakeQueue.size() == 5 &&
                    quakeQueue.peekLast().getTime() - quakeQueue.remove().getTime() <= TIMER_THRESHOLD) {
                mainActivity.reportQuake(9.0, "an Earthquake!!");
            }
        }
    }

    private void detectEarthquake() {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //blah blah blah
        //do nothing
    }
}
