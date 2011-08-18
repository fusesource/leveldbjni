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

import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniField;
import org.fusesource.hawtjni.runtime.JniMethod;

import static org.fusesource.hawtjni.runtime.ArgFlag.CRITICAL;
import static org.fusesource.hawtjni.runtime.ArgFlag.NO_OUT;
import static org.fusesource.hawtjni.runtime.ClassFlag.CPP;
import static org.fusesource.hawtjni.runtime.ClassFlag.STRUCT;
import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;
import static org.fusesource.hawtjni.runtime.FieldFlag.POINTER_FIELD;
import static org.fusesource.hawtjni.runtime.MethodFlag.*;

/**
 * <p>
 * Provides a java interface to the C++ leveldb::Logger class.
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public abstract class NativeLogger extends NativeObject {

    @JniClass(name="JNILogger", flags={STRUCT, CPP})
    static public class LoggerJNI {

        static {
            NativeDB.LIBRARY.load();
            init();
        }

        @JniMethod(flags={CPP_NEW})
        public static final native long create();

        @JniMethod(flags={CPP_DELETE})
        public static final native void delete(
                long self
                );

        public static final native void memmove (
                @JniArg(cast="void *") long dest,
                @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) LoggerJNI src,
                @JniArg(cast="size_t") long size);

        @JniField(cast="jobject", flags={POINTER_FIELD})
        long target;

        @JniField(cast="jmethodID", flags={POINTER_FIELD})
        long log_method;

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();

        @JniField(flags={CONSTANT}, accessor="sizeof(struct JNILogger)")
        static int SIZEOF;
    }

    private long globalRef;

    public NativeLogger() {
        super(LoggerJNI.create());
        try {
            globalRef = NativeDB.DBJNI.NewGlobalRef(this);
            if( globalRef==0 ) {
                throw new RuntimeException("jni call failed: NewGlobalRef");
            }
            long clz = NativeDB.DBJNI.GetObjectClass(this);
            if( clz==0 ) {
                throw new RuntimeException("jni call failed: GetObjectClass");
            }

            LoggerJNI struct = new LoggerJNI();
            struct.log_method = NativeDB.DBJNI.GetMethodID(clz, "log", "(Ljava/lang/String;)V");
            if( struct.log_method ==0 ) {
                throw new RuntimeException("jni call failed: GetMethodID");
            }
            struct.target = globalRef;
            LoggerJNI.memmove(self, struct, LoggerJNI.SIZEOF);

        } catch (RuntimeException e) {
            delete();
            throw e;
        }
    }

    NativeLogger(long ptr) {
        super(ptr);
    }

    public void delete() {
        if( globalRef!=0 ) {
            NativeDB.DBJNI.DeleteGlobalRef(globalRef);
            globalRef = 0;
        }
    }

    public abstract void log(String message);

}
