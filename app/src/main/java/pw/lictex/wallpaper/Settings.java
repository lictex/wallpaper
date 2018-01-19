package pw.lictex.wallpaper;

import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by kpx on 1.1-2018.
 */

public class Settings {
    public static final String GYRO_TRANSLATE_SPEED = "XS";
    public static final String TOUCH_TRANSLATE_SPEED = "XST";
    public static final String ALPHA_EASE = "AEV";
    public static final String SCALE_EASE = "SEV";
    public static final String TRANSLATE_EASE = "XEV";
    public static final String GYRO_DELAY = "GD";
    public static final String ALPHA_SCREEN_OFF = "ALPHA_SCREEN_OFF";
    public static final String ALPHA_SCREEN_ON = "ALPHA_SCREEN_ON ";
    public static final String ALPHA_SCREEN_UNLOCKED = "ALPHA_SCREEN_UNLOCKED";
    public static final String SCALE_SCREEN_OFF = "SCALE_SCREEN_OFF";
    public static final String SCALE_SCREEN_ON = "SCALE_SCREEN_ON";
    public static final String SCALE_SCREEN_UNLOCKED = "SCALE_SCREEN_UNLOCKED";
    public static final String DEFAULT_POSITION = "DEFAULT_POSITION";
    public static final String RETURN_DEFAULT_TIME = "RETURN_DEFAULT_TIME";
    public static final String EXT_IMG_PATH = "EXT_IMG_PATH";
    public static final String EXT_IMG_COUNT = "EXT_IMG_COUNT";
    public static final String SHOW_FRAME_DELAY = "SHOW_FRAME_DELAY";
    public static final String USE_ROTATION_VECTOR = "USE_ROTATION_VECTOR";
    private static HashMap<String, Object> def = new HashMap<String, Object>() {{
        put(GYRO_TRANSLATE_SPEED, 75);
        put(TOUCH_TRANSLATE_SPEED, 50);
        put(ALPHA_EASE, 8);
        put(SCALE_EASE, 12);
        put(TRANSLATE_EASE, 10);
        put(GYRO_DELAY, 2);
        put(ALPHA_SCREEN_OFF, 0);
        put(ALPHA_SCREEN_ON, 64);
        put(ALPHA_SCREEN_UNLOCKED, 100);
        put(SCALE_SCREEN_OFF, 150);
        put(SCALE_SCREEN_ON, 125);
        put(SCALE_SCREEN_UNLOCKED, 100);
        put(DEFAULT_POSITION, 66);
        put(RETURN_DEFAULT_TIME, 0);
        put(EXT_IMG_PATH, null);
        put(EXT_IMG_COUNT, 0);
        put(SHOW_FRAME_DELAY, false);
        put(USE_ROTATION_VECTOR, false);
    }};

    private Settings() {
    }

    public static int getInt(SharedPreferences sp, String tag) {
        return sp.getInt(tag, (Integer) def.get(tag));
    }

    public static boolean getBoolean(SharedPreferences sp, String tag) {
        return sp.getBoolean(tag, (Boolean) def.get(tag));
    }

    public static String getString(SharedPreferences sp, String tag) {
        return sp.getString(tag, def.get(tag) == null ? null : def.get(tag).toString());
    }
}
