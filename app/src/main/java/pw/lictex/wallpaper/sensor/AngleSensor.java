package pw.lictex.wallpaper.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by kpx on 1.7-2018.
 */

public abstract class AngleSensor {
    private Sensor[] sensors;
    private SensorEventListener[] listeners;
    private OnRefreshListener onRefreshListener;

    public AngleSensor(SensorManager manager) {
        sensors = initSensors(manager);
        listeners = initSensorListeners();
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    protected abstract SensorEventListener[] initSensorListeners();

    protected abstract Sensor[] initSensors(SensorManager manager);

    protected void onResult(float x, float y, float z) {
        if (onRefreshListener != null) onRefreshListener.onRefresh(x, y, z);
    }

    public void register(SensorManager sensorManager, int rate) {
        for (int i = 0; i < sensors.length; i++) {
            sensorManager.registerListener(listeners[i], sensors[i], rate);
        }
    }

    public void unregister(SensorManager sensorManager) {
        for (SensorEventListener eventListener : listeners) {
            sensorManager.unregisterListener(eventListener);
        }
    }

    public interface OnRefreshListener {
        void onRefresh(float x, float y, float z);
    }
}
