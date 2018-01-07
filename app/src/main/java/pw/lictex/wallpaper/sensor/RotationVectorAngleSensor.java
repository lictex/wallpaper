package pw.lictex.wallpaper.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by kpx on 1.7-2018.
 */

public class RotationVectorAngleSensor extends AngleSensor {
    public RotationVectorAngleSensor(SensorManager manager) {
        super(manager);
    }

    @Override
    protected SensorEventListener[] initSensorListeners() {
        return new SensorEventListener[]{new SensorEventListener() {
            float[] or = new float[9];
            long lt = -1;

            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] r = new float[9];
                SensorManager.getRotationMatrixFromVector(r, event.values);

                if (System.currentTimeMillis() - lt < 600) {
                    float angle[] = new float[3];
                    SensorManager.getAngleChange(angle, r, or);
                    for (int i = 0; i < angle.length; i++) {
                        if (Math.abs(angle[i]) < 0.001) angle[i] = 0;//又是漂移(?)
                    }
                    onResult((float) Math.toDegrees(angle[1]), (float) Math.toDegrees(angle[2]), (float) Math.toDegrees(angle[0]));
                }
                System.arraycopy(r, 0, or, 0, r.length);
                lt = System.currentTimeMillis();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }};

    }

    @Override
    protected Sensor[] initSensors(SensorManager manager) {
        return new Sensor[]{manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)};
    }
}
