package pw.lictex.wallpaper.layer;

import android.graphics.Bitmap;

import javax.microedition.khronos.opengles.GL10;

import pw.lictex.wallpaper.i.GLBitmap;

/**
 * Created by kpx on 1.5-2018.
 */

public class BitmapLayer extends Layer {
    private GLBitmap glBitmap;
    private boolean bitmapChanged = false;
    private Bitmap bitmap;

    public BitmapLayer(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void change(GL10 gl, Bitmap bitmap) {
        release(gl);
        glBitmap = null;
        this.bitmap = bitmap;
    }

    @Override
    protected void onRender(GL10 gl, RenderParams params) {
        if (glBitmap == null || bitmapChanged) {
            if (glBitmap != null) glBitmap.release(gl);
            glBitmap = new GLBitmap();
            glBitmap.loadGLTexture(gl, bitmap);
            bitmapChanged = false;
        }
        gl.glPushMatrix();
        float bitmapRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
        gl.glScalef(bitmapRatio, 1, 1);
        gl.glScalef(scale, scale, scale);
        gl.glTranslatef(1f - 1f / bitmapRatio * params.screenRatio, 0, 0);

        gl.glTranslatef((float) (-offset * (2d - params.screenRatio * 2f / bitmapRatio)), 0, 0);

        glBitmap.setColor(new float[]{1, 1, 1, alpha});
        glBitmap.setBlendMode(blendMode);
        glBitmap.draw(gl);
        gl.glPopMatrix();
    }

    private void release(GL10 gl) {
        if (glBitmap != null) glBitmap.release(gl);
    }
}
