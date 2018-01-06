package pw.lictex.wallpaper.layer;

import android.graphics.Bitmap;

import javax.microedition.khronos.opengles.GL10;

import pw.lictex.wallpaper.i.GLBitmap;

/**
 * Created by kpx on 1.5-2018.
 */

public class BitmapLayer extends Layer {
    GLBitmap glBitmap;
    boolean bitmapChanged = false;
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
    public void render(GL10 gl, int w, int h) {
        if (glBitmap == null || bitmapChanged) {
            if (glBitmap != null) glBitmap.release(gl);
            glBitmap = new GLBitmap();
            glBitmap.loadGLTexture(gl, bitmap);
            bitmapChanged = false;
        }
        int screenBitmapWidth = (int) ((float) bitmap.getWidth() * h / bitmap.getHeight());
        gl.glPushMatrix();
        gl.glViewport(0, 0, screenBitmapWidth, h);
        gl.glTranslatef(-(float) offset / (float) h, 0, 0);

        float x = ((screenBitmapWidth - w) / 2f - offset) / h;
        gl.glTranslatef(-x, 0, 0);
        gl.glScalef(scale, scale, scale);
        gl.glTranslatef(x, 0, 0);

        glBitmap.setColor(new float[]{1, 1, 1, alpha});
        glBitmap.draw(gl);
        gl.glPopMatrix();
    }

    private void release(GL10 gl) {
        if (glBitmap != null) glBitmap.release(gl);
    }
}
