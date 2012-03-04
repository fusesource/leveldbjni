/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *    * Neither the name of FuseSource Corp. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fusesource.leveldbjni.internal;

import java.nio.ByteBuffer;

import org.fusesource.leveldbjni.DataWidth;
import org.fusesource.leveldbjni.KeyValueChunk;

import org.fusesource.hawtjni.runtime.*;

import static org.fusesource.hawtjni.runtime.MethodFlag.*;
import static org.fusesource.hawtjni.runtime.ArgFlag.*;
import static org.fusesource.hawtjni.runtime.ClassFlag.*;

/**
 * Provides a java interface to the C++ leveldb::Iterator class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class NativeIterator extends NativeObject {

    @JniClass(name="leveldb::Iterator", flags={CPP})
    private static class IteratorJNI {
        static {
            NativeDB.LIBRARY.load();
        }

        @JniMethod(flags={CPP_DELETE})
        public static final native void delete(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native boolean Valid(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void SeekToFirst(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void SeekToLast(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void Seek(
                long self,
                @JniArg(flags={BY_VALUE, NO_OUT}) NativeSlice target
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void Next(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void Prev(
                long self
                );

        @JniMethod(copy="leveldb::Slice", flags={CPP_METHOD})
        static final native long key(
                long self
                );

        @JniMethod(copy="leveldb::Slice", flags={CPP_METHOD})
        static final native long value(
                long self
                );

        @JniMethod(copy="leveldb::Status", flags={CPP_METHOD})
        static final native long status(
                long self
                );

        @JniMethod(accessor="chunk_pairs")
        static final native void nextChunk(
                @JniArg(cast = "void *") long self,
                ChunkMetadata meta,
                int maxByteSize,
                @JniArg(cast = "char *", flags={CRITICAL}) byte[] buffer,
                boolean encodeKeys,
                boolean encodeVals,
                int keyWidth,
                int valWidth
                );
    }

    @JniClass(flags={STRUCT})
    public static class ChunkMetadata {
        public int byteLength;
        public int pairLength;
    }

    NativeIterator(long self) {
        super(self);
    }

    protected void doRealDelete() {
        IteratorJNI.delete(self);
    }

    public boolean isValid() {
        assertAllocated();
        return IteratorJNI.Valid(self);
    }

    private void checkStatus() throws NativeDB.DBException {
        NativeDB.checkStatus(IteratorJNI.status(self));
    }

    public void seekToFirst() {
        assertAllocated();
        IteratorJNI.SeekToFirst(self);
    }

    public void seekToLast() {
        assertAllocated();
        IteratorJNI.SeekToLast(self);
    }

    public void seek(byte[] key) throws NativeDB.DBException {
        NativeDB.checkArgNotNull(key, "key");
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            seek(keyBuffer);
        } finally {
            keyBuffer.delete();
        }
    }

    private void seek(NativeBuffer keyBuffer) throws NativeDB.DBException {
        seek(new NativeSlice(keyBuffer));
    }

    private void seek(NativeSlice keySlice) throws NativeDB.DBException {
        assertAllocated();
        IteratorJNI.Seek(self, keySlice);
        checkStatus();
    }

    public void next() throws NativeDB.DBException {
        assertAllocated();
        IteratorJNI.Next(self);
        checkStatus();
    }

    /**
     * Consume the next chunk of key/value pairs into the specified buffer. The buffer will be cleared prior
     * to the read, and on return will have position == 0, limit = total bytes read.
     *
     * @param buffer The buffer to read the chunk into
     * @param keyWidth The encoding to use for key data
     * @param valWidth The encoding to use for value data
     */
    public KeyValueChunk nextChunk(ByteBuffer buffer, DataWidth keyWidth, DataWidth valWidth) throws NativeDB.DBException {
        assertAllocated();
        ChunkMetadata meta = new ChunkMetadata();
        buffer.clear();
        IteratorJNI.nextChunk(self,
                              meta,
                              buffer.capacity(),
                              buffer.array(),
                              keyWidth.isRunLengthEncoded(),
                              valWidth.isRunLengthEncoded(),
                              keyWidth.getEncodingWidth(),
                              valWidth.getEncodingWidth());
        buffer.limit(meta.byteLength);
        KeyValueChunk retVal = new KeyValueChunk(buffer, meta.pairLength, keyWidth, valWidth);
        checkStatus();
        return retVal;
    }

    public void prev() throws NativeDB.DBException {
        assertAllocated();
        IteratorJNI.Prev(self);
        checkStatus();
    }

    public byte[] key() throws NativeDB.DBException {
        assertAllocated();
        long slice_ptr = IteratorJNI.key(self);
        checkStatus();
        try {
            NativeSlice slice = new NativeSlice();
            slice.read(slice_ptr, 0);
            return slice.toByteArray();
        } finally {
            NativeSlice.SliceJNI.delete(slice_ptr);
        }
    }

    public byte[] value() throws NativeDB.DBException {
        assertAllocated();
        long slice_ptr = IteratorJNI.value(self);
        checkStatus();
        try {
            NativeSlice slice = new NativeSlice();
            slice.read(slice_ptr, 0);
            return slice.toByteArray();
        } finally {
            NativeSlice.SliceJNI.delete(slice_ptr);
        }
    }
}
