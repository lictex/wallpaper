package pw.lictex.wallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.RawRes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kpx on 12.31-2017.
 */

public class Utils {
    public static byte[] InputStreamToByteArray(InputStream in) throws IOException {
        int BUFFER_SIZE = 1024;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count;
        while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);
        return outStream.toByteArray();
    }

    public static Bitmap bitmapFromResource(Context context, @RawRes int id) throws IOException {
        return bitmapFromInputStream(context.getResources().openRawResourceFd(id).createInputStream());
    }

    public static Bitmap bitmapFromInputStream(InputStream inputStream) throws IOException {
        byte[] bytes = InputStreamToByteArray(inputStream);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
