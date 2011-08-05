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

import java.io.File;
import java.io.IOException;

import static org.fusesource.hawtjni.runtime.MethodFlag.JNI;
import static org.fusesource.hawtjni.runtime.MethodFlag.POINTER_RETURN;

/**
 * Some miscellaneous utility functions.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Util {

    @JniClass(flags={ClassFlag.CPP})
    static class UtilJNI {

        static {
            DB.LIBRARY.load();
        }

        @JniMethod()
        static final native int link(
                @JniArg(cast="const char*") String source,
                @JniArg(cast="const char*") String target);

        @JniMethod(flags={MethodFlag.CONSTANT_GETTER})
        public static final native int errno();

        @JniMethod(cast="char *")
        public static final native long strerror(int errnum);

        public static final native int strlen(
                @JniArg(cast="const char *")long s);

    }

    /**
     * Creates a hard link from source to target.
     * @param source
     * @param target
     * @return
     */
    public static void link(File source, File target) throws IOException {
        if( UtilJNI.link(source.getCanonicalPath(), target.getCanonicalPath()) != 0 ) {
            throw new IOException("link failed: "+strerror());
        }
    }

    static int errno() {
        return UtilJNI.errno();
    }

    static String strerror() {
        return string(UtilJNI.strerror(errno()));
    }

    static String string(long ptr) {
        if( ptr == 0 )
            return null;
        return new String(new Slice(ptr, UtilJNI.strlen(ptr)).toByteArray());
    }

}
