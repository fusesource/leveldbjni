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
 * Provides a java interface to the C++ leveldb::Cache class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Cache extends NativeObject {

    @JniClass(name="leveldb::Cache", flags={ClassFlag.CPP})
    private static class CacheJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(cast="leveldb::Cache *", accessor="leveldb::NewLRUCache")
        public static final native long NewLRUCache(
                @JniArg(cast="size_t") long capacity);

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Cache *") long self);
    }

    public Cache(long capacity) {
        super(CacheJNI.NewLRUCache(capacity));
    }

    public void delete() {
        assertAllocated();
        CacheJNI.delete(self);
        self = 0;
    }

}
