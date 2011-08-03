/**
 * Copyright (C) 2010, FuseSource Corp.  All rights reserved.
 */
package org.fusesource.leveldbjni;

import org.fusesource.hawtjni.runtime.*;

import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;
import static org.fusesource.hawtjni.runtime.FieldFlag.POINTER_FIELD;
import static org.fusesource.hawtjni.runtime.MethodFlag.CONSTANT_INITIALIZER;

/**
 * <p>
 * Provides a java interface to the C++ leveldb::Logger class.
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract class Logger extends NativeObject {

    @JniClass(name="JNILogger", flags={ClassFlag.STRUCT, ClassFlag.CPP})
    static public class LoggerJNI {

        static {
            DB.LIBRARY.load();
            init();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="JNILogger *")
        public static final native long create();
        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="JNILogger *") long ptr);

        public static final native void memmove (
                @JniArg(cast="void *") long dest,
                @JniArg(cast="const void *", flags={ArgFlag.NO_OUT, ArgFlag.CRITICAL}) LoggerJNI src,
                @JniArg(cast="size_t") long size);

        @JniField(cast="JNIEnv *")
        long env;

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

    public Logger() {
        super(LoggerJNI.create());
        try {
            globalRef = DB.DBJNI.NewGlobalRef(this);
            if( globalRef==0 ) {
                throw new RuntimeException("jni call failed: NewGlobalRef");
            }
            long clz = DB.DBJNI.GetObjectClass(this);
            if( clz==0 ) {
                throw new RuntimeException("jni call failed: GetObjectClass");
            }

            LoggerJNI struct = new LoggerJNI();
            struct.log_method = DB.DBJNI.GetMethodID(clz, "log", "(Ljava/lang/String;)V");
            if( struct.log_method ==0 ) {
                throw new RuntimeException("jni call failed: GetMethodID");
            }
            struct.env = DB.DBJNI.ENV;
            struct.target = globalRef;
            LoggerJNI.memmove(self, struct, LoggerJNI.SIZEOF);

        } catch (RuntimeException e) {
            delete();
            throw e;
        }
    }

    Logger(long ptr) {
        super(ptr);
    }

    public void delete() {
        if( globalRef!=0 ) {
            DB.DBJNI.DeleteGlobalRef(globalRef);
            globalRef = 0;
        }
    }

    abstract void log(String message);

}
