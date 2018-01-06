package pw.lictex.wallpaper.layer;

import android.support.annotation.Nullable;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by kpx on 1.5-2018.
 */

public abstract class Layer {
    protected Layer parent;
    protected int offset;
    protected float scale = 1;
    protected float alpha = 1;

    Layer(@Nullable Layer parent) {
        this.parent = parent;
    }

    public void render(GL10 gl, int w, int h) {

    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
