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

/**
 * Provides a java interface to the C++ leveldb::Slice class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Slice extends NativeObject {

    @JniClass(name="leveldb::Slice", flags={ClassFlag.CPP})
    private static class SliceJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="leveldb::Slice *")
        public static final native long create(
                @JniArg(cast="const char *") long data,
                @JniArg(cast="size_t") long n);

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Slice *") long self);

        @JniMethod(flags={MethodFlag.CPP}, cast="const char*")
        public static final native long data(@JniArg(cast="leveldb::Slice *") long self);

        @JniMethod(flags={MethodFlag.CPP}, cast="size_t")
        public static final native long size(@JniArg(cast="leveldb::Slice *") long self);
    }

    public Slice(long self) {
        super(self);
    }

    public Slice(long self, long length) {
        super(SliceJNI.create(self, length));
    }

    public Slice(NativeBuffer buffer) {
        this(buffer.pointer(), buffer.capacity());
    }

    public void delete() {
        assertAllocated();
        SliceJNI.delete(self);
        self = 0;
    }

    long data() {
        assertAllocated();
        return SliceJNI.data(self);
    }

    long length() {
        assertAllocated();
        return SliceJNI.size(self);
    }

    public byte[] toByteArray() {
        long l = length();
        if( l > Integer.MAX_VALUE ) {
            throw new ArrayIndexOutOfBoundsException("Native slice is larger than the maximum Java array");
        }
        byte []rc = new byte[(int) l];
        NativeBuffer.NativeBufferJNI.buffer_copy(SliceJNI.data(self), 0, rc, 0, rc.length);
        return rc;
    }

}
