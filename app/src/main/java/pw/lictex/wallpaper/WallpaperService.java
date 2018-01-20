package pw.lictex.wallpaper;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import pw.lictex.wallpaper.layer.SolidColorLayer;
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
    private float targetOffset = 0, drawOffset = 0;
    private float targetScale = 1, drawScale = 1;
    private float targetBrightness = 1, drawBrightness = 1;
    private ArrayList<Bitmap> bitmap = new ArrayList<>();
    private int primaryIndex = 0;
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
            if (bitmap.get(primaryIndex) != null) {
                modifyTargetOffset(y / 100f * gyroS);
            }
        }
    };
    private boolean bitmapChanged = true;

    private void modifyTargetOffset(float f) {
        targetOffset -= f * 8;
        float v = (float) bitmap.get(primaryIndex).getWidth() * (float) screenHeight / (float) bitmap.get(primaryIndex).getHeight() - (float) screenWidth;
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
        if (bitmap.size() != 0 && bitmap.get(primaryIndex) != null)
            returnToDefault();
    }

    private void returnToDefault() {
        targetOffset = (int) (Settings.getInt(sharedPreferences, Settings.DEFAULT_POSITION) / 100f * (Math.abs((bitmap.get(primaryIndex).getWidth() * ((float) screenHeight / bitmap.get(primaryIndex).getHeight())) - screenWidth)));
    }

    private void loadBitmap() {
        String string = Settings.getString(sharedPreferences, Settings.EXT_IMG_PATH);
        bitmap.clear();
        try {
            if (string != null) {
                File parent = getDir(string, MODE_PRIVATE);
                for (int i = 0; i < Settings.getInt(sharedPreferences, Settings.EXT_IMG_COUNT); i++) {
                    bitmap.add(Utils.bitmapFromInputStream(new FileInputStream(new File(parent, String.valueOf(i)))));
                }
            } else {
                bitmap.add(Utils.bitmapFromResource(this, R.raw.wp));
            }
            returnToDefault();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmapChanged = true;
    }

    @Override
    public Engine onCreateEngine() {
        loadBitmap();
        return new WallpaperEngine();
    }

    private void showLockScreen() {
        drawBrightness = Settings.getInt(sharedPreferences, Settings.ALPHA_SCREEN_OFF) / 100f;
        drawScale = Settings.getInt(sharedPreferences, Settings.SCALE_SCREEN_OFF) / 100f;
        targetBrightness = Settings.getInt(sharedPreferences, Settings.ALPHA_SCREEN_ON) / 100f;
        targetScale = Settings.getInt(sharedPreferences, Settings.SCALE_SCREEN_ON) / 100f;
        screenUnlocked = false;
    }

    private void showUnlockScreen() {
        drawScale = Settings.getInt(sharedPreferences, Settings.SCALE_SCREEN_OFF) / 100f;
        onScreenUnlock();
    }

    private void onScreenUnlock() {
        targetBrightness = Settings.getInt(sharedPreferences, Settings.ALPHA_SCREEN_UNLOCKED) / 100f;
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
                if (!km.inKeyguardRestrictedInputMode()) {
                    showUnlockScreen();
                } else {
                    screenUnlocked = false;
                    showLockScreen();
                }
            } else {
                if (!pm.isScreenOn()) {
                    screenOffTime = System.currentTimeMillis();
                    showLockScreen();
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
            List<Layer> layers;
            FPSLayer fpsLayer = new FPSLayer();
            SolidColorLayer solidColorLayer = new SolidColorLayer();

            long lastDraw = -1;

            public void onDrawFrame(GL10 gl) {
                if (!screenUnlocked && !km.inKeyguardRestrictedInputMode()) onScreenUnlock();

                long currentDraw = System.nanoTime();
                float delta = (currentDraw - lastDraw) * 0.000001f;

                float screenBitmapWidth = (float) bitmap.get(primaryIndex).getWidth() * (float) screenHeight / (float) bitmap.get(primaryIndex).getHeight();
                drawOffset = (float) x.nextDraw(targetOffset, drawOffset, delta);
                drawScale = (float) s.nextDraw(targetScale, drawScale, delta);
                drawBrightness = (float) a.nextDraw(targetBrightness, drawBrightness, delta);
                if (bitmapChanged) {
                    layers = new ArrayList<Layer>() {
                        {
                            for (Bitmap b : bitmap) {
                                add(new BitmapLayer(b) {{
                                    setBlendMode(BlendMode.Normal);
                                }});
                            }
                        }
                    };
                    bitmapChanged = false;
                }

                lastDraw = currentDraw;
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
                    layer.setScale(drawScale);
                    layer.setOffset((double) drawOffset / (double) (screenBitmapWidth - screenWidth));
                    layer.render(gl, params);
                }

                solidColorLayer.setAlpha(1 - drawBrightness);
                solidColorLayer.render(gl, params);
                if (Settings.getBoolean(sharedPreferences, Settings.SHOW_FRAME_DELAY))
                    fpsLayer.render(gl, params);
            }

            public void onSurfaceChanged(GL10 gl, int width, int height) {
                screenWidth = width;
                screenHeight = height;
                screenRatio = (float) width / (float) height;
                returnToDefault();
            }

            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            }

            void release() {
            }
        }
    }
}