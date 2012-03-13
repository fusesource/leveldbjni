package org.fusesource.leveldbjni.internal;

import java.nio.ByteBuffer;

import org.fusesource.leveldbjni.DataWidth;

public class ChunkWorker {
    public static int getNextChunk(long iterPtr, ByteBuffer keyBuffer, ByteBuffer valBuffer, DataWidth keyEncoding, DataWidth valEncoding) {
        return getNextChunkNative(iterPtr,
                                  keyBuffer,
                                  valBuffer,
                                  keyEncoding.isRunLengthEncoded(),
                                  valEncoding.isRunLengthEncoded(),
                                  keyEncoding.getEncodingWidth(),
                                  valEncoding.getEncodingWidth());
    }

    public static native int getNextChunkNative(long iterPtr, ByteBuffer keyBuffer, ByteBuffer valBuffer, boolean encodeKeys, boolean encodeVals, int keyWidth, int valWidth);
}
