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
public class Snapshot extends NativeObject {

    @JniClass(name="leveldb::Iterator", flags={ClassFlag.CPP})
    private static class IteratorJNI {
        static {
            DB.LIBRARY.load();
        }

        @JniMethod(flags={MethodFlag.CPP_NEW}, cast="leveldb::Iterator *")
        public static final native long create();
        @JniMethod(flags={MethodFlag.CPP_DELETE})
        public static final native void delete(@JniArg(cast="leveldb::Iterator *") long ptr);

        @JniMethod(flags={MethodFlag.CPP})
        static final native boolean Valid(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void SeekToFirst(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void SeekToLast(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void Seek(
                @JniArg(cast="leveldb::Iterator *") long ptr,
                @JniArg(cast="leveldb::Slice *", flags={ArgFlag.BY_VALUE}) long target
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void Next(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );

        @JniMethod(flags={MethodFlag.CPP})
        static final native void Prev(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );

        @JniMethod(copy="leveldb::Slice", flags={MethodFlag.CPP})
        static final native long key(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );

        @JniMethod(copy="leveldb::Slice", flags={MethodFlag.CPP})
        static final native long value(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );

        @JniMethod(copy="leveldb::Status", flags={MethodFlag.CPP})
        static final native long status(
                @JniArg(cast="leveldb::Iterator *") long ptr
                );
    }

    public Snapshot() {
        super(IteratorJNI.create());
    }

    public void delete() {
        assertAllocated();
        IteratorJNI.delete(ptr);
        ptr = 0;
    }

    public boolean isValid() {
        assertAllocated();
        return IteratorJNI.Valid(ptr);
    }

    private void checkStatus() throws DB.DBException {
        DB.checkStatus(IteratorJNI.status(ptr));
    }

    public void seekToFirst() {
        assertAllocated();
        IteratorJNI.SeekToFirst(ptr);
    }

    public void seekToLast() {
        assertAllocated();
        IteratorJNI.SeekToLast(ptr);
    }

    public void seek(byte[] key) throws DB.DBException {
        NativeBuffer keyBuffer = new NativeBuffer(key);
        try {
            seek(keyBuffer);
        } finally {
            keyBuffer.delete();
        }
    }

    private void seek(NativeBuffer keyBuffer) throws DB.DBException {
        Slice keySlice = new Slice(keyBuffer);
        try {
            seek(keySlice);
        } finally {
            keySlice.delete();
        }
    }

    private void seek(Slice keySlice) throws DB.DBException {
        assertAllocated();
        IteratorJNI.Seek(ptr, keySlice.pointer());
        checkStatus();
    }

    public void next() throws DB.DBException {
        assertAllocated();
        IteratorJNI.Next(ptr);
        checkStatus();
    }

    public void prev() throws DB.DBException {
        assertAllocated();
        IteratorJNI.Prev(ptr);
        checkStatus();
    }

    public byte[] key() throws DB.DBException {
        assertAllocated();
        long slice_ptr = IteratorJNI.key(ptr);
        checkStatus();
        Slice slice = new Slice(slice_ptr);
        try {
            return slice.toByteArray();
        } finally {
            slice.delete();
        }
    }

    public byte[] value() throws DB.DBException {
        assertAllocated();
        long slice_ptr = IteratorJNI.value(ptr);
        checkStatus();
        Slice slice = new Slice(slice_ptr);
        try {
            return slice.toByteArray();
        } finally {
            slice.delete();
        }
    }
}
