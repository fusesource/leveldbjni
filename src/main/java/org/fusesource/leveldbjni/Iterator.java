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
 * Provides a java interface to the C++ leveldb::Iterator class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Iterator extends NativeObject {

    @JniClass(name="leveldb::Iterator", flags={ClassFlag.CPP})
    private static class IteratorJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Iterator *") long self);

        @JniMethod(flags={MethodFlag.CPP})
        static final native boolean Valid(
                @JniArg(cast="leveldb::Iterator *") long self
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void SeekToFirst(
                @JniArg(cast="leveldb::Iterator *") long self
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void SeekToLast(
                @JniArg(cast="leveldb::Iterator *") long self
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void Seek(
                @JniArg(cast="leveldb::Iterator *") long self,
                @JniArg(flags={ArgFlag.BY_VALUE, ArgFlag.NO_OUT}) Slice target
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void Next(
                @JniArg(cast="leveldb::Iterator *") long self
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void Prev(
                @JniArg(cast="leveldb::Iterator *") long self
                );

        @JniMethod(copy="leveldb::Slice", flags={MethodFlag.CPP})
        static final native long key(
                @JniArg(cast="leveldb::Iterator *") long self
                );

        @JniMethod(copy="leveldb::Slice", flags={MethodFlag.CPP})
        static final native long value(
                @JniArg(cast="leveldb::Iterator *") long self
                );

        @JniMethod(copy="leveldb::Status", flags={MethodFlag.CPP})
        static final native long status(
                @JniArg(cast="leveldb::Iterator *") long self
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
