package pw.lictex.wallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kpx on 12.31-2017.
 */

public class ScreenStatusReceiver extends BroadcastReceiver {
    private OnStatusChangeListener onScreenChangeListener;

    public void setOnScreenChangeListener(OnStatusChangeListener onScreenChangeListener) {
        this.onScreenChangeListener = onScreenChangeListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            if (onScreenChangeListener != null) onScreenChangeListener.onChange(Status.LOCK);
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            if (onScreenChangeListener != null) onScreenChangeListener.onChange(Status.OFF);
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            if (onScreenChangeListener != null) onScreenChangeListener.onChange(Status.UNLOCK);
        }
    }

    public enum Status {
        OFF, LOCK, UNLOCK
    }

    public interface OnStatusChangeListener {
        void onChange(Status s);
    }
}
