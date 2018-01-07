package pw.lictex.wallpaper.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import javax.microedition.khronos.opengles.GL10;

import pw.lictex.wallpaper.i.GLBitmap;

/**
 * Created by kpx on 1.7-2018.
 */

public class FPSLayer extends Layer {
    private Bitmap bmp;
    private long time = 0;
    private int frames;
    private GLBitmap glBitmap = new GLBitmap();

    public FPSLayer() {
        super(null);
    }

    @Override
    public void render(GL10 gl, RenderParams params) {
        frames++;
        if (System.currentTimeMillis() - time > 1000) {
            time = System.currentTimeMillis();

            bmp = Bitmap.createBitmap(params.screenWidth, 72, Bitmap.Config.ARGB_8888);
            Canvas canvasTemp = new Canvas(bmp);
            canvasTemp.drawColor(Color.TRANSPARENT);
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setFakeBoldText(true);
            p.setTextSize(64);
            canvasTemp.drawText(String.valueOf(frames) + "fps", 0, 64, p);
            glBitmap.release(gl);
            glBitmap.loadGLTexture(gl, bmp);
            frames = 0;
        }
        if (bmp == null) return;

        gl.glPushMatrix();
        gl.glTranslatef(0, -1 + 72f / params.screenWidth, 0);
        gl.glScalef(1, 72f / params.screenWidth, 1);
        gl.glScalef(params.screenRatio, params.screenRatio, 1);
        glBitmap.draw(gl);
        gl.glPopMatrix();
    }
}
