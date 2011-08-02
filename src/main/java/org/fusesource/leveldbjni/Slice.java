/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.leveldbjni;

import org.fusesource.hawtjni.runtime.*;

import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;
import static org.fusesource.hawtjni.runtime.MethodFlag.CONSTANT_INITIALIZER;

/**
 * Provides a java interface to the C++ leveldb::Slice class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(name="leveldb::Slice", flags={ClassFlag.STRUCT, ClassFlag.CPP})
class Slice {

    @JniClass(flags={ClassFlag.CPP})
    static class SliceJNI {
        static {
            DB.LIBRARY.load();
            init();
        }

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Slice *") long ptr);

        public static final native void memmove (
                @JniArg(cast="void *") long dest,
                @JniArg(cast="const void *", flags={ArgFlag.NO_OUT, ArgFlag.CRITICAL}) Slice src,
                @JniArg(cast="size_t") long size);

        public static final native void memmove (
                @JniArg(cast="void *", flags={ArgFlag.NO_IN, ArgFlag.CRITICAL}) Slice dest,
                @JniArg(cast="const void *") long src,
                @JniArg(cast="size_t") long size);


        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();

        @JniField(flags={CONSTANT}, accessor="sizeof(struct leveldb::Slice)")
        static int SIZEOF;

    }
    
    
    @JniField(cast="const char*")
    private long data_;
    @JniField(cast="size_t")
    private long size_;

    public Slice() {
    }

    public Slice(long data, long length) {
        this.data_ = data;
        this.size_ = length;
    }

    public Slice(NativeBuffer buffer) {
        this(buffer.pointer(), buffer.capacity());
    }

    public long data() {
        return data_;
    }

    public Slice data(long data) {
        this.data_ = data;
        return this;
    }

    public long size() {
        return size_;
    }

    public Slice size(long size) {
        this.size_ = size;
        return this;
    }

    public Slice set(Slice buffer) {
        this.size_ = buffer.size_;
        this.data_ = buffer.data_;
        return this;
    }

    public Slice set(NativeBuffer buffer) {
        this.size_ = buffer.capacity();
        this.data_ = buffer.pointer();
        return this;
    }

    public byte[] toByteArray() {
        if( size_ > Integer.MAX_VALUE ) {
            throw new ArrayIndexOutOfBoundsException("Native slice is larger than the maximum Java array");
        }
        byte []rc = new byte[(int) size_];
        NativeBuffer.NativeBufferJNI.buffer_copy(data_, 0, rc, 0, rc.length);
        return rc;
    }
    
    static NativeBuffer arrayCreate(int dimension) {
        return new NativeBuffer(dimension*SliceJNI.SIZEOF);
    }

    void write(long buffer, int index) {
        SliceJNI.memmove(PointerMath.add(buffer, SliceJNI.SIZEOF*index), this, SliceJNI.SIZEOF);
    }

    void read(long buffer, int index) {
        SliceJNI.memmove(this, PointerMath.add(buffer, SliceJNI.SIZEOF*index), SliceJNI.SIZEOF);
    }
    

}
