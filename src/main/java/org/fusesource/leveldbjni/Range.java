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
@JniClass(name="leveldb::Range", flags={ClassFlag.STRUCT, ClassFlag.CPP})
public class Range {

    @JniClass(flags={ClassFlag.CPP})
    static class RangeJNI {
        static {
            DB.LIBRARY.load();
            init();
        }

        public static final native void memmove (
                @JniArg(cast="void *") long dest,
                @JniArg(cast="const void *", flags={ArgFlag.NO_OUT, ArgFlag.CRITICAL}) Range src,
                @JniArg(cast="size_t") long size);

        public static final native void memmove (
                @JniArg(cast="void *", flags={ArgFlag.NO_IN, ArgFlag.CRITICAL}) Range dest,
                @JniArg(cast="const void *") long src,
                @JniArg(cast="size_t") long size);


        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();

        @JniField(flags={CONSTANT}, accessor="sizeof(struct leveldb::Range)")
        static int SIZEOF;

    }

    @JniField
    private Slice start = new Slice();

    @JniField
    private Slice limit = new Slice();

    public Slice limit() {
        return limit;
    }

    public Slice start() {
        return start;
    }

    static NativeBuffer arrayCreate(int dimension) {
        return new NativeBuffer(dimension*RangeJNI.SIZEOF);
    }

    void arrayWrite(long buffer, int index) {
        RangeJNI.memmove(PointerMath.add(buffer, RangeJNI.SIZEOF*index), this, RangeJNI.SIZEOF);
    }

    void arrayRead(long buffer, int index) {
        RangeJNI.memmove(this, PointerMath.add(buffer, RangeJNI.SIZEOF*index), RangeJNI.SIZEOF);
    }

}
