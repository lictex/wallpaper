package pw.lictex.wallpaper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
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

        addSeekBar("位移速度", 0, 200, Settings.GYRO_TRANSLATE_SPEED, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("滑动位移速度", 0, 200, Settings.TOUCH_TRANSLATE_SPEED, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("平滑亮度", 0, 32, Settings.ALPHA_EASE, null);
        addSeekBar("平滑缩放", 0, 32, Settings.SCALE_EASE, null);
        addSeekBar("平滑位移", 0, 32, Settings.TRANSLATE_EASE, null);
        addSeparator();
        addSeekBar("传感器回报率", 0, 3, Settings.GYRO_DELAY, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                switch (i) {
                    case 0:
                        return "最快";
                    case 1:
                        return "快";
                    case 2:
                        return "正常";
                    default:
                        return "慢";
                }
            }
        });
        addSwitch("使用旋转向量传感器", Settings.USE_ROTATION_VECTOR);
        addSeparator();
        addSeekBar("关屏亮度", 0, 100, Settings.ALPHA_SCREEN_OFF, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("关屏缩放", 100, 200, Settings.SCALE_SCREEN_OFF, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("锁屏亮度", 0, 100, Settings.ALPHA_SCREEN_ON, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("锁屏缩放", 100, 200, Settings.SCALE_SCREEN_ON, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("解锁亮度", 0, 100, Settings.ALPHA_SCREEN_UNLOCKED, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("解锁缩放", 100, 200, Settings.SCALE_SCREEN_UNLOCKED, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeparator();
        addSeekBar("默认位置", 0, 100, Settings.DEFAULT_POSITION, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return i + "%";
            }
        });
        addSeekBar("关屏返回默认位置时间", 0, 61, Settings.RETURN_DEFAULT_TIME, new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                switch (i) {
                    case 61:
                        return "禁用";
                    case 0:
                        return "立刻";
                    default:
                        return i + "s";
                }
            }
        });
        addButton("加载图片", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                stopService(MainActivity.this.intent);
                startActivityForResult(intent, 1024);
            }
        });
        addSwitch("显示FPS", Settings.SHOW_FRAME_DELAY);
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

    private void addSeekBar(String name, final int min, int max, final String tag, @Nullable Slider.ValueParser valueParser) {
        Slider slider = new Slider(this);
        slider.setName(name);
        slider.setId(View.generateViewId());
        slider.getSeekBar().setMax(max - min);
        slider.setTag(Slider.TAG_MIN, min);
        int i = Settings.getInt(sharedPreferences, tag);
        slider.setValueParser(valueParser == null ? new Slider.ValueParser() {
            @Override
            public String process(Slider s, int i) {
                return String.valueOf(i + min);
            }
        } : valueParser);
        slider.setValue(slider.getSeekBar().getProgress());
        slider.getSeekBar().setProgress(i - min);
        slider.setOnSeekBarChangeListener(new Slider.OnSeekBarChange() {
            @Override
            public void onChange(Slider s) {
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

    private void addSwitch(String name, final String tag) {
        RelativeLayout switchR = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.switch_, null);
        Switch b = switchR.findViewById(R.id.switch_);
        b.setText(name);
        switchR.setId(View.generateViewId());
        b.setChecked(Settings.getBoolean(sharedPreferences, tag));
        b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferences.edit().putBoolean(tag, isChecked).apply();
            }
        });
        layout.addView(switchR);
    }
}
















