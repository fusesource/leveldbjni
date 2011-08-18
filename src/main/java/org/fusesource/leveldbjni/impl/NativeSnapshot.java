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

/**
 * Provides a java interface to the C++ leveldb::Snapshot class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class NativeSnapshot extends NativeObject {

    NativeSnapshot(long self) {
        super(self);
    }

}
