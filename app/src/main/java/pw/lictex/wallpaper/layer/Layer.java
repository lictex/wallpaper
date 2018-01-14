package pw.lictex.wallpaper.layer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by kpx on 1.5-2018.
 */

public abstract class Layer {
    protected double offset = 0;
    protected float scale = 1;
    protected float alpha = 1;
    protected BlendMode blendMode = BlendMode.Normal;

    protected abstract void onRender(GL10 gl, RenderParams params);

    public final void render(GL10 gl, RenderParams params) {
        onRender(gl, params);
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public enum BlendMode {
        Normal, Additive
    }

    public static class RenderParams {
        public int screenWidth, screenHeight;
        public float screenRatio;
    }
}
