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
 * Provides a java interface to the C++ leveldb::ReadOptions class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class WriteBatch extends NativeObject {

    @JniClass(name="leveldb::ReadOptions", flags={ClassFlag.CPP})
    private static class ReadOptionsJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="leveldb::ReadOptions *")
        public static final native long create();
        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::ReadOptions *") long ptr);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native boolean verify_checksums(@JniArg(cast="leveldb::ReadOptions *") long ptr);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void verify_checksums(@JniArg(cast="leveldb::ReadOptions *") long ptr, boolean value);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native boolean fill_cache(@JniArg(cast="leveldb::ReadOptions *") long ptr);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void fill_cache(@JniArg(cast="leveldb::ReadOptions *") long ptr, boolean value);
    }

    public WriteBatch() {
        super(ReadOptionsJNI.create());
    }

    public void delete() {
        assertAllocated();
        ReadOptionsJNI.delete(ptr);
        ptr = 0;
    }

    public void setVerifyChecksums(boolean value) {
        assertAllocated();
        ReadOptionsJNI.verify_checksums(ptr, value);
    }
    public boolean getVerifyChecksums() {
        assertAllocated();
        return ReadOptionsJNI.verify_checksums(ptr);
    }

    public void setFillCache(boolean value) {
        assertAllocated();
        ReadOptionsJNI.fill_cache(ptr, value);
    }
    public boolean getFillCache() {
        assertAllocated();
        return ReadOptionsJNI.fill_cache(ptr);
    }


}
