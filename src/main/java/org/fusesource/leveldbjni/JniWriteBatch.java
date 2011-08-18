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

import org.fusesource.leveldbjni.impl.NativeWriteBatch;
import org.iq80.leveldb.api.WriteBatch;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JniWriteBatch implements WriteBatch {

    private final NativeWriteBatch writeBatch;

    JniWriteBatch(NativeWriteBatch writeBatch) {
        this.writeBatch = writeBatch;
    }

    public void close() {
        writeBatch.delete();
    }

    public void put(byte[] key, byte[] value) {
        writeBatch.put(key, value);
    }

    public void delete(byte[] key) {
        writeBatch.delete(key);
    }

    public NativeWriteBatch writeBatch() {
        return writeBatch;
    }
}
