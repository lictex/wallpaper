package pw.lictex.wallpaper.layer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by kpx on 1.14-2018.
 */

public class SolidColorLayer extends Layer {
    private static final float triangleCoords[] = {
            -1.0f, -1.0f, 0.0f, // V1 - bottom left
            -1.0f, 1.0f, 0.0f, // V2 - top left
            1.0f, -1.0f, 0.0f, // V3 - bottom right
            1.0f, 1.0f, 0.0f // V4 - top right
    };
    private FloatBuffer floatBuffer;

    public SolidColorLayer() {
        ByteBuffer allocate = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        allocate.order(ByteOrder.nativeOrder());
        floatBuffer = allocate.asFloatBuffer();
        floatBuffer.put(triangleCoords);
        floatBuffer.position(0);
    }

    @Override
    protected void onRender(GL10 gl, RenderParams params) {
        switch (blendMode) {
            case Normal:
                gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
                break;
            case Additive:
                gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE);
                break;
        }
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_BLEND);
        gl.glColor4f(0, 0, 0, alpha);
        gl.glFrontFace(GL10.GL_CW);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, floatBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, triangleCoords.length / 3);
    }
}
