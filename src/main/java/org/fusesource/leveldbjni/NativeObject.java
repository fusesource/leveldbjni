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

/**
 * A helper base class which is used to track a pointer to a native
 * structure or class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class NativeObject {

    protected long self;

    protected NativeObject(long self) {
        this.self = self;
        if( self ==0 ) {
            throw new OutOfMemoryError("Failure allocating native heap memory");
        }
    }

    long pointer() {
        return self;
    }

    public boolean isAllocated() {
        return self !=0;
    }

    protected void assertAllocated() {
        assert isAllocated() : "This object has been deleted";
    }

}
