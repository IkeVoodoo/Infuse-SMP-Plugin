package me.ikevoodoo.infusesmp.utils;

import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    public static int readInt(InputStream is) throws IOException {
        byte[] bytes = new byte[4];
        if(is.read(bytes) == -1) {
            throw new IllegalStateException("Unable to grab int from input stream!");
        }

        return Ints.fromByteArray(bytes);
    }

    public static void writeInt(int i, OutputStream os) throws IOException {
        os.write(Ints.toByteArray(i));
    }


}
