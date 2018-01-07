package pw.lictex.wallpaper.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by kpx on 1.7-2018.
 */

public abstract class AngleSensor {
    private Sensor sensor;
    private SensorEventListener listener;
    private OnRefreshListener onRefreshListener;

    public AngleSensor(SensorManager manager) {
        sensor = initSensor(manager);
        listener = initSensorListener();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public Sensor getSensor() {
        return sensor;
    }

    protected abstract SensorEventListener initSensorListener();

    protected abstract Sensor initSensor(SensorManager manager);

    protected void onResult(float x, float y, float z) {
        if (onRefreshListener != null) onRefreshListener.onRefresh(x, y, z);
    }

    public SensorEventListener getListener() {
        return listener;
    }

    public interface OnRefreshListener {
        void onRefresh(float x, float y, float z);
    }
}
