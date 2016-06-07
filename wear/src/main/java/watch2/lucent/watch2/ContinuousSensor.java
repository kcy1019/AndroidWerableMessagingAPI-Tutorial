package watch2.lucent.watch2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

public class ContinuousSensor
    extends Service
    implements SensorEventListener
{

    private static final int SENS_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    private static final String TAG = "watch/ContinuousSensor";
    private static final String ACTION_WAKEUP = "ALARM_WAKEUP_ACTION";
    private static final long ALARM_INTERVAL = 5000;
    private MessageSender messageSender;
    private SensorManager sensorManager;
    private AlarmManager alarmManager;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private long lastMeasured;

    public ContinuousSensor() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null && action.equals(ALARM_SERVICE)) {
            wakeLock.acquire();
            wakeLock.release();
        } else {
            lastMeasured = -153523809;
            messageSender = MessageSender.getInstance(this);

            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(SENS_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);

            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

            alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(this, ContinuousSensor.class);
            alarmIntent.setAction(ACTION_WAKEUP);
            PendingIntent pendingAlarmIntent = PendingIntent.getService(this, 0, alarmIntent, 0);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(), ALARM_INTERVAL, pendingAlarmIntent);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (SystemClock.currentThreadTimeMillis() - lastMeasured >= ALARM_INTERVAL) {
            if (!wakeLock.isHeld())
                wakeLock.acquire();

            lastMeasured = SystemClock.currentThreadTimeMillis();

            String s = "Sensor:{";
            for (float x : event.values) {
                s = s + " " + x;
            }
            s = s + " }";
            Log.d(TAG, s);
            messageSender.sendMessage(TAG, s);

            if (wakeLock.isHeld())
                wakeLock.release();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
         Log.d(TAG, "Accuracy of sensor " + sensor + " is changed to: " + accuracy);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
