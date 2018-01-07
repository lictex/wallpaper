package pw.lictex.wallpaper;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pw.lictex.wallpaper.ease.Default;
import pw.lictex.wallpaper.ease.Ease;
import pw.lictex.wallpaper.i.GLWallpaperService;
import pw.lictex.wallpaper.layer.BitmapLayer;
import pw.lictex.wallpaper.layer.FPSLayer;
import pw.lictex.wallpaper.layer.Layer;
import pw.lictex.wallpaper.sensor.AngleSensor;
import pw.lictex.wallpaper.sensor.GyroscopeAngleSensor;
import pw.lictex.wallpaper.sensor.RotationVectorAngleSensor;

/**
 * Created by kpx on 12.30-2017.
 */


public class WallpaperService extends GLWallpaperService {
    int screenWidth = 0, screenHeight = 0;
    float screenRatio = 1;
    SensorManager sensorManager;
    SharedPreferences sharedPreferences;
    long screenOffTime = -1;
    KeyguardManager km;
    PowerManager pm;
    private int targetOffset = 0, drawOffset = 0;
    private float targetScale = 1, drawScale = 1;
    private float targetAlpha = 1, drawAlpha = 1;
    private Bitmap bitmap;
    private Ease x;
    private Ease s;
    private Ease a;
    private boolean screenUnlocked;
    private float gyroS = 100;
    private float touchS = 100;
    private AngleSensor angleSensor;
    private AngleSensor.OnRefreshListener onRefreshListener = new AngleSensor.OnRefreshListener() {
        @Override
        public void onRefresh(float x, float y, float z) {
            if (bitmap != null) {
                modifyTargetOffset(y / 100f * gyroS);
            }
        }
    };
    private boolean bitmapChanged = true;

    private void modifyTargetOffset(float f) {
        targetOffset -= f * 8;
        float v = (float) bitmap.getWidth() * (float) screenHeight / (float) bitmap.getHeight() - (float) screenWidth;
        if (targetOffset > v)
            targetOffset = (int) (v);
        if (targetOffset < 0)
            targetOffset = 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        load(null);
        pm = (PowerManager) WallpaperService.this.getSystemService(Context.POWER_SERVICE);
        km = (KeyguardManager) WallpaperService.this.getSystemService(Context.KEYGUARD_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        load(intent);
        return START_STICKY;
    }

    private void load(Intent intent) {
        sharedPreferences = getSharedPreferences("root", Context.MODE_PRIVATE);
        x = new Default(Settings.getInt(sharedPreferences, Settings.TRANSLATE_EASE));
        s = new Default(Settings.getInt(sharedPreferences, Settings.SCALE_EASE));
        a = new Default(Settings.getInt(sharedPreferences, Settings.ALPHA_EASE));
        gyroS = Settings.getInt(sharedPreferences, Settings.GYRO_TRANSLATE_SPEED);
        touchS = Settings.getInt(sharedPreferences, Settings.TOUCH_TRANSLATE_SPEED);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (angleSensor != null) angleSensor.unregister(sensorManager);
        if (Settings.getBoolean(sharedPreferences, Settings.USE_ROTATION_VECTOR)) {
            angleSensor = new RotationVectorAngleSensor(sensorManager);
        } else {
            angleSensor = new GyroscopeAngleSensor(sensorManager);
        }
        angleSensor.setOnRefreshListener(onRefreshListener);

        if (intent != null && intent.getBooleanExtra("bitmapChanged", false))
            loadBitmap();
        if (bitmap != null)
            returnToDefault();
    }

    private void returnToDefault() {
        targetOffset = (int) (Settings.getInt(sharedPreferences, Settings.DEFAULT_POSITION) / 100f * (Math.abs((bitmap.getWidth() * ((float) screenHeight / bitmap.getHeight())) - screenWidth)));
    }

    private void loadBitmap() {
        String string = Settings.getString(sharedPreferences, Settings.EXT_IMG_PATH);
        try {
            InputStream inputStream;
            if (string != null) {
                inputStream = openFileInput(string);
            } else {
                inputStream = getResources().openRawResourceFd(R.raw.wp).createInputStream();
            }
            byte[] bytes = Utils.InputStreamToByteArray(inputStream);
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            returnToDefault();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmapChanged = true;
    }

    @Override
    public Engine onCreateEngine() {
        loadBitmap();

        ScreenStatusReceiver instance = new ScreenStatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(instance, filter);
        instance.setOnScreenChangeListener(new ScreenStatusReceiver.OnStatusChangeListener() {
            @Override
            public void onChange(ScreenStatusReceiver.Status s) {
                switch (s) {
                    case OFF:
                        onScreenOff();
                        break;
                }
            }
        });

        return new WallpaperEngine();
    }

    private void onScreenOff() {
        if (pm.isScreenOn()) return;
        targetAlpha = Settings.getInt(sharedPreferences, Settings.ALPHA_SCREEN_OFF) / 100f;
        targetScale = Settings.getInt(sharedPreferences, Settings.SCALE_SCREEN_OFF) / 100f;
        drawAlpha = targetAlpha;
        drawScale = targetScale;
        screenUnlocked = false;
    }

    private void onBackground() {
        if (!pm.isScreenOn()) {
            onScreenOff();
            return;
        }
        targetScale = Settings.getInt(sharedPreferences, Settings.SCALE_SCREEN_OFF) / 100f;
        drawScale = targetScale;
    }

    private void onScreenOn() {
        if (screenUnlocked) return;
        targetAlpha = Settings.getInt(sharedPreferences, Settings.ALPHA_SCREEN_ON) / 100f;
        targetScale = Settings.getInt(sharedPreferences, Settings.SCALE_SCREEN_ON) / 100f;
        screenUnlocked = false;
    }

    private void onScreenUnlock() {
        targetAlpha = Settings.getInt(sharedPreferences, Settings.ALPHA_SCREEN_UNLOCKED) / 100f;
        targetScale = Settings.getInt(sharedPreferences, Settings.SCALE_SCREEN_UNLOCKED) / 100f;
        screenUnlocked = true;
    }

    class WallpaperEngine extends GLEngine {
        WallpaperRenderer renderer;

        float lastX;
        boolean touchDown;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            renderer = new WallpaperRenderer();
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);
            setTouchEventsEnabled(true);
        }


        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchDown = true;
                lastX = event.getX();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                touchDown = false;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE && touchDown) {
                modifyTargetOffset(((event.getX() - lastX) / 10) / 100f * touchS);
                lastX = event.getX();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                angleSensor.register(sensorManager, Settings.getInt(sharedPreferences, Settings.GYRO_DELAY));
                int i = Settings.getInt(sharedPreferences, Settings.RETURN_DEFAULT_TIME);
                if (screenOffTime != -1 && i != 61) {
                    if ((System.currentTimeMillis() - screenOffTime) / 1000 >= i) {
                        returnToDefault();
                    }
                }
                screenOffTime = -1;
                onScreenOn();
                if (!km.inKeyguardRestrictedInputMode()) {
                    onScreenUnlock();
                } else {
                    screenUnlocked = false;
                    onScreenOn();
                }
            } else {
                if (!pm.isScreenOn()) {
                    screenOffTime = System.currentTimeMillis();
                    onScreenOff();
                } else {
                    onBackground();
                }
                angleSensor.unregister(sensorManager);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (renderer != null) {
                renderer.release();
            }
            renderer = null;
        }


        public class WallpaperRenderer implements GLSurfaceView.Renderer {
            List<Layer> layers = new ArrayList<Layer>() {
                {
                    add(new BitmapLayer(bitmap));
                    add(new FPSLayer());
                }
            };

            public void onDrawFrame(GL10 gl) {
                if (!screenUnlocked && !km.inKeyguardRestrictedInputMode()) onScreenUnlock();

                float screenBitmapWidth = (float) bitmap.getWidth() * (float) screenHeight / (float) bitmap.getHeight();
                drawOffset = (int) x.nextDraw(targetOffset, drawOffset);
                drawScale = s.nextDraw(targetScale, drawScale);
                drawAlpha = a.nextDraw(targetAlpha, drawAlpha);
                if (bitmapChanged) {
                    for (Layer layer : layers) {
                        if (layer instanceof BitmapLayer) ((BitmapLayer) layer).change(gl, bitmap);
                    }
                    bitmapChanged = false;
                }

                gl.glClearColor(0f, 0f, 0f, 1f);
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
                gl.glViewport(0, 0, screenWidth, screenHeight);
                gl.glLoadIdentity();
                gl.glScalef(1 / screenRatio, 1, 1);

                Layer.RenderParams params = new Layer.RenderParams() {{
                    screenRatio = WallpaperService.this.screenRatio;
                    screenHeight = WallpaperService.this.screenHeight;
                    screenWidth = WallpaperService.this.screenWidth;
                }};
                for (Layer layer : layers) {
                    if (layer instanceof FPSLayer && !Settings.getBoolean(sharedPreferences, Settings.SHOW_FRAME_DELAY))
                        continue;
                    layer.setScale(drawScale);
                    layer.setAlpha(drawAlpha);
                    layer.setOffset((float) drawOffset / screenBitmapWidth);
                    layer.render(gl, params);
                }
            }

            public void onSurfaceChanged(GL10 gl, int width, int height) {
                WallpaperService.this.screenWidth = width;
                WallpaperService.this.screenHeight = height;
                screenRatio = (float) width / (float) height;
            }

            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            }

            void release() {
            }
        }
    }
}