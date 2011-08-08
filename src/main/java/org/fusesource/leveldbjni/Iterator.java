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
import static org.fusesource.hawtjni.runtime.FieldFlag.*;
import static org.fusesource.hawtjni.runtime.MethodFlag.*;
import static org.fusesource.hawtjni.runtime.ArgFlag.*;
import static org.fusesource.hawtjni.runtime.ClassFlag.*;

/**
 * Provides a java interface to the C++ leveldb::Iterator class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Iterator extends NativeObject {

    @JniClass(name="leveldb::Iterator", flags={CPP})
    private static class IteratorJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={CPP_DELETE})
        public static final native void delete(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native boolean Valid(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void SeekToFirst(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void SeekToLast(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void Seek(
                long self,
                @JniArg(flags={BY_VALUE, NO_OUT}) Slice target
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void Next(
                long self
                );

        @JniMethod(flags={CPP_METHOD})
        static final native void Prev(
                long self
                );

        @JniMethod(copy="leveldb::Slice", flags={CPP_METHOD})
        static final native long key(
                long self
                );

        @JniMethod(copy="leveldb::Slice", flags={CPP_METHOD})
        static final native long value(
                long self
                );

        @JniMethod(copy="leveldb::Status", flags={CPP_METHOD})
        static final native long status(
                long self
                );
    }

    Iterator(long self) {
        super(self);
    }

    public void delete() {
        assertAllocated();
        IteratorJNI.delete(self);
        self = 0;
    }

    public boolean isValid() {
        assertAllocated();
        return IteratorJNI.Valid(self);
    }

    private void checkStatus() throws DB.DBException {
        DB.checkStatus(IteratorJNI.status(self));
    }

    public void seekToFirst() {
        assertAllocated();
        IteratorJNI.SeekToFirst(self);
    }

    public void seekToLast() {
        assertAllocated();
        IteratorJNI.SeekToLast(self);
    }

    public void seek(byte[] key) throws DB.DBException {
        DB.checkArgNotNull(key, "key");
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            seek(keyBuffer);
        } finally {
            keyBuffer.delete();
        }
    }

    private void seek(NativeBuffer keyBuffer) throws DB.DBException {
        seek(new Slice(keyBuffer));
    }

    private void seek(Slice keySlice) throws DB.DBException {
        assertAllocated();
        IteratorJNI.Seek(self, keySlice);
        checkStatus();
    }

    public void next() throws DB.DBException {
        assertAllocated();
        IteratorJNI.Next(self);
        checkStatus();
    }

    public void prev() throws DB.DBException {
        assertAllocated();
        IteratorJNI.Prev(self);
        checkStatus();
    }

    public byte[] key() throws DB.DBException {
        assertAllocated();
        long slice_ptr = IteratorJNI.key(self);
        checkStatus();
        try {
            Slice slice = new Slice();
            slice.read(slice_ptr, 0);
            return slice.toByteArray();
        } finally {
            Slice.SliceJNI.delete(slice_ptr);
        }
    }

    public byte[] value() throws DB.DBException {
        assertAllocated();
        long slice_ptr = IteratorJNI.value(self);
        checkStatus();
        try {
            Slice slice = new Slice();
            slice.read(slice_ptr, 0);
            return slice.toByteArray();
        } finally {
            Slice.SliceJNI.delete(slice_ptr);
        }
    }
}
