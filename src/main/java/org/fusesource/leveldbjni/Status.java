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
 * Provides a java interface to the C++ leveldb::Status class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class Status extends NativeObject{

    @JniClass(name="leveldb::Status", flags={ClassFlag.CPP})
    static class StatusJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Status *") long self);

        @JniMethod(flags={MethodFlag.CPP})
        public static final native boolean ok(
                @JniArg(cast="leveldb::Status *") long self);

        @JniMethod(flags={MethodFlag.CPP})
        public static final native boolean IsNotFound(
                @JniArg(cast="leveldb::Status *") long self);

        @JniMethod(copy="std::string", flags={MethodFlag.CPP})
        public static final native long ToString(
                @JniArg(cast="leveldb::Status *") long self);
    }

    public Status(long self) {
        super(self);
    }

    public void delete() {
        assertAllocated();
        StatusJNI.delete(self);
        self = 0;
    }

    public boolean isOk() {
        assertAllocated();
        return StatusJNI.ok(self);
    }

    public boolean isNotFound() {
        assertAllocated();
        return StatusJNI.IsNotFound(self);
    }

    public String toString() {
        assertAllocated();
        long strptr = StatusJNI.ToString(self);
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
