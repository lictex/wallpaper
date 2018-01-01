package pw.lictex.wallpaper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * TODO: document your custom view class.
 */
public class Slider extends RelativeLayout {
    SeekBar s;
    TextView n, v;
    OnSeekBarChange onSeekBarChange;

    public Slider(Context context) {
        super(context);
        init();
    }

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Slider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChange onSeekBarChange) {
        this.onSeekBarChange = onSeekBarChange;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.slider, this);

        s = findViewById(R.id.seekBar);
        n = findViewById(R.id.nameTextView);
        v = findViewById(R.id.valueTextView);
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (onSeekBarChange != null) onSeekBarChange.onChange(Slider.this);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public SeekBar getSeekBar() {
        return s;
    }

    public void setName(String s) {
        n.setText(s);
    }

    public void setValue(String s) {
        v.setText(s);
    }

    public interface OnSeekBarChange {
        void onChange(Slider s);
    }
}