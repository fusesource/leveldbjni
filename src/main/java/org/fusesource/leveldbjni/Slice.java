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
        public static final native void delete(@JniArg(cast="leveldb::Slice *") long ptr);

        @JniMethod(flags={MethodFlag.CPP}, cast="const char*")
        public static final native long data(@JniArg(cast="leveldb::Slice *") long ptr);

        @JniMethod(flags={MethodFlag.CPP}, cast="size_t")
        public static final native long size(@JniArg(cast="leveldb::Slice *") long ptr);
    }

    public Slice(long ptr, long length) {
        super(SliceJNI.create(ptr, length));
    }

    public Slice(NativeBuffer buffer) {
        this(buffer.pointer(), buffer.capacity());
    }

    public void delete() {
        assertAllocated();
        SliceJNI.delete(ptr);
        ptr = 0;
    }

    long getData() {
        assertAllocated();
        return SliceJNI.data(ptr);
    }

    long getSize() {
        assertAllocated();
        return SliceJNI.size(ptr);
    }

}
