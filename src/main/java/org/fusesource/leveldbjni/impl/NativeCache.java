/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.leveldbjni.impl;

import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniMethod;

import static org.fusesource.hawtjni.runtime.ClassFlag.CPP;
import static org.fusesource.hawtjni.runtime.MethodFlag.CPP_DELETE;

/**
 * Provides a java interface to the C++ leveldb::Cache class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class NativeCache extends NativeObject {

    @JniClass(name="leveldb::Cache", flags={CPP})
    private static class CacheJNI {
        static {
            NativeDB.LIBRARY.load();
        }

        @JniMethod(cast="leveldb::Cache *", accessor="leveldb::NewLRUCache")
        public static final native long NewLRUCache(
                @JniArg(cast="size_t") long capacity);

        @JniMethod(flags={CPP_DELETE})
        public static final native void delete(long self);
    }

    public NativeCache(long capacity) {
        super(CacheJNI.NewLRUCache(capacity));
    }

    public void delete() {
        assertAllocated();
        CacheJNI.delete(self);
        self = 0;
    }

}
