package pw.lictex.wallpaper.layer;

import android.support.annotation.Nullable;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by kpx on 1.5-2018.
 */

public abstract class Layer {
    protected Layer parent;
    protected float offset = 0;
    protected float scale = 1;
    protected float alpha = 1;

    Layer(@Nullable Layer parent) {
        this.parent = parent;
    }

    public void render(GL10 gl, RenderParams params) {

    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public static class RenderParams {
        public int screenWidth, screenHeight;
        public float screenRatio;
    }
}
