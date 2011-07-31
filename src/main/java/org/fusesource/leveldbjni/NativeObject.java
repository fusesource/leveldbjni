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
public abstract class NativeObject {

    protected long ptr;

    protected NativeObject(long ptr) {
        this.ptr = ptr;
        if( ptr==0 ) {
            throw new OutOfMemoryError("Failure allocating native heap memory");
        }
    }

    long pointer() {
        return ptr;
    }

    public boolean isAllocated() {
        return ptr!=0;
    }

    protected void assertAllocated() {
        assert isAllocated() : "This object has been deleted";
    }

    abstract public void delete();


}
