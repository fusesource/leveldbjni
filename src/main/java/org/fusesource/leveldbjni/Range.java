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
 * Provides a java interface to the C++ leveldb::ReadOptions class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Range {

    @JniClass(name="leveldb::Range", flags={ClassFlag.STRUCT, ClassFlag.CPP})
    static public class RangeJNI {

        static {
            DB.LIBRARY.load();
            init();
        }

        public static final native void memmove (
                @JniArg(cast="void *") long dest,
                @JniArg(cast="const void *", flags={ArgFlag.NO_OUT, ArgFlag.CRITICAL}) RangeJNI src,
                @JniArg(cast="size_t") long size);

        public static final native void memmove (
                @JniArg(cast="void *", flags={ArgFlag.NO_IN, ArgFlag.CRITICAL}) RangeJNI dest,
                @JniArg(cast="const void *") long src,
                @JniArg(cast="size_t") long size);


        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();

        @JniField(flags={CONSTANT}, accessor="sizeof(struct leveldb::Range)")
        static int SIZEOF;

        @JniField
        Slice start = new Slice();
        @JniField(ignore = true)
        NativeBuffer start_buffer;

        @JniField
        Slice limit = new Slice();
        @JniField(ignore = true)
        NativeBuffer limit_buffer;

        public RangeJNI(Range range) {
            start_buffer = new NativeBuffer(range.start());
            start.set(start_buffer);
            try {
                limit_buffer = new NativeBuffer(range.limit());
            } catch (OutOfMemoryError e) {
                start_buffer.delete();
                throw e;
            }
            limit.set(limit_buffer);
        }

        public void delete() {
            start_buffer.delete();
            limit_buffer.delete();
        }

        static NativeBuffer arrayCreate(int dimension) {
            return new NativeBuffer(dimension*SIZEOF);
        }

        void arrayWrite(long buffer, int index) {
            RangeJNI.memmove(PointerMath.add(buffer, SIZEOF * index), this, SIZEOF);
        }

        void arrayRead(long buffer, int index) {
            RangeJNI.memmove(this, PointerMath.add(buffer, SIZEOF * index), SIZEOF);
        }

    }

    final private byte[] start;
    final private byte[] limit;

    public byte[] limit() {
        return limit;
    }

    public byte[] start() {
        return start;
    }

    public Range(byte[] start, byte[] limit) {
        DB.checkArgNotNull(start, "start");
        DB.checkArgNotNull(limit, "limit");
        this.limit = limit;
        this.start = start;
    }
}
