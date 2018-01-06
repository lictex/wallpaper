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
        super(null);
        this.bitmap = bitmap;
    }

    public void change(GL10 gl, Bitmap bitmap) {
        release(gl);
        glBitmap = null;
        this.bitmap = bitmap;
    }

    @Override
    public void render(GL10 gl, float screenRatio) {
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
        gl.glTranslatef(1f - 1f / bitmapRatio * screenRatio, 0, 0);

        gl.glTranslatef(-offset * 2f, 0, 0);

        glBitmap.setColor(new float[]{1, 1, 1, alpha});
        glBitmap.draw(gl);
        gl.glPopMatrix();
    }

    private void release(GL10 gl) {
        if (glBitmap != null) glBitmap.release(gl);
    }
}
