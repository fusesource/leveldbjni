/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.leveldbjni.internal;

import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniMethod;

import static org.fusesource.hawtjni.runtime.ClassFlag.CPP;
import static org.fusesource.hawtjni.runtime.MethodFlag.CPP_DELETE;
import static org.fusesource.hawtjni.runtime.MethodFlag.CPP_METHOD;

/**
 * Provides a java interface to the C++ leveldb::Status class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class NativeStatus extends NativeObject{

    @JniClass(name="leveldb::Status", flags={CPP})
    static class StatusJNI {
        static {
            NativeDB.LIBRARY.load();
        }

        @JniMethod(flags={CPP_DELETE})
        public static final native void delete(
                long self);

        @JniMethod(flags={CPP_METHOD})
        public static final native boolean ok(
                long self);

        @JniMethod(flags={CPP_METHOD})
        public static final native boolean IsNotFound(
                long self);

        @JniMethod(copy="std::string", flags={CPP_METHOD})
        public static final native long ToString(
                long self);
    }

    public NativeStatus(long self) {
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
            NativeStdString rc = new NativeStdString(strptr);
            try {
                return rc.toString();
            } finally {
                rc.delete();
            }
        }
    }

}
