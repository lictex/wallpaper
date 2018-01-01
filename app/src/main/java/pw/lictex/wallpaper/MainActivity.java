package pw.lictex.wallpaper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    LinearLayout layout;
    SharedPreferences sharedPreferences;
    boolean bitmapChanged = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("重置");
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("重置")) {
            bitmapChanged = true;
            sharedPreferences.edit().clear().commit();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MA", "onCreate()");
        setContentView(R.layout.activity_main);
        intent = new Intent(this, WallpaperService.class);
        layout = findViewById(R.id.root);
        layout.removeAllViews();
        sharedPreferences = getSharedPreferences("root", Context.MODE_PRIVATE);
        addSeekBar("位移速度(%)", 0, 200, Settings.GYRO_TRANSLATE_SPEED, 100);
        addSeekBar("滑动位移速度(%)", 0, 200, Settings.TOUCH_TRANSLATE_SPEED, 75);
        addSeekBar("亮度减速器", 0, 32, Settings.ALPHA_EASE, 10);
        addSeekBar("缩放减速器", 0, 32, Settings.SCALE_EASE, 12);
        addSeekBar("位移减速器", 0, 32, Settings.TRANSLATE_EASE, 8);
        addSeekBar("陀螺仪延迟", 0, 3, Settings.GYRO_DELAY, 2);
        addSeparator();
        addSeekBar("关屏亮度(%)", 0, 100, Settings.ALPHA_SCREEN_OFF, 0);
        addSeekBar("关屏缩放(%)", 100, 200, Settings.SCALE_SCREEN_OFF, 150);
        addSeekBar("锁屏亮度(%)", 0, 100, Settings.ALPHA_SCREEN_ON, 64);
        addSeekBar("锁屏缩放(%)", 100, 200, Settings.SCALE_SCREEN_ON, 125);
        addSeekBar("解锁亮度(%)", 0, 100, Settings.ALPHA_SCREEN_UNLOCKED, 100);
        addSeekBar("解锁缩放(%)", 100, 200, Settings.SCALE_SCREEN_UNLOCKED, 100);
        addSeparator();
        addSeekBar("默认位置(%)", 0, 100, Settings.DEFAULT_POSITION, 78);
        addSeekBar("屏幕关闭后返回默认位置[61s=never](s)", 0, 61, Settings.RETURN_DEFAULT_TIME, 5);
        addButton("自定义图片", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                stopService(MainActivity.this.intent);
                startActivityForResult(intent, 1024);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1024: {
                if (resultCode == RESULT_OK) {
                    bitmapChanged = true;
                    Uri uri = data.getData();
                    Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        byte[] b = Utils.InputStreamToByteArray(inputStream);
                        FileOutputStream fileOutputStream = openFileOutput("img", MODE_PRIVATE);
                        fileOutputStream.write(b);
                        fileOutputStream.close();
                        sharedPreferences.edit().putString(Settings.EXT_IMG_PATH, "img").apply();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent i = (Intent) intent.clone();
        i.putExtra("bitmapChanged", bitmapChanged);
        bitmapChanged = false;
        startService(i);
    }

    private void addSeekBar(String name, final int min, int max, final String tag, int def) {
        Slider slider = new Slider(this);
        slider.setName(name);
        slider.setId(View.generateViewId());
        slider.getSeekBar().setMax(max - min);
        int i = sharedPreferences.getInt(tag, def);
        slider.getSeekBar().setProgress(i - min);
        slider.setValue(String.valueOf(slider.getSeekBar().getProgress() + min));
        slider.setOnSeekBarChangeListener(new Slider.OnSeekBarChange() {
            @Override
            public void onChange(Slider s) {
                s.setValue(String.valueOf(s.getSeekBar().getProgress() + min));
                sharedPreferences.edit().putInt(tag, s.getSeekBar().getProgress() + min).apply();
            }
        });
        layout.addView(slider);
    }

    private void addSeparator() {
        RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.separator, null);
        relativeLayout.setId(View.generateViewId());
        layout.addView(relativeLayout);
    }

    private void addButton(String name, Button.OnClickListener onClickListener) {
        RelativeLayout buttonR = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.button, null);
        Button b = buttonR.findViewById(R.id.btn);
        b.setOnClickListener(onClickListener);
        b.setText(name);
        buttonR.setId(View.generateViewId());
        layout.addView(buttonR);
    }
}
















