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

import org.fusesource.leveldbjni.internal.NativeWriteBatch;
import org.iq80.leveldb.WriteBatch;

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

    public WriteBatch put(byte[] key, byte[] value) {
        writeBatch.put(key, value);
        return this;
    }

    public WriteBatch delete(byte[] key) {
        writeBatch.delete(key);
        return this;
    }

    public NativeWriteBatch writeBatch() {
        return writeBatch;
    }
}
