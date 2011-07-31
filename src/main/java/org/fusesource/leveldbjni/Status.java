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
class Status extends NativeObject{

    @JniClass(name="leveldb::Status", flags={ClassFlag.CPP})
    static class StatusJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Status *") long ptr);

        @JniMethod(flags={MethodFlag.CPP})
        public static final native boolean ok(
                @JniArg(cast="leveldb::Status *") long ptr);

        @JniMethod(flags={MethodFlag.CPP})
        public static final native boolean IsNotFound(
                @JniArg(cast="leveldb::Status *") long ptr);

        @JniMethod(copy="std::string", flags={MethodFlag.CPP})
        public static final native long ToString(
                @JniArg(cast="leveldb::Status *") long ptr);
    }

    public Status(long ptr) {
        super(ptr);
    }

    @Override
    public void delete() {
        assertAllocated();
        StatusJNI.delete(ptr);
        ptr = 0;
    }

    public boolean isOk() {
        assertAllocated();
        return StatusJNI.ok(ptr);
    }

    public boolean isNotFound() {
        assertAllocated();
        return StatusJNI.IsNotFound(ptr);
    }

    public String toString() {
        assertAllocated();
        long strptr = StatusJNI.ToString(ptr);
        if( strptr==0 ) {
            return null;
        } else {
            StdString rc = new StdString(strptr);
            try {
                return rc.toString();
            } finally {
                rc.delete();
            }
        }
    }

}
