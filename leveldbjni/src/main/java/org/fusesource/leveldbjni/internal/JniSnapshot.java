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

import org.fusesource.leveldbjni.internal.*;
import org.iq80.leveldb.Snapshot;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JniSnapshot implements Snapshot {

    private final NativeDB db;
    private final NativeSnapshot snapshot;

    JniSnapshot(NativeDB db, NativeSnapshot snapshot) {
        this.db = db;
        this.snapshot = snapshot;
    }

    public void close() {
        db.releaseSnapshot(snapshot);
    }

    NativeSnapshot snapshot() {
        return snapshot;
    }
}
