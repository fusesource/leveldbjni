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
 * Provides a java interface to the C++ leveldb::WriteOptions class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(name="leveldb::WriteOptions", flags={ClassFlag.STRUCT, ClassFlag.CPP})
public class WriteOptions {

    @JniField
    boolean sync;

//    @JniField(cast="const leveldb::Snapshot**")
//    long post_write_snapshot;

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

//    public void enablePostWriteSnapshot(boolean value) {
//        if( value ) {
//            post_write_snapshot = new long[1];
//        } else {
//            post_write_snapshot = null;
//        }
//    }
//
//    public Snapshot getPostWriteSnapshot() {
//        if( post_write_snapshot==null || post_write_snapshot[0]==0 ) {
//            return null;
//        } else {
//            return new Snapshot(post_write_snapshot[0]);
//        }
//    }

}
