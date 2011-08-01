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
 * Provides a java interface to the C++ leveldb::ReadOptions class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(name="leveldb::ReadOptions", flags={ClassFlag.STRUCT, ClassFlag.CPP})
public class ReadOptions {

    @JniField
    private boolean verify_checksums = false;

    @JniField
    private boolean fill_cache = true;

    @JniField(cast="const leveldb::Snapshot*")
    private long snapshot=0;

    public boolean isFillCache() {
        return fill_cache;
    }

    public void setFillCache(boolean fill_cache) {
        this.fill_cache = fill_cache;
    }

    public Snapshot getSnapshot() {
        if( snapshot == 0 ) {
            return null;
        } else {
            return new Snapshot(snapshot);
        }
    }

    public void setSnapshot(Snapshot snapshot) {
        if( snapshot==null ) {
            this.snapshot = 0;
        } else {
            this.snapshot = snapshot.pointer();
        }
    }

    public boolean isVerifyChecksums() {
        return verify_checksums;
    }

    public void setVerifyChecksums(boolean verify_checksums) {
        this.verify_checksums = verify_checksums;
    }
}
