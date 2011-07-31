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
public class WriteOptions extends NativeObject {

    @JniClass(name="leveldb::WriteOptions", flags={ClassFlag.CPP})
    private static class WriteOptionsJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="leveldb::WriteOptions *")
        public static final native long create();
        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::WriteOptions *") long ptr);

        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.GETTER})
        public static final native boolean sync(@JniArg(cast="leveldb::WriteOptions *") long ptr);
        @JniMethod(flags = {MethodFlag.CPP, MethodFlag.SETTER})
        public static final native void sync(@JniArg(cast="leveldb::WriteOptions *") long ptr, boolean value);

    }

    public WriteOptions() {
        super(WriteOptionsJNI.create());
    }

    public void delete() {
        assertAllocated();
        WriteOptionsJNI.delete(ptr);
        ptr = 0;
    }

    public void setSync(boolean value) {
        assertAllocated();
        WriteOptionsJNI.sync(ptr, value);
    }
    public boolean getSync() {
        assertAllocated();
        return WriteOptionsJNI.sync(ptr);
    }

}
