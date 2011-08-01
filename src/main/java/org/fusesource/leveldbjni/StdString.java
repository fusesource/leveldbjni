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
 * Provides a java interface to the C++ std::string class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class StdString extends NativeObject {

    @JniClass(name="std::string", flags={ClassFlag.CPP})
    private static class StdStringJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="std::string *")
        public static final native long create();

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="std::string *")
        public static final native long create(String value);

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        static final native void delete(
                @JniArg(cast="std::string *") long self);

        @JniMethod(flags={MethodFlag.CPP}, accessor = "c_str", cast="const char*")
        public static final native long c_str_ptr (
                @JniArg(cast="std::string *") long self);

        @JniMethod(flags={MethodFlag.CPP},cast = "size_t")
        public static final native long length (
                @JniArg(cast="std::string *") long self);

    }

    public StdString(long self) {
        super(self);
    }

    public StdString() {
        super(StdStringJNI.create());
    }

    public void delete() {
        assertAllocated();
        StdStringJNI.delete(self);
        self = 0;
    }

    public String toString() {
        return new String(toByteArray());
    }

    public long length() {
        assertAllocated();
        return StdStringJNI.length(self);
    }

    public byte[] toByteArray() {
        long l = length();
        if( l > Integer.MAX_VALUE ) {
            throw new ArrayIndexOutOfBoundsException("Native string is larger than the maximum Java array");
        }
        byte []rc = new byte[(int) l];
        NativeBuffer.NativeBufferJNI.buffer_copy(StdStringJNI.c_str_ptr(self), 0, rc, 0, rc.length);
        return rc;
    }
}
