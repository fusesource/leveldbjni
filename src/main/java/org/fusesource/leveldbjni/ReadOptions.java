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

import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniField;

import static org.fusesource.hawtjni.runtime.ClassFlag.CPP;
import static org.fusesource.hawtjni.runtime.ClassFlag.STRUCT;

/**
 * Provides a java interface to the C++ leveldb::ReadOptions class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(name="leveldb::ReadOptions", flags={STRUCT, CPP})
public class ReadOptions {

    @JniField
    private boolean verify_checksums = false;

    @JniField
    private boolean fill_cache = true;

    @JniField(cast="const leveldb::Snapshot*")
    private long snapshot=0;

    public boolean fillCache() {
        return fill_cache;
    }

    public ReadOptions fillCache(boolean fill_cache) {
        this.fill_cache = fill_cache;
        return this;
    }

    public Snapshot snapshot() {
        if( snapshot == 0 ) {
            return null;
        } else {
            return new Snapshot(snapshot);
        }
    }

    public ReadOptions snapshot(Snapshot snapshot) {
        if( snapshot==null ) {
            this.snapshot = 0;
        } else {
            this.snapshot = snapshot.pointer();
        }
        return this;
    }

    public boolean verifyChecksums() {
        return verify_checksums;
    }

    public ReadOptions verifyChecksums(boolean verify_checksums) {
        this.verify_checksums = verify_checksums;
        return this;
    }
}
