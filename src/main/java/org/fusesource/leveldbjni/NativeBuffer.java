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

import java.util.concurrent.atomic.AtomicInteger;

import static org.fusesource.hawtjni.runtime.ArgFlag.CRITICAL;
import static org.fusesource.hawtjni.runtime.ArgFlag.NO_IN;
import static org.fusesource.hawtjni.runtime.ArgFlag.NO_OUT;

/**
 * A NativeBuffer allocates a native buffer on the heap.  It supports
 * creating sub slices/views of that buffer and manages reference tracking
 * so that the the native buffer is freed once all NativeBuffer views
 * are deleted.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class NativeBuffer extends NativeObject {

    @JniClass
    static class NativeBufferJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(cast="void *")
        public static final native long malloc(
                @JniArg(cast="size_t") long size);

        public static final native void free(
                @JniArg(cast="void *") long self);

        public static final native void buffer_copy (
                @JniArg(cast="const void *") long src,
                @JniArg(cast="size_t") long srcPos,
                @JniArg(cast="void *") long dest,
                @JniArg(cast="size_t") long destPos,
                @JniArg(cast="size_t") long length);

        public static final native void buffer_copy (
                @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) byte[] src,
                @JniArg(cast="size_t") long srcPos,
                @JniArg(cast="void *") long dest,
                @JniArg(cast="size_t") long destPos,
                @JniArg(cast="size_t") long length);

        public static final native void buffer_copy (
                @JniArg(cast="const void *") long src,
                @JniArg(cast="size_t") long srcPos,
                @JniArg(cast="void *", flags={NO_IN, CRITICAL}) byte[] dest,
                @JniArg(cast="size_t") long destPos,
                @JniArg(cast="size_t") long length);

        @JniMethod(cast="void *")
        public static final native long memset (
                @JniArg(cast="void *") long buffer,
                int c,
                @JniArg(cast="size_t") long num);

        public static final native int strlen(
                @JniArg(cast="char *")long s);    }

    private final long capacity;
    private final AtomicInteger retained;

    public NativeBuffer(long capacity) {
        super(NativeBufferJNI.malloc(capacity));
        this.capacity = capacity;
        this.retained = new AtomicInteger(1);
    }

    public NativeBuffer(byte data[]) {
        this(data, 0, data.length);
    }

    public NativeBuffer(byte data[], int offset, int length) {
        super(NativeBufferJNI.malloc(length));
        this.capacity = length;
        this.retained = new AtomicInteger(1);
        write(0, data, 0, length);
    }

    private NativeBuffer(NativeBuffer other, long offset, long capacity) {
        super(PointerMath.add(other.self, offset));
        this.retained = other.retained;
        this.capacity = capacity;
        retained.incrementAndGet();
    }

    public NativeBuffer slice(long offset, long length) {
        assertAllocated();
        if( length < 0 ) throw new IllegalArgumentException("length cannot be negative");
        if( offset < 0 ) throw new IllegalArgumentException("offset cannot be negative");
        if( offset+length >= capacity) throw new ArrayIndexOutOfBoundsException("offset + length exceed the length of this buffer");
        return new NativeBuffer(this, offset, length);
    }

    public NativeBuffer head(long length) {
        return slice(0, length);
    }

    public NativeBuffer tail(long length) {
        if( capacity-length < 0) throw new ArrayIndexOutOfBoundsException("capacity-length cannot be less than zero");
        return slice(capacity-length, length);
    }

    public void delete() {
        assertAllocated();
        int r = retained.decrementAndGet();
        if( r < 0 ) {
            throw new Error("The object has already been deleted.");
        } else if( r==0 ) {
            NativeBufferJNI.free(self);
        }
        self = 0;
    }

    public long capacity() {
        return capacity;
    }

    public void write(long at, byte []source, int offset, int length) {
        assertAllocated();
        if( length < 0 ) throw new IllegalArgumentException("length cannot be negative");
        if( offset < 0 ) throw new IllegalArgumentException("offset cannot be negative");
        if( at < 0 ) throw new IllegalArgumentException("at cannot be negative");
        if( at+length > capacity ) throw new ArrayIndexOutOfBoundsException("at + length exceeds the capacity of this object");
        if( offset+length > source.length) throw new ArrayIndexOutOfBoundsException("offset + length exceed the length of the source buffer");
        NativeBufferJNI.buffer_copy(source, offset, self, at, length);
    }

    public void read(long at, byte []target, int offset, int length) {
        assertAllocated();
        if( length < 0 ) throw new IllegalArgumentException("length cannot be negative");
        if( offset < 0 ) throw new IllegalArgumentException("offset cannot be negative");
        if( at < 0 ) throw new IllegalArgumentException("at cannot be negative");
        if( at+length > capacity ) throw new ArrayIndexOutOfBoundsException("at + length exceeds the capacity of this object");
        if( offset+length > target.length) throw new ArrayIndexOutOfBoundsException("offset + length exceed the length of the target buffer");
        NativeBufferJNI.buffer_copy(self, at, target, offset, length);
    }

    public byte[] toByteArray() {
        if( capacity > Integer.MAX_VALUE ) {
            throw new OutOfMemoryError("Native buffer larger than the largest allowed Java byte[]");
        }
        byte [] rc = new byte[(int) capacity];
        read(0, rc, 0, rc.length);
        return rc;
    }
}
